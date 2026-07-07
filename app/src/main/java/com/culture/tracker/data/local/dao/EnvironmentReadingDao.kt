package com.culture.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.culture.tracker.data.local.entity.EnvironmentReading
import kotlinx.coroutines.flow.Flow

@Dao
interface EnvironmentReadingDao {
    @Query("SELECT * FROM environment_readings WHERE environmentId = :environmentId ORDER BY recordedAt DESC")
    fun observeForEnvironment(environmentId: Long): Flow<List<EnvironmentReading>>

    @Query("SELECT * FROM environment_readings WHERE id IN (SELECT MAX(id) FROM environment_readings GROUP BY environmentId)")
    fun observeLatestPerEnvironment(): Flow<List<EnvironmentReading>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reading: EnvironmentReading): Long
}
