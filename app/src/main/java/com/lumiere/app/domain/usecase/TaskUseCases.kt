package com.lumiere.app.domain.usecase

import com.lumiere.app.data.local.entity.*
import com.lumiere.app.data.repository.TaskRepository
import com.lumiere.app.core.time.TimeProvider
import kotlinx.coroutines.flow.first
import java.util.Calendar

class AddTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(task: Task): Result<Long> {
        return repository.insertTask(task).onSuccess { id ->
            repository.insertHistory(TaskHistory(taskId = id, action = "Tarefa Criada"))
        }
    }
}

class UpdateTaskUseCase(private val repository: TaskRepository, private val timeProvider: TimeProvider) {
    suspend operator fun invoke(task: Task): Result<Unit> {
        return repository.updateTask(task.copy(updatedAt = timeProvider.currentTimeMillis())).onSuccess {
            repository.insertHistory(TaskHistory(taskId = task.id, action = "Tarefa Editada"))
        }
    }
}

class CreateNextRecurringTaskUseCase(private val repository: TaskRepository, private val timeProvider: TimeProvider) {
    suspend operator fun invoke(task: Task): Result<Unit> = runCatching {
        // Correção D1-B: Usa o prazo anterior como base
        val baseDate = task.dueDate ?: timeProvider.currentTimeMillis()
        val nextDate = when (task.recurrenceRule) {
            "DAILY" -> addDays(baseDate, 1)
            "WEEKLY" -> addDays(baseDate, 7)
            "MONTHLY" -> addMonths(baseDate, 1)
            "YEARLY" -> addYears(baseDate, 1)
            else -> return@runCatching
        }
        val newTask = task.copy(
            id = 0, status = 0, dueDate = nextDate,
            createdAt = timeProvider.currentTimeMillis(), updatedAt = timeProvider.currentTimeMillis(),
            completedAt = null, timerStartedAt = null, actualDurationMinutes = 0
        )
        val newId = repository.insertTask(newTask).getOrThrow()
        repository.insertHistory(TaskHistory(taskId = newId, action = "Gerada por Recorrência"))
        val subtasks = runCatching { repository.getSubtasks(task.id).first() }.getOrDefault(emptyList())
        subtasks.forEach { st -> repository.insertSubtask(st.copy(id = 0, taskId = newId, isCompleted = false)) }
    }

    private fun addDays(base: Long, days: Int): Long = base + days * 24L * 60L * 60L * 1000L
    private fun addMonths(base: Long, months: Int): Long {
        val cal = Calendar.getInstance(); cal.timeInMillis = base
        val originalDay = cal.get(Calendar.DAY_OF_MONTH)
        cal.add(Calendar.MONTH, months)
        if (cal.get(Calendar.DAY_OF_MONTH) != originalDay) cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        return cal.timeInMillis
    }
    private fun addYears(base: Long, years: Int): Long {
        val cal = Calendar.getInstance(); cal.timeInMillis = base
        val originalDay = cal.get(Calendar.DAY_OF_MONTH)
        cal.add(Calendar.YEAR, years)
        if (cal.get(Calendar.DAY_OF_MONTH) != originalDay) cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        return cal.timeInMillis
    }
}

class ToggleTaskCompletionUseCase(private val repository: TaskRepository, private val timeProvider: TimeProvider) {
    private val createNext = CreateNextRecurringTaskUseCase(repository, timeProvider)

    suspend operator fun invoke(task: Task): Result<Unit> {
        val newStatus = if (task.isCompleted) 0 else 2
        if (newStatus == 2 && task.blockedByTaskId != null) {
            val blocker = repository.getTaskById(task.blockedByTaskId)
            if (blocker != null && !blocker.isCompleted && blocker.status != 3 && blocker.status != 4) {
                repository.insertHistory(TaskHistory(taskId = task.id, action = "Conclusão bloqueada: dependência pendente"))
                return Result.failure(Exception("Tarefa bloqueada por dependência pendente"))
            }
        }
        val newCompletedAt = if (newStatus == 2) timeProvider.currentTimeMillis() else null
        val result = repository.updateTask(task.copy(status = newStatus, completedAt = newCompletedAt, updatedAt = timeProvider.currentTimeMillis()))
        result.onSuccess {
            repository.insertHistory(TaskHistory(taskId = task.id, action = if (newStatus == 2) "Concluída" else "Reaberta"))
            if (newStatus == 2 && task.recurrenceRule != null && task.recurrenceRule != "NONE") createNext(task)
        }
        return result
    }
}

