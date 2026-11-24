package com.lumiere.app.core.contract

import com.lumiere.app.core.model.Schedulable
import com.lumiere.app.core.model.Sensitivity
import com.lumiere.app.core.time.TimeProvider
import com.lumiere.app.data.local.dao.TaskDao
import com.lumiere.app.data.local.entity.Task
import kotlinx.coroutines.flow.first

class RoomTaskContract(
    private val taskDao: TaskDao,
    private val timeProvider: TimeProvider
) : TaskContract {
    override val contractVersion = "1.1.0"

    override suspend fun getActiveTasks(windowDays: Int): Result<List<TaskSnapshot>> = runCatching {
        val now = timeProvider.currentTimeMillis()
        val windowMs = windowDays.toLong() * 86_400_000L
        taskDao.getAllTasks().first()
            .filter { isInAnalysisWindow(it, now, windowMs) }
            .map { it.toSnapshot() }
    }

    override suspend fun getTask(id: String): Result<TaskSnapshot?> = runCatching {
        val taskId = id.toLongOrNull() ?: return@runCatching null
        taskDao.getTaskById(taskId)?.toSnapshot()
    }

    /**
     * Regra explícita de janela:
     * - Sem prazo -> sempre (tarefa sem deadline continua relevante)
     * - Atrasada (dueDate < now) -> sempre (é o caso mais crítico)
     * - Dentro da janela [now, now+windowMs] -> sim
     * - Fora da janela (futuro distante) -> não
     */
    private fun isInAnalysisWindow(task: Task, now: Long, windowMs: Long): Boolean {
        // F12 defesa em profundidade: DONE(2), CANCELLED(3), ARCHIVED(4) nunca entram
        if (task.status == 2 || task.status == 3 || task.status == 4) return false
        if (task.deletedAt != null) return false
        val deadline = task.dueDate ?: return true
        if (deadline < now) return true
        return deadline <= now + windowMs
    }

    private fun Task.toSnapshot() = TaskSnapshot(
        id = id.toString(),
        titleShort = title.take(60),
        state = when {
            status == 2 -> TaskState.DONE
            status == 3 -> TaskState.CANCELLED
            status == 4 -> TaskState.ARCHIVED
            deletedAt != null -> TaskState.ARCHIVED
            blockedByTaskId != null -> TaskState.BLOCKED
            timerStartedAt != null -> TaskState.IN_PROGRESS
            dueDate != null && dueDate < timeProvider.currentTimeMillis() -> TaskState.OVERDUE
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
