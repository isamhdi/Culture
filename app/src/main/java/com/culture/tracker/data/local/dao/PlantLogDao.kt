package com.culture.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.culture.tracker.data.local.entity.PlantLog
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantLogDao {
    @Query("SELECT * FROM plant_logs WHERE plantId = :plantId ORDER BY date DESC, id DESC")
    fun observeForPlant(plantId: Long): Flow<List<PlantLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(log: PlantLog): Long

    @Delete
    suspend fun delete(log: PlantLog)
}
