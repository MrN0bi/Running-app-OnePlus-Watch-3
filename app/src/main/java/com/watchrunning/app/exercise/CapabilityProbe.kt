package com.watchrunning.app.exercise

import android.content.Context
import android.os.Build
import androidx.health.services.client.HealthServices
import androidx.health.services.client.getCapabilities
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseType
import androidx.wear.input.WearableButtons
import com.watchrunning.app.model.ExerciseCapabilitiesSnapshot

class CapabilityProbe(private val context: Context) {
    suspend fun read(): ExerciseCapabilitiesSnapshot {
        val capabilities = HealthServices.getClient(context).exerciseClient.getCapabilities()
        val runningSupported = ExerciseType.RUNNING in capabilities.supportedExerciseTypes
        val running = capabilities.typeToCapabilities[ExerciseType.RUNNING]
        val types = running?.supportedDataTypes.orEmpty()
        return ExerciseCapabilitiesSnapshot(
            runningSupported = runningSupported,
            heartRateSupported = DataType.HEART_RATE_BPM in types,
            locationSupported = DataType.LOCATION in types,
            distanceSupported = DataType.DISTANCE_TOTAL in types,
            speedSupported = DataType.SPEED in types,
            paceSupported = DataType.PACE in types,
            heartRateStatsSupported = DataType.HEART_RATE_BPM_STATS in types,
            paceStatsSupported = DataType.PACE_STATS in types,
            autoPauseSupported = running?.supportsAutoPauseAndResume == true,
            apiLevel = Build.VERSION.SDK_INT,
            release = Build.VERSION.RELEASE,
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            buttonCount = WearableButtons.getButtonCount(context),
        )
    }

    fun deviceOnly(): ExerciseCapabilitiesSnapshot = ExerciseCapabilitiesSnapshot(
        apiLevel = Build.VERSION.SDK_INT,
        release = Build.VERSION.RELEASE,
        manufacturer = Build.MANUFACTURER,
        model = Build.MODEL,
        buttonCount = WearableButtons.getButtonCount(context),
    )
}
