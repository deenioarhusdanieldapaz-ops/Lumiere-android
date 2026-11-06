package com.lumiere.app.data.local.dao

import androidx.room.*
import com.lumiere.app.data.local.entity.TaskHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskHistoryDao {
    @Query("SELECT * FROM task_history WHERE taskId = :taskId ORDER BY timestamp DESC")
    fun getHistoryForTask(taskId: Long): Flow<List<TaskHistory>>

    @Insert
    suspend fun insertHistory(history: TaskHistory)
}
