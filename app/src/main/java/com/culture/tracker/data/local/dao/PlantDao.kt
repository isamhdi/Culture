package com.culture.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.culture.tracker.data.local.entity.Plant
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantDao {
    @Query("SELECT * FROM plants WHERE archived = 0 ORDER BY name ASC")
    fun observeActive(): Flow<List<Plant>>

    @Query("SELECT * FROM plants WHERE id = :id")
    fun observeById(id: Long): Flow<Plant?>

    @Query("SELECT * FROM plants WHERE id = :id")
    suspend fun getById(id: Long): Plant?

    @Query("SELECT * FROM plants WHERE archived = 0 AND wateringIntervalDays IS NOT NULL")
    suspend fun getPlantsWithWateringSchedule(): List<Plant>

    @Query("SELECT * FROM plants WHERE archived = 0 AND fertilizingIntervalDays IS NOT NULL")
    suspend fun getPlantsWithFertilizingSchedule(): List<Plant>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(plant: Plant): Long

    @Update
    suspend fun update(plant: Plant)

    @Delete
    suspend fun delete(plant: Plant)
}
