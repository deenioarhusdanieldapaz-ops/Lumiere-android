package com.lumiere.app.core.contract

class EmptyTaskContract : TaskContract {
    override val contractVersion: String = "0.0.0-stub"
    override suspend fun getActiveTasks(windowDays: Int): Result<List<TaskSnapshot>> = Result.success(emptyList())
    override suspend fun getTask(id: String): Result<TaskSnapshot?> = Result.success(null)
    override suspend fun updateTaskDueDate(id: String, newDueDate: Long): Result<Unit> = Result.success(Unit)
}
