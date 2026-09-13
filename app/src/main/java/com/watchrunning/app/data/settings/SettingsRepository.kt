package com.watchrunning.app.data.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "running_settings")

data class RunningSettings(
    val paceWindowSeconds: Int = 5,
    val autoPauseEnabled: Boolean = false,
)

class SettingsRepository(private val context: Context) {
    private object Keys {
        val paceWindowSeconds = intPreferencesKey("pace_window_seconds")
        val autoPauseEnabled = booleanPreferencesKey("auto_pause_enabled")
    }

    val settings: Flow<RunningSettings> = context.settingsDataStore.data.map(::mapSettings)

    suspend fun setPaceWindowSeconds(seconds: Int) {
        require(seconds in setOf(3, 5, 10))
        context.settingsDataStore.edit { it[Keys.paceWindowSeconds] = seconds }
    }

    suspend fun setAutoPauseEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.autoPauseEnabled] = enabled }
    }

    private fun mapSettings(preferences: Preferences): RunningSettings {
        val window = preferences[Keys.paceWindowSeconds]?.takeIf { it in setOf(3, 5, 10) } ?: 5
        return RunningSettings(
            paceWindowSeconds = window,
            autoPauseEnabled = preferences[Keys.autoPauseEnabled] ?: false,
        )
    }
}
