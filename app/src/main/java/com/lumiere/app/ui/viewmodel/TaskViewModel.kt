package com.lumiere.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lumiere.app.data.local.entity.*
import com.lumiere.app.data.repository.TaskRepository
import com.lumiere.app.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    // ===== Observáveis (iguais à versão anterior) =====
    val tasks: StateFlow<List<Task>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val projects: StateFlow<List<Project>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getSubtasks(taskId: Long): StateFlow<List<Subtask>> = repository.getSubtasks(taskId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getHistory(taskId: Long): StateFlow<List<TaskHistory>> = repository.getHistory(taskId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ===== Use cases =====
    private val addTaskUC = AddTaskUseCase(repository)
    private val updateTaskUC = UpdateTaskUseCase(repository)
    private val toggleCompletionUC = ToggleTaskCompletionUseCase(repository)
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

    // ===== API pública (assinaturas iguais — testes passam) =====
    fun addTask(task: Task) {
        viewModelScope.launch { addTaskUC(task) }
    }
    fun updateTask(task: Task) {
        viewModelScope.launch { updateTaskUC(task) }
    }
    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch { toggleCompletionUC(task) }
    }
    fun cancelTask(task: Task, reason: String) {
        viewModelScope.launch { cancelTaskUC(task, reason) }
    }
    fun resumeTask(task: Task) {
        viewModelScope.launch { resumeTaskUC(task) }
    }
    fun duplicateTask(task: Task) {
        viewModelScope.launch { duplicateTaskUC(task) }
    }
    fun archiveTask(task: Task) {
        viewModelScope.launch { archiveTaskUC(task) }
    }
    fun deleteTask(task: Task) {
        viewModelScope.launch { deleteTaskUC(task) }
    }
    fun toggleTimer(task: Task) {
        viewModelScope.launch { toggleTimerUC(task) }
    }
    fun addSubtask(taskId: Long, title: String) {
        viewModelScope.launch { addSubtaskUC(taskId, title) }
    }
    fun toggleSubtask(subtask: Subtask) {
        viewModelScope.launch { toggleSubtaskUC(subtask) }
    }
    fun deleteSubtask(subtask: Subtask) {
        viewModelScope.launch { deleteSubtaskUC(subtask) }
    }
    fun addProject(name: String) {
        viewModelScope.launch { addProjectUC(name) }
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
