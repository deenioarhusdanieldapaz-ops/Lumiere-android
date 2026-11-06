package com.lumiere.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lumiere.app.data.local.entity.Task
import com.lumiere.app.data.repository.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {
    val tasks: StateFlow<List<Task>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addTask(task: Task) {
        viewModelScope.launch {
            repository.insertTask(task)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            val newStatus = if (task.isCompleted) 0 else 2
            repository.updateTask(task.copy(status = newStatus, updatedAt = System.currentTimeMillis()))
            
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
        repository.insertTask(newTask)
    }

    fun duplicateTask(task: Task) {
        viewModelScope.launch { repository.insertTask(task.copy(id = 0, title = "${task.title} (Cópia)", status = 0)) }
    }

    fun archiveTask(task: Task) {
        viewModelScope.launch { repository.updateTask(task.copy(status = 4)) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { repository.deleteTask(task) }
    }
}


    fun toggleTimer(task: Task) {
        viewModelScope.launch {
            if (task.timerStartedAt != null) {
                // Parar o cronómetro: calcular tempo decorrido
                val elapsedMillis = System.currentTimeMillis() - task.timerStartedAt
                val elapsedMinutes = (elapsedMillis / 60000).toInt()
                repository.updateTask(task.copy(
                    actualDurationMinutes = task.actualDurationMinutes + elapsedMinutes,
                    timerStartedAt = null,
                    updatedAt = System.currentTimeMillis()
                ))
            } else {
                // Iniciar o cronómetro
                repository.updateTask(task.copy(
                    timerStartedAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                ))
            }
        }
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
