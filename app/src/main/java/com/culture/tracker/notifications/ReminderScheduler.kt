package com.culture.tracker.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

object ReminderScheduler {
    private const val WORK_NAME = "action_reminder_daily"

    fun schedule(context: Context, reminderHour: Int) {
        val now = LocalDateTime.now()
        var nextRun = now.toLocalDate().atTime(LocalTime.of(reminderHour, 0))
        if (nextRun.isBefore(now)) {
            nextRun = nextRun.plusDays(1)
        }
        val initialDelay = Duration.between(now, nextRun)

        val request = PeriodicWorkRequestBuilder<ActionReminderWorker>(Duration.ofDays(1))
            .setInitialDelay(initialDelay)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
