package com.culture.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.culture.tracker.data.local.entity.PhaseHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface PhaseHistoryDao {
    @Query("SELECT * FROM phase_history WHERE plantId = :plantId ORDER BY startDate ASC")
    fun observeForPlant(plantId: Long): Flow<List<PhaseHistory>>

    @Query("SELECT * FROM phase_history WHERE plantId = :plantId ORDER BY startDate ASC")
    suspend fun getForPlant(plantId: Long): List<PhaseHistory>

    @Query("SELECT * FROM phase_history WHERE endDate IS NULL")
    fun observeAllOpenPhases(): Flow<List<PhaseHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(phaseHistory: PhaseHistory): Long

    @Update
    suspend fun update(phaseHistory: PhaseHistory)

    @Delete
    suspend fun delete(phaseHistory: PhaseHistory)
}
