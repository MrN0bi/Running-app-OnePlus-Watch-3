package com.watchrunning.app

import android.app.Application
import com.watchrunning.app.data.settings.SettingsRepository
import com.watchrunning.app.exercise.CapabilityProbe
import com.watchrunning.app.exercise.WorkoutRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WatchRunningApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val settingsRepository by lazy { SettingsRepository(this) }
    val workoutRepository by lazy { WorkoutRepository() }

    override fun onCreate() {
        super.onCreate()
        val probe = CapabilityProbe(this)
        workoutRepository.updateCapabilities(probe.deviceOnly())
        applicationScope.launch {
            runCatching { probe.read() }.onSuccess(workoutRepository::updateCapabilities)
        }
    }
}
