package com.lumiere.app.data.local.dao

import androidx.room.*
import com.lumiere.app.data.local.entity.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE status != 4 ORDER BY priority DESC, dueDate ASC")
    fun getAllActiveTasks(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)
}
