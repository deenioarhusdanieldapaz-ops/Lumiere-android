package com.lumiere.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lumiere.app.data.local.entity.*
import com.lumiere.app.data.repository.TaskRepository
import com.lumiere.app.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository, private val timeProvider: com.lumiere.app.core.time.TimeProvider) : ViewModel() {

    val tasks: StateFlow<List<Task>> = repository.allTasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val projects: StateFlow<List<Project>> = repository.allProjects.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiError = MutableStateFlow<String?>(null)
    val uiError: StateFlow<String?> = _uiError.asStateFlow()

    fun clearError() { _uiError.value = null }

    fun getSubtasks(taskId: Long): StateFlow<List<Subtask>> = repository.getSubtasks(taskId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun getHistory(taskId: Long): StateFlow<List<TaskHistory>> = repository.getHistory(taskId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val addTaskUC = AddTaskUseCase(repository)
    private val updateTaskUC = UpdateTaskUseCase(repository)
    private val toggleCompletionUC = ToggleTaskCompletionUseCase(repository, timeProvider)
    private val cancelTaskUC = CancelTaskUseCase(repository)
    private val resumeTaskUC = ResumeTaskUseCase(repository)
    private val duplicateTaskUC = DuplicateTaskUseCase(repository)
    private val archiveTaskUC = ArchiveTaskUseCase(repository)
    private val deleteTaskUC = DeleteTaskUseCase(repository)
    private val toggleTimerUC = ToggleTimerUseCase(repository)
    private val addSubtaskUC = AddSubtaskUseCase(repository)
    private val toggleSubtaskUC = ToggleSubtaskUseCase(repository)
    private val deleteSubtaskUC = DeleteSubtaskUseCase(repository)
    private val addProjectUC = AddProjectUseCase(repository)

    private fun handleResult(result: Result<*>) {
        result.onFailure { _uiError.value = it.message ?: "Ocorreu um erro desconhecido" }
    }

    fun addTask(task: Task) { viewModelScope.launch { handleResult(addTaskUC(task)) } }
    fun updateTask(task: Task) { viewModelScope.launch { handleResult(updateTaskUC(task)) } }
    fun toggleTaskCompletion(task: Task) { viewModelScope.launch { handleResult(toggleCompletionUC(task)) } }
    fun cancelTask(task: Task, reason: String) { viewModelScope.launch { handleResult(cancelTaskUC(task, reason)) } }
    fun resumeTask(task: Task) { viewModelScope.launch { handleResult(resumeTaskUC(task)) } }
    fun duplicateTask(task: Task) { viewModelScope.launch { handleResult(duplicateTaskUC(task)) } }
    fun archiveTask(task: Task) { viewModelScope.launch { handleResult(archiveTaskUC(task)) } }
    fun deleteTask(task: Task) { viewModelScope.launch { handleResult(deleteTaskUC(task)) } }
    fun toggleTimer(task: Task) { viewModelScope.launch { handleResult(toggleTimerUC(task)) } }
    fun addSubtask(taskId: Long, title: String) { viewModelScope.launch { handleResult(addSubtaskUC(taskId, title)) } }
    fun toggleSubtask(subtask: Subtask) { viewModelScope.launch { handleResult(toggleSubtaskUC(subtask)) } }
    fun deleteSubtask(subtask: Subtask) { viewModelScope.launch { handleResult(deleteSubtaskUC(subtask)) } }
    fun addProject(name: String) { viewModelScope.launch { handleResult(addProjectUC(name)) } }
}

class TaskViewModelFactory(private val repository: TaskRepository, private val timeProvider: com.lumiere.app.core.time.TimeProvider) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(repository, timeProvider) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
