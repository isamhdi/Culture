package com.culture.tracker

import android.app.Application
import com.culture.tracker.di.appModule
import com.culture.tracker.notifications.NotificationHelper
import com.culture.tracker.notifications.ReminderScheduler
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
    }
}
