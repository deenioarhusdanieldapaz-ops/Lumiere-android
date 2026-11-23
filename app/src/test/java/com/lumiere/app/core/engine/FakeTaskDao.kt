package com.lumiere.app.core.engine

import com.lumiere.app.data.local.dao.TaskDao
import com.lumiere.app.data.local.entity.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeTaskDao : TaskDao {
    override fun getAllActiveTasks(): Flow<List<Task>> = flowOf(emptyList())
    override suspend fun getTaskById(id: Long): Task? = null
    override suspend fun completionTimestampsSince(since: Long): List<Long> = emptyList()
    override suspend fun insertTask(task: Task): Long = 0L
    override suspend fun updateTask(task: Task) {}
    override suspend fun deleteTask(task: Task) {}
}
