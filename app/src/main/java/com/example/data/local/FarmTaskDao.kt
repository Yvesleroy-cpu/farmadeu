package com.example.data.local

import androidx.room.*
import com.example.data.model.FarmTask
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmTaskDao {
    @Query("SELECT * FROM farm_tasks ORDER BY date ASC")
    fun getAllTasks(): Flow<List<FarmTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: FarmTask): Long

    @Update
    suspend fun updateTask(task: FarmTask)

    @Delete
    suspend fun deleteTask(task: FarmTask)

    @Query("DELETE FROM farm_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)
}
