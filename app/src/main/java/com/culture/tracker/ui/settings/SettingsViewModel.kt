package com.culture.tracker.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.culture.tracker.data.repository.AppSettings
import com.culture.tracker.data.repository.SettingsRepository
import com.culture.tracker.data.repository.ThemeMode
import com.culture.tracker.notifications.ReminderScheduler
import com.culture.tracker.sync.SensorPollScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository,
    private val appContext: Context,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    // `settings` démarre toujours avec une valeur par défaut synthétique le temps que DataStore
    // livre la vraie première valeur ; ce flag permet de distinguer "pas encore chargé" d'un
    // vrai userName vide, pour ne pré-remplir les champs texte qu'une fois la vraie valeur connue.
    val isLoaded: StateFlow<Boolean> = repository.settings
        .map { true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setNotificationsEnabled(enabled)
            if (enabled) {
                ReminderScheduler.schedule(appContext, settings.value.reminderHour)
            } else {
                ReminderScheduler.cancel(appContext)
            }
        }
    }

    fun setReminderHour(hour: Int) {
        viewModelScope.launch {
            repository.setReminderHour(hour)
            if (settings.value.notificationsEnabled) {
                ReminderScheduler.schedule(appContext, hour)
            }
        }
    }

    fun setUserName(name: String?) {
        viewModelScope.launch { repository.setUserName(name) }
    }

    fun setNetworkSensorsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setNetworkSensorsEnabled(enabled)
            if (enabled) {
                SensorPollScheduler.schedule(appContext)
            } else {
                SensorPollScheduler.cancel(appContext)
            }
        }
    }

    fun completeOnboarding(name: String?) {
        viewModelScope.launch {
            if (!name.isNullOrBlank()) repository.setUserName(name)
            repository.setOnboardingCompleted(true)
        }
    }
}
