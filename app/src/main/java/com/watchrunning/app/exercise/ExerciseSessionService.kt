package com.watchrunning.app.exercise

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseEndReason
import androidx.health.services.client.data.ExerciseState
import androidx.health.services.client.data.ExerciseUpdate
import androidx.health.services.client.data.HeartRateAccuracy
import androidx.health.services.client.data.LocationAccuracy
import androidx.health.services.client.data.LocationAvailability
import com.watchrunning.app.BuildConfig
import com.watchrunning.app.WatchRunningApplication
import com.watchrunning.app.calculation.HeartRateZoneCalculator
import com.watchrunning.app.calculation.LocationSourceSelector
import com.watchrunning.app.calculation.PaceEstimator
import com.watchrunning.app.calculation.SystemMonotonicClock
import com.watchrunning.app.calculation.ZoneTimeAccumulator
import com.watchrunning.app.model.HeartRateSample
import com.watchrunning.app.model.GpsStatus
import com.watchrunning.app.model.GpsSource
import com.watchrunning.app.model.LiveMetrics
import com.watchrunning.app.model.LocationSample
import com.watchrunning.app.model.PaceEstimate
import com.watchrunning.app.model.PaceUnavailableReason
import com.watchrunning.app.model.WorkoutCommand
import com.watchrunning.app.model.WorkoutPhase
import com.watchrunning.app.model.WorkoutUiState
import com.watchrunning.app.model.ZoneConfiguration
import java.time.Duration
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ExerciseSessionService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val commandMutex = Mutex()
    private val locationMutex = Mutex()
    private lateinit var application: WatchRunningApplication
    private lateinit var repository: WorkoutRepository
    private lateinit var gateway: ExerciseGateway
    private lateinit var gnssLocationGateway: GnssLocationGateway
    private lateinit var fusedLocationGateway: FusedLocationGateway
    private lateinit var notifier: OngoingWorkoutNotifier

    private var paceEstimator = PaceEstimator()
    private var zoneConfiguration: ZoneConfiguration? = null
    private var zoneAccumulator: ZoneTimeAccumulator? = null
    private var sessionStartMonotonicMillis: Long? = null
    private var pauseOpen = false
    private var continuitySegment = 0
    private var routeFallbackDistance = 0.0
    private var previousAcceptedLocation: LocationSample? = null
    private var lastLocationFixMonotonic: Long? = null
    private var lastLocationAccuracyMetres: Double? = null
    private var healthServicesLocationSource = GpsSource.HEALTH_SERVICES
    private val locationSourceSelector = LocationSourceSelector()
    private var lastHeartRate: HeartRateSample? = null
    private var heartRateSum = 0.0
    private var heartRateCount = 0L
    private var maximumHeartRateSeen: Double? = null
    private var activeCheckpointMillis = 0L
    private var activeCheckpointMonotonic = 0L
    private var lastNotificationAt = 0L
    private var lastCommandAt = 0L
    private var ticker: Job? = null
    private val bootEpochMillis = System.currentTimeMillis() - SystemClock.elapsedRealtime()

    override fun onCreate() {
        super.onCreate()
        application = applicationContext as WatchRunningApplication
        repository = application.workoutRepository
        gateway = HealthServicesExerciseGateway(this)
        gnssLocationGateway = GnssLocationGateway(
            context = this,
            onLocation = { location -> scope.launch { processGnssLocation(location) } },
            onUnavailable = {
                repository.updateState { old ->
                    if (old.metrics.gpsSource != GpsSource.NONE) old else old.copy(
                        metrics = old.metrics.copy(gpsStatus = GpsStatus.ACQUIRING),
                    )
                }
            },
        )
        fusedLocationGateway = FusedLocationGateway(
            context = this,
            onLocation = { location -> scope.launch { processFusedLocation(location) } },
            onFailure = { error ->
                if (BuildConfig.DEBUG) Log.w(TAG, "Fused location unavailable; retaining other sources", error)
            },
        )
        notifier = OngoingWorkoutNotifier(this).also { it.createChannel() }
        startForeground(
            OngoingWorkoutNotifier.NOTIFICATION_ID,
            notifier.build(repository.state.value.copy(phase = WorkoutPhase.Preparing)),
        )
        scope.launch { gateway.events.collect(::handleGatewayEvent) }
        scope.launch { recoverIfNeeded() }
        ticker = scope.launch { runTicker() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val command = intent?.getStringExtra(EXTRA_COMMAND)?.let(::commandFromName)
        if (command != null) scope.launch { execute(command) }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        gnssLocationGateway.stop()
        fusedLocationGateway.stop()
        scope.launch { runCatching { gateway.close() } }
        scope.cancel()
        super.onDestroy()
    }

    private suspend fun execute(command: WorkoutCommand) = commandMutex.withLock {
        val now = SystemMonotonicClock.nowMillis()
        if (now - lastCommandAt < COMMAND_DEBOUNCE_MILLIS) return@withLock
        lastCommandAt = now
        val state = repository.state.value
        val pending = WorkoutStateReducer.pending(state, command) ?: return@withLock
        repository.replaceState(pending)
        try {
            when (command) {
                WorkoutCommand.Prepare -> prepare()
                WorkoutCommand.Start, WorkoutCommand.StartWithoutFix -> gateway.start(hasHeartRatePermission())
                WorkoutCommand.Pause -> gateway.pause()
                WorkoutCommand.Resume -> gateway.resume()
                WorkoutCommand.RequestEnd -> repository.updateState { it.copy(pendingCommand = null) }
                WorkoutCommand.ConfirmEnd -> gateway.end()
                WorkoutCommand.CancelPrepare -> {
                    runCatching { gateway.end() }
                    repository.replaceState(WorkoutUiState())
                    stopSessionService()
                }
            }
        } catch (error: Throwable) {
            repository.fail(error.message ?: error.javaClass.simpleName)
        }
    }

    private suspend fun prepare() {
        val ownership = gateway.currentExercise()
        when (ownership) {
            ExerciseOwnership.OtherApp -> {
                repository.updateState {
                    it.copy(
                        phase = WorkoutPhase.Idle,
                        pendingCommand = null,
                        warning = "Another app is recording an exercise.",
                    )
                }
                stopSessionService()
                return
            }
            ExerciseOwnership.Ours -> {
                recoverIfNeeded()
                return
            }
            ExerciseOwnership.None -> Unit
        }

        val capabilities = gateway.capabilities()
        repository.updateCapabilities(capabilities)
        require(capabilities.runningSupported) { "Running is not supported by Health Services." }
        val settings = application.settingsRepository.settings.first()
        paceEstimator = PaceEstimator(settings.paceWindowSeconds)
        locationSourceSelector.reset()
        zoneConfiguration = settings.effectiveMaximumHeartRate?.let {
            HeartRateZoneCalculator.fromMaximumHeartRate(it)
        }
        zoneAccumulator = zoneConfiguration?.let(::ZoneTimeAccumulator)
        repository.replaceState(
            WorkoutUiState.newSession().copy(
                phase = WorkoutPhase.Preparing,
                preparationStartedAtMillis = SystemMonotonicClock.nowMillis(),
                pendingCommand = null,
            ),
        )
        gnssLocationGateway.start()
        fusedLocationGateway.start()
        gateway.prepare(hasHeartRatePermission())
    }

    private suspend fun recoverIfNeeded() {
        when (gateway.currentExercise()) {
            ExerciseOwnership.Ours -> {
                gnssLocationGateway.start()
                fusedLocationGateway.start()
                repository.replaceState(
                    repository.state.value.copy(
                        phase = WorkoutPhase.Starting,
                        sessionId = repository.state.value.sessionId ?: UUID.randomUUID().toString(),
                        warning = "Reconnected to the display session; earlier values are not retained.",
                    ),
                )
            }
            ExerciseOwnership.None -> Unit
            ExerciseOwnership.OtherApp -> Unit
        }
    }

    private suspend fun handleGatewayEvent(event: GatewayEvent) {
        when (event) {
            is GatewayEvent.Update -> handleExerciseUpdate(event.value)
            is GatewayEvent.CallbackFailure -> repository.fail(
                "Health Services listener failed: ${event.throwable.message}",
                interrupted = repository.state.value.hasActiveSession,
            )
            is GatewayEvent.AvailabilityChanged -> handleLocationAvailability(event)
        }
    }

    private fun handleLocationAvailability(event: GatewayEvent.AvailabilityChanged) {
        if (event.dataType != DataType.LOCATION) return
        val availability = event.availability as? LocationAvailability ?: return
        healthServicesLocationSource = when (availability) {
            LocationAvailability.ACQUIRED_TETHERED -> GpsSource.HEALTH_SERVICES_PHONE
            LocationAvailability.ACQUIRED_UNTETHERED -> GpsSource.HEALTH_SERVICES_WATCH
            else -> healthServicesLocationSource
        }
        val now = SystemMonotonicClock.nowMillis()
        if (hasUsableSelectedFix(now)) return
        val status = when (availability) {
            LocationAvailability.ACQUIRED_TETHERED,
            LocationAvailability.ACQUIRED_UNTETHERED,
            -> GpsStatus.ACQUIRED
            LocationAvailability.ACQUIRING,
            LocationAvailability.UNKNOWN,
            -> GpsStatus.ACQUIRING
            LocationAvailability.NO_GNSS -> GpsStatus.NO_GNSS
            LocationAvailability.UNAVAILABLE -> GpsStatus.UNAVAILABLE
            else -> GpsStatus.UNKNOWN
        }
        repository.updateState { old ->
            old.copy(
                metrics = old.metrics.copy(
                    gpsReady = false,
                    gpsStatus = status,
                    gpsSource = if (status == GpsStatus.ACQUIRED) healthServicesLocationSource else old.metrics.gpsSource,
                ),
            )
        }
    }

    @SuppressLint("RestrictedApi")
    private suspend fun handleExerciseUpdate(update: ExerciseUpdate) {
        val platformState = update.exerciseStateInfo.state
        val previousPhase = repository.state.value.phase
        val reportedPhase = platformState.toWorkoutPhase()
        val interrupted = reportedPhase == WorkoutPhase.Ended &&
            update.exerciseStateInfo.endReason != ExerciseEndReason.USER_END
        val phase = if (interrupted) WorkoutPhase.Interrupted else reportedPhase
        updateActiveCheckpoint(update, platformState == ExerciseState.ACTIVE)
        if (reportedPhase == WorkoutPhase.Active && sessionStartMonotonicMillis == null) createSession(update)
        if (reportedPhase == WorkoutPhase.Paused && previousPhase != WorkoutPhase.Paused) {
            openPause()
        } else if (reportedPhase == WorkoutPhase.Active && pauseOpen) {
            closePause()
        }
        processHeartRate(update, platformState == ExerciseState.ACTIVE)
        processHealthServicesLocations(update, platformState == ExerciseState.ACTIVE)

        if (phase == WorkoutPhase.Paused) {
            paceEstimator.breakContinuity()
            zoneAccumulator?.breakContinuity()
            previousAcceptedLocation = null
            continuitySegment++
        }

        repository.updateState { old ->
            val now = SystemMonotonicClock.nowMillis()
            val distance = authoritativeDistance()
            val active = currentActiveDuration(platformState == ExerciseState.ACTIVE)
            val elapsed = sessionStartMonotonicMillis?.let {
                Duration.ofMillis((now - it).coerceAtLeast(0))
            } ?: Duration.ZERO
            old.copy(
                phase = phase,
                pendingCommand = null,
                metrics = old.metrics.copy(
                    pace = if (phase == WorkoutPhase.Active) paceEstimator.estimate(now) else old.metrics.pace,
                    distanceMetres = distance,
                    activeDuration = active,
                    elapsedDuration = elapsed,
                    averagePaceSecondsPerKm = if (distance >= 50.0) active.toMillis() / distance else null,
                ),
                zoneTimeMillis = zoneAccumulator?.zoneMillis() ?: old.zoneTimeMillis,
                unclassifiedHeartRateMillis = zoneAccumulator?.unclassifiedMillis()
                    ?: old.unclassifiedHeartRateMillis,
                error = null,
            )
        }

        if (phase in setOf(WorkoutPhase.Ended, WorkoutPhase.Interrupted)) {
            finalizeSession()
            stopSessionService(delayMillis = 1_000)
        }
    }

    private fun updateActiveCheckpoint(update: ExerciseUpdate, isActive: Boolean) {
        val checkpoint = update.activeDurationCheckpoint ?: return
        activeCheckpointMillis = checkpoint.activeDuration.toMillis().coerceAtLeast(0)
        activeCheckpointMonotonic = checkpoint.time.toEpochMilli() - bootEpochMillis
        if (!isActive) activeCheckpointMonotonic = SystemMonotonicClock.nowMillis()
    }

    private fun processHeartRate(update: ExerciseUpdate, active: Boolean) {
        val points = update.latestMetrics.getData(DataType.HEART_RATE_BPM)
        for (point in points) {
            val accuracy = point.accuracy as? HeartRateAccuracy
            val acceptable = accuracy == null || accuracy.sensorStatus in setOf(
                HeartRateAccuracy.SensorStatus.ACCURACY_LOW,
                HeartRateAccuracy.SensorStatus.ACCURACY_MEDIUM,
                HeartRateAccuracy.SensorStatus.ACCURACY_HIGH,
            )
            val value = point.value
            if (!value.isFinite() || value <= 0.0 || !acceptable) continue
            val sample = HeartRateSample(value, point.timeDurationFromBoot.toMillis(), true)
            lastHeartRate = sample
            zoneAccumulator?.add(sample, active)
            if (active) {
                heartRateSum += value
                heartRateCount++
                maximumHeartRateSeen = maxOf(maximumHeartRateSeen ?: value, value)
            }
            val configuration = zoneConfiguration
            repository.updateState { old ->
                old.copy(
                    metrics = old.metrics.copy(
                        heartRateBpm = value.toInt(),
                        averageHeartRateBpm = if (heartRateCount > 0) heartRateSum / heartRateCount else null,
                        maximumHeartRateBpm = maximumHeartRateSeen,
                        heartRateZone = configuration?.let { HeartRateZoneCalculator.zone(value, it) }
                            ?: old.metrics.heartRateZone,
                        zoneIndicatorFraction = configuration?.let {
                            HeartRateZoneCalculator.indicatorFraction(value, it)
                        },
                        heartRateReady = true,
                        heartRateStale = false,
                    ),
                )
            }
        }
    }

    private suspend fun processGnssLocation(location: android.location.Location) = locationMutex.withLock {
        val now = SystemMonotonicClock.nowMillis()
        val sample = location.toLocationSample(now) ?: return@withLock
        if (BuildConfig.DEBUG) Log.d(TAG, "Watch GNSS fix: accuracy=${sample.horizontalErrorMeters}")
        if (!sample.isUsable(now)) {
            reportCandidate(sample, GpsSource.WATCH_GNSS, now)
            return@withLock
        }
        considerLocationSample(sample, GpsSource.WATCH_GNSS, now)
    }

    private suspend fun processFusedLocation(location: android.location.Location) = locationMutex.withLock {
        val now = SystemMonotonicClock.nowMillis()
        val sample = location.toLocationSample(now) ?: return@withLock
        if (BuildConfig.DEBUG) Log.d(TAG, "Fused fix: accuracy=${sample.horizontalErrorMeters}")
        if (!sample.isUsable(now)) {
            reportCandidate(sample, GpsSource.FUSED, now)
            return@withLock
        }
        considerLocationSample(sample, GpsSource.FUSED, now)
    }

    private suspend fun processHealthServicesLocations(update: ExerciseUpdate, active: Boolean) =
        locationMutex.withLock {
            val now = SystemMonotonicClock.nowMillis()
            for (point in update.latestMetrics.getData(DataType.LOCATION)) {
                val value = point.value
                val accuracy = (point.accuracy as? LocationAccuracy)
                    ?.horizontalPositionErrorMeters
                    ?.takeIf { it.isFinite() && it >= 0.0 }
                    ?: continue
                val sample = LocationSample(
                    latitude = value.latitude,
                    longitude = value.longitude,
                    altitudeMeters = value.altitude.takeIf { it.isFinite() },
                    bearingDegrees = value.bearing.takeIf { it.isFinite() && it in 0.0..<360.0 },
                    horizontalErrorMeters = accuracy,
                    monotonicMillis = point.timeDurationFromBoot.toMillis(),
                )
                if (!sample.isUsable(now)) {
                    reportCandidate(sample, healthServicesLocationSource, now)
                    continue
                }
                considerLocationSample(sample, healthServicesLocationSource, now, active)
            }
        }

    private fun considerLocationSample(
        sample: LocationSample,
        source: GpsSource,
        now: Long,
        active: Boolean = repository.state.value.phase == WorkoutPhase.Active,
    ) {
        val decision = locationSourceSelector.observe(source, sample.horizontalErrorMeters, now)
        if (!decision.useSample) return
        acceptLocationSample(sample, active, decision)
    }

    private fun android.location.Location.toLocationSample(now: Long): LocationSample? {
        val accuracy = accuracy.toDouble().takeIf { hasAccuracy() && it.isFinite() && it >= 0.0 }
            ?: return null
        return LocationSample(
            latitude = latitude,
            longitude = longitude,
            altitudeMeters = altitude.takeIf { hasAltitude() && it.isFinite() },
            bearingDegrees = bearing.toDouble().takeIf { hasBearing() && it.isFinite() && it in 0.0..<360.0 },
            horizontalErrorMeters = accuracy,
            monotonicMillis = elapsedRealtimeNanos.takeIf { it > 0L }?.div(1_000_000L) ?: now,
        )
    }

    private fun LocationSample.isUsable(now: Long): Boolean =
        latitude.isFinite() && latitude in -90.0..90.0 &&
            longitude.isFinite() && longitude in -180.0..180.0 &&
            horizontalErrorMeters <= GPS_READY_ERROR_METRES &&
            (now - monotonicMillis).coerceAtLeast(0) <= GPS_READY_AGE_MILLIS

    private fun reportCandidate(sample: LocationSample, source: GpsSource, now: Long) {
        if (hasUsableSelectedFix(now)) return
        val age = (now - sample.monotonicMillis).coerceAtLeast(0)
        repository.updateState { old ->
            old.copy(
                metrics = old.metrics.copy(
                    gpsReady = false,
                    gpsStatus = GpsStatus.ACQUIRED,
                    gpsSource = source,
                    gpsHorizontalErrorMetres = sample.horizontalErrorMeters,
                    gpsFixAgeMillis = age,
                    gpsStale = age > GPS_STALE_MILLIS,
                ),
            )
        }
    }

    private fun hasUsableSelectedFix(now: Long): Boolean =
        lastLocationFixMonotonic?.let { now - it <= GPS_STALE_MILLIS } == true &&
            lastLocationAccuracyMetres?.let { it <= GPS_READY_ERROR_METRES } == true

    private fun acceptLocationSample(
        sample: LocationSample,
        active: Boolean,
        decision: LocationSourceSelector.Decision,
    ) {
        val acceptance = if (active) paceEstimator.add(sample, active = true) else null
        if (active && acceptance?.accepted != true) {
            locationSourceSelector.reject(decision)
            return
        }

        lastLocationFixMonotonic = sample.monotonicMillis
        lastLocationAccuracyMetres = sample.horizontalErrorMeters
        val age = (SystemMonotonicClock.nowMillis() - sample.monotonicMillis).coerceAtLeast(0)
        repository.updateState { old ->
            old.copy(
                metrics = old.metrics.copy(
                    gpsReady = sample.horizontalErrorMeters <= GPS_READY_ERROR_METRES && age <= GPS_READY_AGE_MILLIS,
                    gpsStatus = GpsStatus.ACQUIRED,
                    gpsSource = decision.source,
                    gpsHorizontalErrorMetres = sample.horizontalErrorMeters,
                    gpsFixAgeMillis = age,
                    gpsStale = age > GPS_STALE_MILLIS,
                ),
            )
        }

        if (!active || acceptance == null) return
        if (acceptance.startsNewSegment) {
            continuitySegment++
            previousAcceptedLocation = null
        }
        previousAcceptedLocation?.let {
            routeFallbackDistance += PaceEstimator.greatCircleDistanceMetres(it, sample)
        }
        previousAcceptedLocation = sample
    }

    private suspend fun createSession(update: ExerciseUpdate) {
        val nowMonotonic = SystemMonotonicClock.nowMillis()
        sessionStartMonotonicMillis = update.startTime?.toEpochMilli()?.let { startEpoch ->
            nowMonotonic - (System.currentTimeMillis() - startEpoch).coerceAtLeast(0)
        } ?: nowMonotonic
    }

    private fun openPause() {
        if (pauseOpen) return
        pauseOpen = true
        repository.updateState { it.copy(pauseCount = it.pauseCount + 1) }
    }

    private fun closePause() {
        pauseOpen = false
    }

    private fun finalizeSession() {
        pauseOpen = false
        gnssLocationGateway.stop()
        fusedLocationGateway.stop()
    }

    private suspend fun runTicker() {
        while (true) {
            delay(1_000)
                val state = repository.state.value
                val now = SystemMonotonicClock.nowMillis()
            if (state.phase == WorkoutPhase.Preparing) {
                val started = state.preparationStartedAtMillis ?: now
                val elapsed = (now - started).coerceAtLeast(0)
                repository.updateState {
                    it.copy(
                        canStartWithoutFix = elapsed >= START_ANYWAY_DELAY_MILLIS,
                        startWithoutFixRemainingSeconds =
                            ((START_ANYWAY_DELAY_MILLIS - elapsed).coerceAtLeast(0) + 999L).div(1_000L).toInt(),
                    )
                }
            }
            if (state.hasActiveSession) {
                val heartRateStale = lastHeartRate?.let { now - it.monotonicMillis > HR_STALE_MILLIS } ?: true
                val active = state.phase == WorkoutPhase.Active
                val activeDuration = currentActiveDuration(active)
                val elapsed = sessionStartMonotonicMillis?.let {
                    Duration.ofMillis((now - it).coerceAtLeast(0))
                } ?: state.metrics.elapsedDuration
                repository.updateState { old ->
                    val fixAge = lastLocationFixMonotonic?.let { (now - it).coerceAtLeast(0) }
                    val gpsReady = fixAge != null && fixAge <= GPS_READY_AGE_MILLIS &&
                        lastLocationAccuracyMetres?.let { it <= GPS_READY_ERROR_METRES } == true
                    old.copy(
                        metrics = old.metrics.copy(
                            activeDuration = activeDuration,
                            elapsedDuration = elapsed,
                            pace = if (active) paceEstimator.estimate(now) else old.metrics.pace,
                            heartRateBpm = if (heartRateStale) null else old.metrics.heartRateBpm,
                            heartRateStale = heartRateStale,
                            gpsReady = gpsReady,
                            gpsFixAgeMillis = fixAge,
                            gpsStale = fixAge == null || fixAge > GPS_STALE_MILLIS,
                        ),
                        zoneTimeMillis = zoneAccumulator?.zoneMillis() ?: old.zoneTimeMillis,
                        unclassifiedHeartRateMillis = zoneAccumulator?.unclassifiedMillis()
                            ?: old.unclassifiedHeartRateMillis,
                    )
                }
                if (now - lastNotificationAt >= NOTIFICATION_UPDATE_MILLIS) {
                    notifier.notify(repository.state.value)
                    lastNotificationAt = now
                }
            }
        }
    }

    private fun authoritativeDistance(): Double = routeFallbackDistance

    private fun currentActiveDuration(active: Boolean): Duration {
        val additional = if (active) {
            (SystemMonotonicClock.nowMillis() - activeCheckpointMonotonic).coerceAtLeast(0)
        } else 0
        return Duration.ofMillis(activeCheckpointMillis + additional)
    }

    private fun hasHeartRatePermission(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= 36) READ_HEART_RATE else Manifest.permission.BODY_SENSORS
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun stopSessionService(delayMillis: Long = 0) {
        scope.launch {
            if (delayMillis > 0) delay(delayMillis)
            notifier.cancel()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun ExerciseState.toWorkoutPhase(): WorkoutPhase = when (this) {
        ExerciseState.PREPARING -> WorkoutPhase.Preparing
        ExerciseState.USER_STARTING -> WorkoutPhase.Starting
        ExerciseState.ACTIVE -> WorkoutPhase.Active
        ExerciseState.USER_PAUSING, ExerciseState.AUTO_PAUSING -> WorkoutPhase.Pausing
        ExerciseState.USER_PAUSED, ExerciseState.AUTO_PAUSED -> WorkoutPhase.Paused
        ExerciseState.USER_RESUMING, ExerciseState.AUTO_RESUMING -> WorkoutPhase.Resuming
        ExerciseState.ENDING -> WorkoutPhase.Ending
        ExerciseState.ENDED -> WorkoutPhase.Ended
        else -> WorkoutPhase.Error
    }

    companion object {
        private const val TAG = "WatchRunning"
        private const val EXTRA_COMMAND = "command"
        private const val READ_HEART_RATE = "android.permission.health.READ_HEART_RATE"
        private const val COMMAND_DEBOUNCE_MILLIS = 750L
        private const val START_ANYWAY_DELAY_MILLIS = 60_000L
        private const val GPS_READY_ERROR_METRES = 25.0
        private const val GPS_READY_AGE_MILLIS = 5_000L
        private const val GPS_STALE_MILLIS = 5_000L
        private const val HR_STALE_MILLIS = 10_000L
        private const val NOTIFICATION_UPDATE_MILLIS = 15_000L

        fun submit(context: Context, command: WorkoutCommand) {
            val intent = Intent(context, ExerciseSessionService::class.java)
                .putExtra(EXTRA_COMMAND, commandName(command))
            ContextCompat.startForegroundService(context, intent)
        }

        private fun commandName(command: WorkoutCommand): String = when (command) {
            WorkoutCommand.Prepare -> "prepare"
            WorkoutCommand.Start -> "start"
            WorkoutCommand.StartWithoutFix -> "start_without_fix"
            WorkoutCommand.Pause -> "pause"
            WorkoutCommand.Resume -> "resume"
            WorkoutCommand.RequestEnd -> "request_end"
            WorkoutCommand.ConfirmEnd -> "confirm_end"
            WorkoutCommand.CancelPrepare -> "cancel_prepare"
        }

        private fun commandFromName(value: String): WorkoutCommand? = when (value) {
            "prepare" -> WorkoutCommand.Prepare
            "start" -> WorkoutCommand.Start
            "start_without_fix" -> WorkoutCommand.StartWithoutFix
            "pause" -> WorkoutCommand.Pause
            "resume" -> WorkoutCommand.Resume
            "request_end" -> WorkoutCommand.RequestEnd
            "confirm_end" -> WorkoutCommand.ConfirmEnd
            "cancel_prepare" -> WorkoutCommand.CancelPrepare
            else -> null
        }
    }
}
