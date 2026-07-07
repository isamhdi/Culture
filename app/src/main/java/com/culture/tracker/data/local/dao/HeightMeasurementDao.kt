package com.culture.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.culture.tracker.data.local.entity.HeightMeasurement
import kotlinx.coroutines.flow.Flow

@Dao
interface HeightMeasurementDao {
    @Query("SELECT * FROM height_measurements WHERE plantId = :plantId ORDER BY date ASC, id ASC")
    fun observeForPlant(plantId: Long): Flow<List<HeightMeasurement>>

    @Query("SELECT * FROM height_measurements WHERE id IN (SELECT MAX(id) FROM height_measurements GROUP BY plantId)")
    fun observeLatestPerPlant(): Flow<List<HeightMeasurement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(measurement: HeightMeasurement): Long

    @Delete
    suspend fun delete(measurement: HeightMeasurement)
}
