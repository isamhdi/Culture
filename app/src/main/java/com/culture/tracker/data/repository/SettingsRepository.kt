package com.culture.tracker.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val reminderHour: Int = 8,
    val userName: String? = null,
    val networkSensorsEnabled: Boolean = false,
    val hasCompletedOnboarding: Boolean = false,
)

class SettingsRepository(private val dataStore: DataStore<Preferences>) {
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val USER_NAME = stringPreferencesKey("user_name")
        val NETWORK_SENSORS_ENABLED = booleanPreferencesKey("network_sensors_enabled")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    val settings: Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            themeMode = prefs[Keys.THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
            reminderHour = prefs[Keys.REMINDER_HOUR] ?: 8,
            userName = prefs[Keys.USER_NAME]?.takeIf { it.isNotBlank() },
            networkSensorsEnabled = prefs[Keys.NETWORK_SENSORS_ENABLED] ?: false,
            hasCompletedOnboarding = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setReminderHour(hour: Int) {
        dataStore.edit { it[Keys.REMINDER_HOUR] = hour }
    }

    suspend fun setUserName(name: String?) {
        dataStore.edit {
            if (name.isNullOrBlank()) it.remove(Keys.USER_NAME) else it[Keys.USER_NAME] = name
        }
    }

    suspend fun setNetworkSensorsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.NETWORK_SENSORS_ENABLED] = enabled }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }
}