class CancelTaskUseCase(private val repository: TaskRepository, private val timeProvider: TimeProvider) {
    suspend operator fun invoke(task: Task, reason: String): Result<Unit> {
        return repository.updateTask(task.copy(status = 3, cancellationReason = reason, timerStartedAt = null, completedAt = null, updatedAt = timeProvider.currentTimeMillis())).onSuccess {
            repository.insertHistory(TaskHistory(taskId = task.id, action = "Cancelada: $reason"))
        }
    }
}

class ResumeTaskUseCase(private val repository: TaskRepository, private val timeProvider: TimeProvider) {
    suspend operator fun invoke(task: Task): Result<Unit> {
        return repository.updateTask(task.copy(status = 0, cancellationReason = null, completedAt = null, updatedAt = timeProvider.currentTimeMillis())).onSuccess {
            repository.insertHistory(TaskHistory(taskId = task.id, action = "Retomada"))
        }
    }
}

class DuplicateTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(task: Task): Result<Long> {
        return repository.insertTask(task.copy(id = 0, title = "${task.title} (Cópia)", status = 0)).onSuccess { newId ->
            repository.insertHistory(TaskHistory(taskId = newId, action = "Tarefa Duplicada"))
            // Correção: Copiar subtarefas
            runCatching {
                repository.getSubtasks(task.id).first().forEach { st ->
                    repository.insertSubtask(st.copy(id = 0, taskId = newId, isCompleted = false))
                }
            }
        }
    }
}

class ArchiveTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(task: Task): Result<Unit> {
        return repository.updateTask(task.copy(status = 4)).onSuccess {
            repository.insertHistory(TaskHistory(taskId = task.id, action = "Arquivada"))
        }
    }
}

class DeleteTaskUseCase(private val repository: TaskRepository, private val timeProvider: TimeProvider) {
    suspend operator fun invoke(task: Task): Result<Unit> {
        val now = timeProvider.currentTimeMillis()
        return repository.updateTask(task.copy(deletedAt = now, updatedAt = now)).onSuccess {
            repository.insertHistory(TaskHistory(taskId = task.id, action = "Tarefa Eliminada (recuperável)"))
        }
    }
}

class ToggleTimerUseCase(private val repository: TaskRepository, private val timeProvider: TimeProvider) {
    suspend operator fun invoke(task: Task): Result<Unit> {
        if (task.timerStartedAt != null) {
            // Correção: Usa o timeProvider em vez de System.currentTimeMillis
            val elapsedMinutes = ((timeProvider.currentTimeMillis() - task.timerStartedAt) / 60000).toInt().coerceAtLeast(0)
            return repository.updateTask(task.copy(actualDurationMinutes = task.actualDurationMinutes + elapsedMinutes, timerStartedAt = null, updatedAt = timeProvider.currentTimeMillis())).onSuccess {
                repository.insertHistory(TaskHistory(taskId = task.id, action = "Cronómetro Parado (+$elapsedMinutes min)"))
            }
        } else {
            return repository.updateTask(task.copy(timerStartedAt = timeProvider.currentTimeMillis(), updatedAt = timeProvider.currentTimeMillis())).onSuccess {
                repository.insertHistory(TaskHistory(taskId = task.id, action = "Cronómetro Iniciado"))
            }
        }
    }
}

class AddSubtaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(taskId: Long, title: String): Result<Unit> {
        return repository.insertSubtask(Subtask(taskId = taskId, title = title)).onSuccess {
            repository.insertHistory(TaskHistory(taskId = taskId, action = "Subtarefa Adicionada: $title"))
        }.map { }
    }
}

class ToggleSubtaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(subtask: Subtask): Result<Unit> = repository.updateSubtask(subtask.copy(isCompleted = !subtask.isCompleted))
}

class DeleteSubtaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(subtask: Subtask): Result<Unit> = repository.deleteSubtask(subtask)
}

class AddProjectUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(name: String): Result<Long> = repository.insertProject(Project(name = name))
}
