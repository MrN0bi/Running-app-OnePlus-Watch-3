package com.watchrunning.app.exercise

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.health.services.client.ExerciseClient
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.HealthServices
import androidx.health.services.client.clearUpdateCallback
import androidx.health.services.client.endExercise
import androidx.health.services.client.getCapabilities
import androidx.health.services.client.getCurrentExerciseInfo
import androidx.health.services.client.pauseExercise
import androidx.health.services.client.prepareExercise
import androidx.health.services.client.resumeExercise
import androidx.health.services.client.startExercise
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseCapabilities
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseInfo
import androidx.health.services.client.data.ExerciseLapSummary
import androidx.health.services.client.data.ExerciseTrackedStatus
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.data.ExerciseUpdate
import androidx.health.services.client.data.WarmUpConfig
import com.watchrunning.app.model.ExerciseCapabilitiesSnapshot
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed interface ExerciseOwnership {
    data object None : ExerciseOwnership
    data object Ours : ExerciseOwnership
    data object OtherApp : ExerciseOwnership
}

sealed interface GatewayEvent {
    data class Update(val value: ExerciseUpdate) : GatewayEvent
    data class AvailabilityChanged(
        val dataType: DataType<*, *>,
        val availability: Availability,
    ) : GatewayEvent
    data class CallbackFailure(val throwable: Throwable) : GatewayEvent
}

interface ExerciseGateway {
    val events: Flow<GatewayEvent>
    suspend fun capabilities(): ExerciseCapabilitiesSnapshot
    suspend fun currentExercise(): ExerciseOwnership
    suspend fun prepare(includeHeartRate: Boolean)
    suspend fun start(includeHeartRate: Boolean)
    suspend fun pause()
    suspend fun resume()
    suspend fun end()
    suspend fun close()
}

class HealthServicesExerciseGateway(context: Context) : ExerciseGateway {
    private val exerciseClient: ExerciseClient =
        HealthServices.getClient(context.applicationContext).exerciseClient
    private var rawCapabilities: ExerciseCapabilities? = null
    private var runningDataTypes: Set<DataType<*, *>> = emptySet()

    private val mutableEvents = MutableSharedFlow<GatewayEvent>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    override val events: Flow<GatewayEvent> = mutableEvents.asSharedFlow()

    private val callback = object : ExerciseUpdateCallback {
        override fun onRegistered() = Unit

        override fun onRegistrationFailed(throwable: Throwable) {
            mutableEvents.tryEmit(GatewayEvent.CallbackFailure(throwable))
        }

        override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
            mutableEvents.tryEmit(GatewayEvent.Update(update))
        }

        override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) = Unit

        override fun onAvailabilityChanged(
            dataType: DataType<*, *>,
            availability: Availability,
        ) {
            mutableEvents.tryEmit(GatewayEvent.AvailabilityChanged(dataType, availability))
        }
    }

    init {
        exerciseClient.setUpdateCallback(callback)
    }

    override suspend fun capabilities(): ExerciseCapabilitiesSnapshot {
        val capabilities = exerciseClient.getCapabilities().also {
            rawCapabilities = it
        }
        val runningSupported = ExerciseType.RUNNING in capabilities.supportedExerciseTypes
        val supported = if (runningSupported) {
            capabilities.getExerciseTypeCapabilities(ExerciseType.RUNNING)
        } else {
            null
        }
        runningDataTypes = supported?.supportedDataTypes.orEmpty()
        return ExerciseCapabilitiesSnapshot(
            runningSupported = runningSupported,
            heartRateSupported = DataType.HEART_RATE_BPM in runningDataTypes,
            locationSupported = DataType.LOCATION in runningDataTypes,
            distanceSupported = DataType.DISTANCE_TOTAL in runningDataTypes,
            speedSupported = DataType.SPEED in runningDataTypes,
            paceSupported = DataType.PACE in runningDataTypes,
            heartRateStatsSupported = DataType.HEART_RATE_BPM_STATS in runningDataTypes,
            paceStatsSupported = DataType.PACE_STATS in runningDataTypes,
            autoPauseSupported = supported?.supportsAutoPauseAndResume == true,
            apiLevel = Build.VERSION.SDK_INT,
            release = Build.VERSION.RELEASE,
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
        )
    }

    @SuppressLint("RestrictedApi")
    override suspend fun currentExercise(): ExerciseOwnership {
        val info: ExerciseInfo = exerciseClient.getCurrentExerciseInfo()
        return when (info.exerciseTrackedStatus) {
            ExerciseTrackedStatus.OWNED_EXERCISE_IN_PROGRESS -> ExerciseOwnership.Ours
            ExerciseTrackedStatus.OTHER_APP_IN_PROGRESS -> ExerciseOwnership.OtherApp
            else -> ExerciseOwnership.None
        }
    }

    override suspend fun prepare(includeHeartRate: Boolean) {
        ensureCapabilities()
        val warmUpTypes = buildSet {
            if (DataType.LOCATION in runningDataTypes) {
                add(DataType.LOCATION)
            }
            if (includeHeartRate && DataType.HEART_RATE_BPM in runningDataTypes) {
                add(DataType.HEART_RATE_BPM)
            }
        }
        if (warmUpTypes.isEmpty()) return
        exerciseClient.prepareExercise(
            WarmUpConfig(ExerciseType.RUNNING, warmUpTypes),
        )
    }

    override suspend fun start(includeHeartRate: Boolean) {
        ensureCapabilities()
        val requested = buildSet {
            fun addIfSupported(type: DataType<*, *>) {
                if (type in runningDataTypes) add(type)
            }
            addIfSupported(DataType.LOCATION)
            addIfSupported(DataType.DISTANCE_TOTAL)
            addIfSupported(DataType.SPEED)
            addIfSupported(DataType.PACE)
            addIfSupported(DataType.PACE_STATS)
            if (includeHeartRate) {
                addIfSupported(DataType.HEART_RATE_BPM)
                addIfSupported(DataType.HEART_RATE_BPM_STATS)
            }
        }
        val config = ExerciseConfig.builder(ExerciseType.RUNNING)
            .setDataTypes(requested)
            .setIsGpsEnabled(DataType.LOCATION in requested)
            .setIsAutoPauseAndResumeEnabled(false)
            .build()
        exerciseClient.startExercise(config)
    }

    override suspend fun pause() {
        exerciseClient.pauseExercise()
    }

    override suspend fun resume() {
        exerciseClient.resumeExercise()
    }

    override suspend fun end() {
        exerciseClient.endExercise()
    }

    override suspend fun close() {
        exerciseClient.clearUpdateCallback(callback)
    }

    private suspend fun ensureCapabilities() {
        if (rawCapabilities == null) capabilities()
    }
}
