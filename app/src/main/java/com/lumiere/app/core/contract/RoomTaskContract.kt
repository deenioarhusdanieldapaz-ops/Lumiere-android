package com.lumiere.app.core.contract

import com.lumiere.app.core.model.Sensitivity
import com.lumiere.app.data.local.dao.TaskDao
import kotlinx.coroutines.flow.first

class RoomTaskContract(private val taskDao: TaskDao) : TaskContract {
    override val contractVersion = "1.0.0"

    override suspend fun getActiveTasks(windowDays: Int): Result<List<TaskSnapshot>> = runCatching {
        taskDao.getAllActiveTasks().first().map { it.toSnapshot() }
    }

    override suspend fun getTask(id: String): Result<TaskSnapshot?> = runCatching {
        val taskId = id.toLongOrNull() ?: return@runCatching null
        taskDao.getTaskById(taskId)?.toSnapshot()
    }

    private fun com.lumiere.app.data.local.entity.Task.toSnapshot() = TaskSnapshot(
        id = id.toString(),
        titleShort = title.take(60),
        state = when (status) {
            2 -> TaskState.DONE
            4 -> TaskState.ARCHIVED
            else -> TaskState.ACTIVE
        },
        deadlineMillis = dueDate,
        priorityDeclared = priority,
        estimatedEffortMinutes = estimatedDurationMinutes,
        dependencyIds = blockedByTaskId?.let { listOf(it.toString()) } ?: emptyList(),
        lastUpdatedMillis = updatedAt,
        sensitivity = Sensitivity.NORMAL,
        category = category, tags = tags, urgency = urgency,
        importance = importance, energy = energy,
        timeReservedMinutes = timeReservedMinutes, actualDurationMinutes = actualDurationMinutes
    )
}
