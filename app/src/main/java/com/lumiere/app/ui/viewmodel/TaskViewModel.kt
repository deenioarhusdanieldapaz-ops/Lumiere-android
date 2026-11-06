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

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {
    val tasks: StateFlow<List<Task>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addTask(title: String, category: String?, tags: String?, priority: Int, dueDate: Long?) {
        viewModelScope.launch {
            repository.insertTask(Task(title = title, category = category, tags = tags, priority = priority, dueDate = dueDate))
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
        }
    }

    fun duplicateTask(task: Task) {
        viewModelScope.launch {
            val newTask = task.copy(id = 0, title = "${task.title} (Cópia)", status = 0, createdAt = System.currentTimeMillis())
            repository.insertTask(newTask)
        }
    }

    fun archiveTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task.copy(status = 4)) // 4 = Arquivada
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
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
