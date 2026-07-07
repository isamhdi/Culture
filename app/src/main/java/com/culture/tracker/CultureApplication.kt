package com.culture.tracker

import android.app.Application
import com.culture.tracker.data.repository.GardenRepository
import com.culture.tracker.di.appModule
import com.culture.tracker.domain.predefinedGenetics
import com.culture.tracker.notifications.NotificationHelper
import com.culture.tracker.notifications.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
        ReminderScheduler.schedule(this, reminderHour = 8)

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            get<GardenRepository>().seedDefaultGeneticsIfEmpty(predefinedGenetics)
        }
    }
}
