package com.lumiere.app.core.contract

import com.lumiere.app.core.model.Schedulable
import com.lumiere.app.core.model.Sensitivity

interface TaskContract {
    val contractVersion: String
    suspend fun getActiveTasks(windowDays: Int): Result<List<TaskSnapshot>>
    suspend fun getTask(id: String): Result<TaskSnapshot?>
}

data class TaskSnapshot(
    override val id: String, 
    val titleShort: String, 
    val state: TaskState,
    override val deadlineMillis: Long?, 
    val priorityDeclared: Int, 
    val estimatedEffortMinutes: Int?,
    val dependencyIds: List<String>, 
    val lastUpdatedMillis: Long,
    val sensitivity: Sensitivity = Sensitivity.NORMAL,
    val category: String? = null, 
    val tags: String? = null,
    val urgency: Int = 1, 
    val importance: Int = 1, 
    val energy: Int = 1,
    val timeReservedMinutes: Int? = null, 
    val actualDurationMinutes: Int = 0
) : Schedulable {
    override val moduleId: String get() = "M01_TASKS"
    override val title: String get() = titleShort
    override val startTimeMillis: Long? get() = deadlineMillis?.let { prazo ->
        prazo - ((estimatedEffortMinutes ?: 30) * 60_000L)
    }
    override val durationMinutes: Int? get() = estimatedEffortMinutes ?: 30
    override val energyCost: Int get() = energy
    override val priority: Int get() = importance
}
enum class TaskState { ACTIVE, IN_PROGRESS, DONE, OVERDUE, BLOCKED, PAUSED, CANCELLED, ARCHIVED }
