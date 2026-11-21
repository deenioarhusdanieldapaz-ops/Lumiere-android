package com.lumiere.app.core.contract

import com.lumiere.app.core.model.Sensitivity

interface TaskContract {
    val contractVersion: String
    suspend fun getActiveTasks(windowDays: Int): Result<List<TaskSnapshot>>
    suspend fun getTask(id: String): Result<TaskSnapshot?>
}

data class TaskSnapshot(
    val id: String,
    val titleShort: String,
    val state: TaskState,
    val deadlineMillis: Long?,
    val priorityDeclared: Int,
    val estimatedEffortMinutes: Int?,
    val dependencyIds: List<String>,
    val lastUpdatedMillis: Long,
    val sensitivity: Sensitivity = Sensitivity.NORMAL
)

enum class TaskState { ACTIVE, DONE, ARCHIVED }
