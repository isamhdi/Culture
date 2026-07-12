package com.culture.tracker.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.culture.tracker.data.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class BootCompletedReceiver : BroadcastReceiver(), KoinComponent {
    private val settingsRepository: SettingsRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val appContext = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            val settings = settingsRepository.settings.first()
            if (settings.notificationsEnabled) {
                ReminderScheduler.schedule(appContext, settings.reminderHour)
            }
        }
    }
}
