package com.culture.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.culture.tracker.data.local.entity.SensorSource
import kotlinx.coroutines.flow.Flow

@Dao
interface SensorSourceDao {
    @Query("SELECT * FROM sensor_sources WHERE environmentId = :environmentId LIMIT 1")
    fun observeForEnvironment(environmentId: Long): Flow<SensorSource?>

    @Query("SELECT * FROM sensor_sources")
    suspend fun getAll(): List<SensorSource>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(source: SensorSource): Long

    @Update
    suspend fun update(source: SensorSource)

    @Delete
    suspend fun delete(source: SensorSource)
}
