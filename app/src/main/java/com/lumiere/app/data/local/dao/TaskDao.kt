package com.lumiere.app.data.local.dao

import androidx.room.*
import com.lumiere.app.data.local.entity.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE deletedAt IS NULL ORDER BY priority DESC, dueDate ASC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): Task?

    @Query("SELECT completedAt FROM tasks WHERE status = 2 AND completedAt IS NOT NULL AND completedAt >= :since ORDER BY completedAt ASC")
    suspend fun completionTimestampsSince(since: Long): List<Long>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)
}
