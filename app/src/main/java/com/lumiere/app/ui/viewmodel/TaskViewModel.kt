package com.lumiere.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lumiere.app.data.local.entity.*
import com.lumiere.app.data.repository.TaskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {
    val tasks: StateFlow<List<Task>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val projects: StateFlow<List<Project>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getSubtasks(taskId: Long): StateFlow<List<Subtask>> = repository.getSubtasks(taskId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getHistory(taskId: Long): StateFlow<List<TaskHistory>> = repository.getHistory(taskId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTask(task: Task) {
        viewModelScope.launch {
            val id = repository.insertTask(task)
            repository.insertHistory(TaskHistory(taskId = id, action = "Tarefa Criada"))
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task.copy(updatedAt = System.currentTimeMillis()))
            repository.insertHistory(TaskHistory(taskId = task.id, action = "Tarefa Editada"))
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            val newStatus = if (task.isCompleted) 0 else 2
            val newCompletedAt = if (newStatus == 2) System.currentTimeMillis() else null
            repository.updateTask(task.copy(status = newStatus, completedAt = newCompletedAt, updatedAt = System.currentTimeMillis()))
            repository.insertHistory(TaskHistory(taskId = task.id, action = if (newStatus == 2) "Concluída" else "Reaberta"))
            
            if (newStatus == 2 && task.recurrenceRule != null && task.recurrenceRule != "NONE") {
                createNextRecurringTask(task)
            }
        }
    }

    private suspend fun createNextRecurringTask(task: Task) {
        val calendar = Calendar.getInstance()
        val baseDate = task.dueDate ?: System.currentTimeMillis()
        calendar.timeInMillis = baseDate

        when (task.recurrenceRule) {
            "DAILY" -> calendar.add(Calendar.DAY_OF_YEAR, 1)
            "WEEKLY" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            "MONTHLY" -> calendar.add(Calendar.MONTH, 1)
            "YEARLY" -> calendar.add(Calendar.YEAR, 1)
        }

        val newTask = task.copy(id = 0, status = 0, dueDate = calendar.timeInMillis, createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis())
        val newId = repository.insertTask(newTask)
        repository.insertHistory(TaskHistory(taskId = newId, action = "Gerada por Recorrência"))
    }

    fun duplicateTask(task: Task) {
        viewModelScope.launch {
            val newTask = task.copy(id = 0, title = "${task.title} (Cópia)", status = 0)
            val newId = repository.insertTask(newTask)
            repository.insertHistory(TaskHistory(taskId = newId, action = "Tarefa Duplicada"))
        }
    }

    fun archiveTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task.copy(status = 4))
            repository.insertHistory(TaskHistory(taskId = task.id, action = "Arquivada"))
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { repository.deleteTask(task) }
    }

    fun cancelTask(task: Task, reason: String) {
        viewModelScope.launch {
            repository.updateTask(task.copy(status = 3, cancellationReason = reason, timerStartedAt = null, completedAt = null, updatedAt = System.currentTimeMillis()))
            repository.insertHistory(TaskHistory(taskId = task.id, action = "Cancelada: $reason"))
        }
    }

    fun resumeTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task.copy(status = 0, cancellationReason = null, completedAt = null, updatedAt = System.currentTimeMillis()))
            repository.insertHistory(TaskHistory(taskId = task.id, action = "Retomada"))
        }
    }

    fun toggleTimer(task: Task) {
        viewModelScope.launch {
            if (task.timerStartedAt != null) {
                val elapsedMillis = System.currentTimeMillis() - task.timerStartedAt
                val elapsedMinutes = (elapsedMillis / 60000).toInt()
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

    fun addSubtask(taskId: Long, title: String) {
        viewModelScope.launch {
            repository.insertSubtask(Subtask(taskId = taskId, title = title))
            repository.insertHistory(TaskHistory(taskId = taskId, action = "Subtarefa Adicionada: $title"))
        }
    }

    fun toggleSubtask(subtask: Subtask) {
        viewModelScope.launch {
            repository.updateSubtask(subtask.copy(isCompleted = !subtask.isCompleted))
        }
    }

    fun deleteSubtask(subtask: Subtask) {
        viewModelScope.launch { repository.deleteSubtask(subtask) }
    }

    fun addProject(name: String) {
        viewModelScope.launch { repository.insertProject(Project(name = name)) }
    }
}

class TaskViewModelFactory(private val repository: TaskRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
