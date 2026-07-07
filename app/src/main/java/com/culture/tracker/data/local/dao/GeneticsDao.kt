package com.culture.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.culture.tracker.data.local.entity.Genetics
import kotlinx.coroutines.flow.Flow

@Dao
interface GeneticsDao {
    @Query("SELECT * FROM genetics ORDER BY name ASC")
    fun observeAll(): Flow<List<Genetics>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(genetics: Genetics): Long

    @Update
    suspend fun update(genetics: Genetics)

    @Delete
    suspend fun delete(genetics: Genetics)
}
