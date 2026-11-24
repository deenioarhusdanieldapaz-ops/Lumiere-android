package com.lumiere.app.ui.viewmodel

import com.lumiere.app.data.local.entity.Task

data class TaskUiState(
    val tasks: List<Task> = emptyList(),
    val filteredTasks: List<Task> = emptyList(),
    val activeFilter: String = "ALL",
    val searchQuery: String = "",
    val selectedProjectFilter: Long? = null,
    val tasksOverdueCount: Int = 0,
    val isLoading: Boolean = false
)
