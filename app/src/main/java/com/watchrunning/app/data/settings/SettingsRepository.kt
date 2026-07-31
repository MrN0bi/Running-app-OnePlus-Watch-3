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

enum class MaximumHeartRateMode { AGE_ESTIMATE, MANUAL }

data class RunningSettings(
    val age: Int? = null,
    val maximumHeartRateMode: MaximumHeartRateMode = MaximumHeartRateMode.AGE_ESTIMATE,
    val manualMaximumHeartRate: Int? = null,
    val paceWindowSeconds: Int = 5,
    val autoPauseEnabled: Boolean = false,
) {
    val effectiveMaximumHeartRate: Int?
        get() = when (maximumHeartRateMode) {
            MaximumHeartRateMode.MANUAL -> manualMaximumHeartRate
            MaximumHeartRateMode.AGE_ESTIMATE -> age?.let { 220 - it }
        }
}

class SettingsRepository(private val context: Context) {
    private object Keys {
        val age = intPreferencesKey("age")
        val maximumHeartRateMode = intPreferencesKey("maximum_heart_rate_mode")
        val manualMaximumHeartRate = intPreferencesKey("manual_maximum_heart_rate")
        val paceWindowSeconds = intPreferencesKey("pace_window_seconds")
        val autoPauseEnabled = booleanPreferencesKey("auto_pause_enabled")
    }

    val settings: Flow<RunningSettings> = context.settingsDataStore.data.map(::mapSettings)

    suspend fun setAge(age: Int?) {
        require(age == null || age in 13..100)
        context.settingsDataStore.edit { preferences ->
            if (age == null) preferences.remove(Keys.age) else preferences[Keys.age] = age
        }
    }

    suspend fun setMaximumHeartRateMode(mode: MaximumHeartRateMode) {
        context.settingsDataStore.edit { it[Keys.maximumHeartRateMode] = mode.ordinal }
    }

    suspend fun setManualMaximumHeartRate(value: Int?) {
        require(value == null || value in 100..240)
        context.settingsDataStore.edit { preferences ->
            if (value == null) preferences.remove(Keys.manualMaximumHeartRate)
            else preferences[Keys.manualMaximumHeartRate] = value
        }
    }

    suspend fun setPaceWindowSeconds(seconds: Int) {
        require(seconds in setOf(3, 5, 10))
        context.settingsDataStore.edit { it[Keys.paceWindowSeconds] = seconds }
    }

    suspend fun setAutoPauseEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.autoPauseEnabled] = enabled }
    }

    private fun mapSettings(preferences: Preferences): RunningSettings {
        val mode = MaximumHeartRateMode.entries.getOrElse(preferences[Keys.maximumHeartRateMode] ?: 0) {
            MaximumHeartRateMode.AGE_ESTIMATE
        }
        val window = preferences[Keys.paceWindowSeconds]?.takeIf { it in setOf(3, 5, 10) } ?: 5
        return RunningSettings(
            age = preferences[Keys.age]?.takeIf { it in 13..100 },
            maximumHeartRateMode = mode,
            manualMaximumHeartRate = preferences[Keys.manualMaximumHeartRate]?.takeIf { it in 100..240 },
            paceWindowSeconds = window,
            autoPauseEnabled = preferences[Keys.autoPauseEnabled] ?: false,
        )
    }
}
