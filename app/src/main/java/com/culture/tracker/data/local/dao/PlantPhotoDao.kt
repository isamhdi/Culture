package com.culture.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.culture.tracker.data.local.entity.PlantPhoto
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantPhotoDao {
    @Query("SELECT * FROM plant_photos WHERE plantId = :plantId ORDER BY takenAt DESC")
    fun observeForPlant(plantId: Long): Flow<List<PlantPhoto>>

    @Query("SELECT * FROM plant_photos WHERE id IN (SELECT MAX(id) FROM plant_photos GROUP BY plantId)")
    fun observeLatestPerPlant(): Flow<List<PlantPhoto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: PlantPhoto): Long

    @Delete
    suspend fun delete(photo: PlantPhoto)
}
