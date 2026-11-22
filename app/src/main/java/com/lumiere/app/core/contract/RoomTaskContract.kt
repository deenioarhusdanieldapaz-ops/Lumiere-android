package com.lumiere.app.core.contract

import com.lumiere.app.core.model.Sensitivity
import com.lumiere.app.data.local.dao.TaskDao

class RoomTaskContract(private val taskDao: TaskDao) : TaskContract {
    override val contractVersion = "1.0.0"

    override suspend fun getActiveTasks(windowDays: Int): Result<List<TaskSnapshot>> = runCatching {
        val tasks = taskDao.getActiveTasks() // Assumindo que este método existe no DAO
        tasks.map { it.toSnapshot() }
    }

    override suspend fun getTask(id: String): Result<TaskSnapshot?> = runCatching {
        val taskId = id.toLongOrNull() ?: return@runCatching null
        val task = taskDao.getTaskById(taskId)
        task?.toSnapshot()
    }

    private fun com.lumiere.app.data.local.entity.Task.toSnapshot() = TaskSnapshot(
        id = id.toString(),
        titleShort = title.take(60),
        state = if (isCompleted) TaskState.DONE else TaskState.ACTIVE,
        deadlineMillis = dueDate,
        priorityDeclared = priority,
        estimatedEffortMinutes = estimatedDurationMinutes,
        dependencyIds = blockedByTaskId?.let { listOf(it.toString()) } ?: emptyList(),
        lastUpdatedMillis = updatedAt,
        sensitivity = Sensitivity.NORMAL
    )
}
