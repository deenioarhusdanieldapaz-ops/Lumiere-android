package com.lumiere.app.domain.usecase

import com.lumiere.app.data.local.entity.*
import com.lumiere.app.data.repository.TaskRepository
import kotlinx.coroutines.flow.first
import java.util.Calendar

class AddTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(task: Task): Long {
        val id = repository.insertTask(task)
        repository.insertHistory(TaskHistory(taskId = id, action = "Tarefa Criada"))
        return id
    }
}

class UpdateTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(task: Task) {
        repository.updateTask(task.copy(updatedAt = System.currentTimeMillis()))
        repository.insertHistory(TaskHistory(taskId = task.id, action = "Tarefa Editada"))
    }
}

class CreateNextRecurringTaskUseCase(private val repository: TaskRepository) {
    suspend fun invoke(task: Task) {
        val baseDate = task.dueDate ?: System.currentTimeMillis()
        val nextDate = when (task.recurrenceRule) {
            "DAILY" -> addDays(baseDate, 1)
            "WEEKLY" -> addDays(baseDate, 7)
            "MONTHLY" -> addMonths(baseDate, 1)
            "YEARLY" -> addYears(baseDate, 1)
            else -> return
        }
        val newTask = task.copy(
            id = 0, status = 0, dueDate = nextDate,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            completedAt = null,
            timerStartedAt = null,
            actualDurationMinutes = 0
        )
        val newId = repository.insertTask(newTask)
        repository.insertHistory(TaskHistory(taskId = newId, action = "Gerada por Recorrência"))
        // D-26 fix: copiar subtarefas
        val subtasks = repository.getSubtasks(task.id).first()
        subtasks.forEach { st ->
            repository.insertSubtask(st.copy(id = 0, taskId = newId, isCompleted = false))
        }
    }

    private fun addDays(base: Long, days: Int): Long =
        base + days * 24L * 60L * 60L * 1000L

    private fun addMonths(base: Long, months: Int): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = base
        val originalDay = cal.get(Calendar.DAY_OF_MONTH)
        cal.add(Calendar.MONTH, months)
        if (cal.get(Calendar.DAY_OF_MONTH) != originalDay) {
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        }
        return cal.timeInMillis
    }

    private fun addYears(base: Long, years: Int): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = base
        val originalDay = cal.get(Calendar.DAY_OF_MONTH)
        cal.add(Calendar.YEAR, years)
        if (cal.get(Calendar.DAY_OF_MONTH) != originalDay) {
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        }
        return cal.timeInMillis
    }
}

class ToggleTaskCompletionUseCase(private val repository: TaskRepository) {
    private val createNext = CreateNextRecurringTaskUseCase(repository)

    suspend operator fun invoke(task: Task) {
        val newStatus = if (task.isCompleted) 0 else 2
        val newCompletedAt = if (newStatus == 2) System.currentTimeMillis() else null
        repository.updateTask(task.copy(status = newStatus, completedAt = newCompletedAt, updatedAt = System.currentTimeMillis()))
        repository.insertHistory(TaskHistory(taskId = task.id, action = if (newStatus == 2) "Concluída" else "Reaberta"))
        if (newStatus == 2 && task.recurrenceRule != null && task.recurrenceRule != "NONE") {
            createNext(task)
        }
    }
}

class CancelTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(task: Task, reason: String) {
        repository.updateTask(task.copy(status = 3, cancellationReason = reason, timerStartedAt = null, completedAt = null, updatedAt = System.currentTimeMillis()))
        repository.insertHistory(TaskHistory(taskId = task.id, action = "Cancelada: $reason"))
    }
}

class ResumeTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(task: Task) {
        repository.updateTask(task.copy(status = 0, cancellationReason = null, completedAt = null, updatedAt = System.currentTimeMillis()))
        repository.insertHistory(TaskHistory(taskId = task.id, action = "Retomada"))
    }
}

class DuplicateTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(task: Task): Long {
        val newTask = task.copy(id = 0, title = "${task.title} (Cópia)", status = 0)
        val newId = repository.insertTask(newTask)
        repository.insertHistory(TaskHistory(taskId = newId, action = "Tarefa Duplicada"))
        return newId
    }
}

class ArchiveTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(task: Task) {
        repository.updateTask(task.copy(status = 4))
        repository.insertHistory(TaskHistory(taskId = task.id, action = "Arquivada"))
    }
}

class DeleteTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(task: Task) {
        repository.deleteTask(task)
    }
}

class ToggleTimerUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(task: Task) {
        if (task.timerStartedAt != null) {
            // D-24 fix: impedir valores negativos
            val elapsedMinutes = ((System.currentTimeMillis() - task.timerStartedAt) / 60000).toInt().coerceAtLeast(0)
            repository.updateTask(task.copy(
                actualDurationMinutes = task.actualDurationMinutes + elapsedMinutes,
                timerStartedAt = null,
                updatedAt = System.currentTimeMillis()
            ))
            repository.insertHistory(TaskHistory(taskId = task.id, action = "Cronómetro Parado (+$elapsedMinutes min)"))
        } else {
            repository.updateTask(task.copy(timerStartedAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()))
            repository.insertHistory(TaskHistory(taskId = task.id, action = "Cronómetro Iniciado"))
        }
    }
}

class AddSubtaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(taskId: Long, title: String) {
        repository.insertSubtask(Subtask(taskId = taskId, title = title))
        repository.insertHistory(TaskHistory(taskId = taskId, action = "Subtarefa Adicionada: $title"))
    }
}

class ToggleSubtaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(subtask: Subtask) {
        repository.updateSubtask(subtask.copy(isCompleted = !subtask.isCompleted))
    }
}

class DeleteSubtaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(subtask: Subtask) {
        repository.deleteSubtask(subtask)
    }
}

class AddProjectUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(name: String) {
        repository.insertProject(Project(name = name))
    }
}
