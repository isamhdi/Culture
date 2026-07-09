package com.culture.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.culture.tracker.data.local.entity.EnvironmentLog
import kotlinx.coroutines.flow.Flow

@Dao
interface EnvironmentLogDao {
    @Query("SELECT * FROM environment_logs WHERE environmentId = :environmentId ORDER BY date DESC, id DESC")
    fun observeForEnvironment(environmentId: Long): Flow<List<EnvironmentLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(log: EnvironmentLog): Long

    @Delete
    suspend fun delete(log: EnvironmentLog)
}
