package com.culture.tracker.data.repository

import com.culture.tracker.data.local.dao.CalendarActionDao
import com.culture.tracker.data.local.dao.EnvironmentReadingDao
import com.culture.tracker.data.local.dao.FertilizerDao
import com.culture.tracker.data.local.entity.CalendarAction
import com.culture.tracker.data.local.entity.EnvironmentReading
import com.culture.tracker.data.local.entity.Fertilizer
import com.culture.tracker.domain.model.ActionType
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

class CalendarRepository(
    private val calendarActionDao: CalendarActionDao,
    private val fertilizerDao: FertilizerDao,
    private val environmentReadingDao: EnvironmentReadingDao,
) {
    fun observeActionsBetween(start: LocalDate, end: LocalDate): Flow<List<CalendarAction>> =
        calendarActionDao.observeBetween(start, end)

    fun observeActionsForPlant(plantId: Long): Flow<List<CalendarAction>> =
        calendarActionDao.observeForPlant(plantId)

    fun observeFertilizers(): Flow<List<Fertilizer>> = fertilizerDao.observeAll()

    fun observeReadings(environmentId: Long): Flow<List<EnvironmentReading>> =
        environmentReadingDao.observeForEnvironment(environmentId)

    fun observeLatestReadings(): Flow<List<EnvironmentReading>> =
        environmentReadingDao.observeLatestPerEnvironment()

    suspend fun addAction(action: CalendarAction): Long = calendarActionDao.upsert(action)
    suspend fun updateAction(action: CalendarAction) = calendarActionDao.update(action)
    suspend fun deleteAction(action: CalendarAction) = calendarActionDao.delete(action)

    suspend fun lastActionDate(plantId: Long, actionType: ActionType): LocalDate? =
        calendarActionDao.getLastActionOfType(plantId, actionType)?.date

    suspend fun createFertilizer(fertilizer: Fertilizer): Long = fertilizerDao.upsert(fertilizer)
    suspend fun updateFertilizer(fertilizer: Fertilizer) = fertilizerDao.update(fertilizer)
    suspend fun deleteFertilizer(fertilizer: Fertilizer) = fertilizerDao.delete(fertilizer)

    suspend fun recordReading(reading: EnvironmentReading): Long = environmentReadingDao.insert(reading)
}
