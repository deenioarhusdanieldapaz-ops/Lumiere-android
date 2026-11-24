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

    // ==== Compatibilidade com ecrãs antigos (Dashboard, GlobalSearch, Notifications, WeeklyReview) ====
    val tasks: StateFlow<List<Task>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val projects: StateFlow<List<Project>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiError = MutableStateFlow<String?>(null)
    val uiError: StateFlow<String?> = _uiError.asStateFlow()
    fun clearError() { _uiError.value = null }

    // ==== Novo: estado de UI combinado para a TaskScreen ====
    private val _activeFilter = MutableStateFlow("ALL")
    private val _searchQuery = MutableStateFlow("")
    private val _selectedProjectFilter = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<TaskUiState> = combine(
        repository.allTasks,
        _activeFilter,
        _searchQuery,
        _selectedProjectFilter
    ) { tasks, filter, query, projectId ->
        val now = timeProvider.currentTimeMillis()
        val endOfDay = getEndOfDay(now)
        val overdue = tasks.filter { !it.isCompleted && it.dueDate != null && it.dueDate < now }
        val filtered = tasks.filter { task ->
            val matchesSearch = query.isBlank() || task.title.contains(query, ignoreCase = true)
            val matchesFilter = when (filter) {
                "TODAY" -> !task.isCompleted && task.dueDate != null && task.dueDate >= now && task.dueDate <= endOfDay
                "OVERDUE" -> !task.isCompleted && task.dueDate != null && task.dueDate < now
                "COMPLETED" -> task.isCompleted
                else -> true
            }
            val matchesProject = projectId == null || task.projectId == projectId
            matchesSearch && matchesFilter && matchesProject
        }
        TaskUiState(
            tasks = tasks,
            filteredTasks = filtered,
            activeFilter = filter,
            searchQuery = query,
            selectedProjectFilter = projectId,
            tasksOverdueCount = overdue.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TaskUiState())

    fun setFilter(filter: String) { _activeFilter.value = filter }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setProjectFilter(projectId: Long?) { _selectedProjectFilter.value = projectId }

    private fun getEndOfDay(timestamp: Long): Long {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
        cal.set(java.util.Calendar.MINUTE, 59)
        cal.set(java.util.Calendar.SECOND, 59)
        cal.set(java.util.Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    fun getTaskById(id: Long): StateFlow<Task?> = flow { emit(repository.getTaskById(id)) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun getSubtasks(taskId: Long): StateFlow<List<Subtask>> = repository.getSubtasks(taskId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getHistory(taskId: Long): StateFlow<List<TaskHistory>> = repository.getHistory(taskId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
