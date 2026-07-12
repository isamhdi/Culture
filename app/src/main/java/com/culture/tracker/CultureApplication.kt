package com.culture.tracker

import android.app.Application
import com.culture.tracker.data.repository.GardenRepository
import com.culture.tracker.data.repository.SettingsRepository
import com.culture.tracker.di.appModule
import com.culture.tracker.domain.predefinedGenetics
import com.culture.tracker.notifications.NotificationHelper
import com.culture.tracker.notifications.ReminderScheduler
import com.culture.tracker.sync.SensorPollScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class CultureApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@CultureApplication)
            modules(appModule)
        }

        get<NotificationHelper>().ensureChannel()

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            get<GardenRepository>().seedDefaultGeneticsIfEmpty(predefinedGenetics)

            val settingsRepository = get<SettingsRepository>()
            val settings = settingsRepository.settings.first()
            if (settings.notificationsEnabled) {
                ReminderScheduler.schedule(this@CultureApplication, settings.reminderHour)
            }
            if (settings.networkSensorsEnabled) {
                SensorPollScheduler.schedule(this@CultureApplication)
            }
            // Une installation qui a déjà des données (mise à jour depuis une version sans onboarding,
            // ou restauration d'une sauvegarde) ne doit jamais revoir l'écran de bienvenue.
            if (!settings.hasCompletedOnboarding &&
                (settings.userName != null || get<GardenRepository>().hasAnyPlantOrEnvironment())
            ) {
                settingsRepository.setOnboardingCompleted(true)
            }
        }
    }
}
