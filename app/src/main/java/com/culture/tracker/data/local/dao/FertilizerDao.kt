package com.culture.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.culture.tracker.data.local.entity.Fertilizer
import kotlinx.coroutines.flow.Flow

@Dao
interface FertilizerDao {
    @Query("SELECT * FROM fertilizers ORDER BY name ASC")
    fun observeAll(): Flow<List<Fertilizer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(fertilizer: Fertilizer): Long

    @Update
    suspend fun update(fertilizer: Fertilizer)

    @Delete
    suspend fun delete(fertilizer: Fertilizer)
}
