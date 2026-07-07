package com.culture.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.culture.tracker.data.local.entity.Environment
import kotlinx.coroutines.flow.Flow

@Dao
interface EnvironmentDao {
    @Query("SELECT * FROM environments ORDER BY name ASC")
    fun observeAll(): Flow<List<Environment>>

    @Query("SELECT * FROM environments WHERE id = :id")
    fun observeById(id: Long): Flow<Environment?>

    @Query("SELECT * FROM environments WHERE id = :id")
    suspend fun getById(id: Long): Environment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(environment: Environment): Long

    @Update
    suspend fun update(environment: Environment)

    @Delete
    suspend fun delete(environment: Environment)
}
