package com.watchrunning.app.model

import java.time.Duration
import java.util.UUID

enum class WorkoutPhase {
    Idle,
    Preparing,
    Starting,
    Active,
    Pausing,
    Paused,
    Resuming,
    Ending,
    Ended,
    Interrupted,
    Error,
}

sealed interface WorkoutCommand {
    data object Prepare : WorkoutCommand
    data object Start : WorkoutCommand
    data object StartWithoutFix : WorkoutCommand
    data object Pause : WorkoutCommand
    data object Resume : WorkoutCommand
    data object RequestEnd : WorkoutCommand
    data object ConfirmEnd : WorkoutCommand
    data object CancelPrepare : WorkoutCommand
}

data class LocationSample(
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double?,
    val bearingDegrees: Double?,
    val horizontalErrorMeters: Double,
    val monotonicMillis: Long,
)

data class HeartRateSample(
    val beatsPerMinute: Double,
    val monotonicMillis: Long,
    val accurate: Boolean,
)

enum class PaceUnavailableReason {
    INSUFFICIENT_SAMPLES,
    POOR_COVERAGE,
    STALE,
    STATIONARY,
    INVALID,
}

data class PaceEstimate(
    val secondsPerKilometre: Double?,
    val speedMetresPerSecond: Double?,
    val coverageMillis: Long,
    val calculatedAtMillis: Long,
    val stale: Boolean = false,
    val unavailableReason: PaceUnavailableReason? = null,
) {
    val isAvailable: Boolean get() = secondsPerKilometre != null
}

enum class GpsStatus {
    UNKNOWN,
    ACQUIRING,
    ACQUIRED,
    UNAVAILABLE,
    NO_GNSS,
}

enum class GpsSource {
    NONE,
    WATCH_GNSS,
    FUSED,
    HEALTH_SERVICES,
    HEALTH_SERVICES_WATCH,
    HEALTH_SERVICES_PHONE,
}

data class LiveMetrics(
    val heartRateBpm: Int? = null,
    val averageHeartRateBpm: Double? = null,
    val maximumHeartRateBpm: Double? = null,
    val pace: PaceEstimate? = null,
    val distanceMetres: Double = 0.0,
    val activeDuration: Duration = Duration.ZERO,
    val elapsedDuration: Duration = Duration.ZERO,
    val averagePaceSecondsPerKm: Double? = null,
    val gpsReady: Boolean = false,
    val gpsStatus: GpsStatus = GpsStatus.UNKNOWN,
    val gpsSource: GpsSource = GpsSource.NONE,
    val gpsHorizontalErrorMetres: Double? = null,
    val gpsFixAgeMillis: Long? = null,
    val heartRateReady: Boolean = false,
    val gpsStale: Boolean = false,
    val heartRateStale: Boolean = false,
)

data class WorkoutUiState(
    val phase: WorkoutPhase = WorkoutPhase.Idle,
    val sessionId: String? = null,
    val metrics: LiveMetrics = LiveMetrics(),
    val pendingCommand: WorkoutCommand? = null,
    val preparationStartedAtMillis: Long? = null,
    val warning: String? = null,
    val error: String? = null,
    val canStartWithoutFix: Boolean = false,
    val startWithoutFixRemainingSeconds: Int = 60,
    val pauseCount: Int = 0,
) {
    val hasActiveSession: Boolean
        get() = phase in setOf(
            WorkoutPhase.Starting,
            WorkoutPhase.Active,
            WorkoutPhase.Pausing,
            WorkoutPhase.Paused,
            WorkoutPhase.Resuming,
            WorkoutPhase.Ending,
        )

    companion object {
        fun newSession(): WorkoutUiState = WorkoutUiState(sessionId = UUID.randomUUID().toString())
    }
}

data class ExerciseCapabilitiesSnapshot(
    val runningSupported: Boolean = false,
    val heartRateSupported: Boolean = false,
    val locationSupported: Boolean = false,
    val distanceSupported: Boolean = false,
    val speedSupported: Boolean = false,
    val paceSupported: Boolean = false,
    val heartRateStatsSupported: Boolean = false,
    val paceStatsSupported: Boolean = false,
    val autoPauseSupported: Boolean = false,
    val apiLevel: Int = 0,
    val release: String = "",
    val manufacturer: String = "",
    val model: String = "",
    val buttonCount: Int = 0,
)

data class WorkoutSummary(
    val sessionId: String,
    val startEpochMillis: Long,
    val endEpochMillis: Long?,
    val elapsedMillis: Long,
    val activeMillis: Long,
    val distanceMetres: Double,
    val averagePaceMillisPerKm: Long?,
    val averageHeartRate: Double?,
    val maximumHeartRate: Double?,
    val interrupted: Boolean,
    val endReason: Int?,
)
