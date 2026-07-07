package com.culture.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.culture.tracker.data.local.entity.CalendarAction
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarActionDao {
    @Query("SELECT * FROM calendar_actions WHERE date BETWEEN :start AND :end ORDER BY date ASC")
    fun observeBetween(start: LocalDate, end: LocalDate): Flow<List<CalendarAction>>

    @Query("SELECT * FROM calendar_actions WHERE plantId = :plantId ORDER BY date DESC")
    fun observeForPlant(plantId: Long): Flow<List<CalendarAction>>

    @Query("SELECT * FROM calendar_actions WHERE plantId = :plantId AND actionType = :actionType ORDER BY date DESC LIMIT 1")
    suspend fun getLastActionOfType(plantId: Long, actionType: com.culture.tracker.domain.model.ActionType): CalendarAction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(action: CalendarAction): Long

    @Update
    suspend fun update(action: CalendarAction)

    @Delete
    suspend fun delete(action: CalendarAction)
}
