package com.lumiere.app.data.local.dao

import androidx.room.*
import com.lumiere.app.data.local.entity.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE isArchived = 0 ORDER BY priority DESC, dueDate ASC")
    fun getAllActiveTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND isArchived = 0 AND dueDate <= :endOfDay AND dueDate >= :startOfDay")
    fun getTasksForToday(startOfDay: Long, endOfDay: Long): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)
}
