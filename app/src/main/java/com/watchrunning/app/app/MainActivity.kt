package com.watchrunning.app.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.watchrunning.app.WatchRunningApplication
import com.watchrunning.app.exercise.ExerciseSessionService
import com.watchrunning.app.model.WorkoutCommand
import com.watchrunning.app.model.WorkoutPhase
import com.watchrunning.app.ui.RunningApp

class MainActivity : ComponentActivity() {
    private val app: WatchRunningApplication get() = application as WatchRunningApplication
    private var permissionMessage by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.BLACK
        window.navigationBarColor = android.graphics.Color.BLACK
        setContent {
            val state by app.workoutRepository.state.collectAsStateWithLifecycle()
            val settings by app.settingsRepository.settings.collectAsStateWithLifecycle(initialValue = null)
            val capabilities by app.workoutRepository.capabilities.collectAsStateWithLifecycle()
            val backgroundPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission(),
            ) { submit(WorkoutCommand.Prepare) }
            val permissionsLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions(),
            ) {
                if (!hasCorePermissions()) {
                    permissionMessage = "Precise location and activity recognition are required for a GPS run."
                    return@rememberLauncherForActivityResult
                }
                if (hasHeartRatePermission() && backgroundHeartRatePermission() != null &&
                    !hasBackgroundHeartRatePermission()
                ) {
                    backgroundPermissionLauncher.launch(requireNotNull(backgroundHeartRatePermission()))
                } else {
                    submit(WorkoutCommand.Prepare)
                }
            }

            RunningApp(
                state = state,
                settings = settings,
                capabilities = capabilities,
                permissionMessage = permissionMessage,
                onDismissMessage = { permissionMessage = null },
                onDismissSummary = { app.workoutRepository.replaceState(com.watchrunning.app.model.WorkoutUiState()) },
                onStartRun = {
                    if (!isLocationEnabled()) {
                        permissionMessage = "Turn on watch location services before starting."
                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    } else if (hasCorePermissions()) {
                        submit(WorkoutCommand.Prepare)
                    } else {
                        permissionsLauncher.launch(requestedPermissions())
                    }
                },
                onCommand = ::submit,
                settingsRepository = app.settingsRepository,
            )
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.repeatCount == 0 && keyCode in setOf(
                KeyEvent.KEYCODE_STEM_1,
                KeyEvent.KEYCODE_STEM_2,
                KeyEvent.KEYCODE_STEM_3,
            )
        ) {
            when (app.workoutRepository.state.value.phase) {
                WorkoutPhase.Active -> submit(WorkoutCommand.Pause)
                WorkoutPhase.Paused -> submit(WorkoutCommand.Resume)
                else -> return super.onKeyDown(keyCode, event)
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun submit(command: WorkoutCommand) {
        ExerciseSessionService.submit(this, command)
    }

    private fun requestedPermissions(): Array<String> = buildList {
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACTIVITY_RECOGNITION)
        add(if (Build.VERSION.SDK_INT >= 36) READ_HEART_RATE else Manifest.permission.BODY_SENSORS)
        if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
    }.toTypedArray()

    private fun hasCorePermissions(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED

    private fun hasHeartRatePermission(): Boolean = ContextCompat.checkSelfPermission(
        this,
        if (Build.VERSION.SDK_INT >= 36) READ_HEART_RATE else Manifest.permission.BODY_SENSORS,
    ) == PackageManager.PERMISSION_GRANTED

    private fun backgroundHeartRatePermission(): String? = when {
        Build.VERSION.SDK_INT >= 36 -> READ_HEALTH_DATA_IN_BACKGROUND
        Build.VERSION.SDK_INT >= 33 -> Manifest.permission.BODY_SENSORS_BACKGROUND
        else -> null
    }

    private fun hasBackgroundHeartRatePermission(): Boolean =
        backgroundHeartRatePermission()?.let {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        } ?: true

    private fun isLocationEnabled(): Boolean =
        getSystemService(LocationManager::class.java).isProviderEnabled(LocationManager.GPS_PROVIDER)

    companion object {
        private const val READ_HEART_RATE = "android.permission.health.READ_HEART_RATE"
        private const val READ_HEALTH_DATA_IN_BACKGROUND = "android.permission.health.READ_HEALTH_DATA_IN_BACKGROUND"
    }
}
