package com.culture.tracker.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.culture.tracker.data.repository.CalendarRepository
import com.culture.tracker.data.repository.GardenRepository
import com.culture.tracker.data.repository.SettingsRepository
import com.culture.tracker.domain.model.ActionType
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ActionReminderWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params), KoinComponent {

    private val gardenRepository: GardenRepository by inject()
    private val calendarRepository: CalendarRepository by inject()
    private val notificationHelper: NotificationHelper by inject()
    private val settingsRepository: SettingsRepository by inject()

    override suspend fun doWork(): Result {
        if (!settingsRepository.settings.first().notificationsEnabled) return Result.success()

        notificationHelper.ensureChannel()
        val today = LocalDate.now()

        val plantsNeedingWater = mutableListOf<String>()
        for (plant in gardenRepository.plantsWithWateringSchedule()) {
            val interval = plant.wateringIntervalDays ?: continue
            val last = calendarRepository.lastActionDate(plant.id, ActionType.ARROSAGE) ?: plant.startDate
            if (!last.plusDays(interval.toLong()).isAfter(today)) {
                plantsNeedingWater += plant.name
            }
        }

        val plantsNeedingFertilizer = mutableListOf<String>()
        for (plant in gardenRepository.plantsWithFertilizingSchedule()) {
            val interval = plant.fertilizingIntervalDays ?: continue
            val last = calendarRepository.lastActionDate(plant.id, ActionType.ENGRAIS) ?: plant.startDate
            if (!last.plusDays(interval.toLong()).isAfter(today)) {
                plantsNeedingFertilizer += plant.name
            }
        }

        notificationHelper.notifyWateringDue(plantsNeedingWater)
        notificationHelper.notifyFertilizingDue(plantsNeedingFertilizer)
        return Result.success()
    }
}
