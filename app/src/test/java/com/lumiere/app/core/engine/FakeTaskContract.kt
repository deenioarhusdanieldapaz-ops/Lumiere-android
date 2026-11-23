package com.lumiere.app.core.engine

import com.lumiere.app.core.contract.TaskContract
import com.lumiere.app.core.contract.TaskSnapshot

class FakeTaskContract(private val tasks: List<TaskSnapshot>) : TaskContract {
    override val contractVersion: String = "fake-1.0.0"
    override suspend fun getActiveTasks(windowDays: Int): Result<List<TaskSnapshot>> =
        Result.success(tasks)
    override suspend fun getTask(id: String): Result<TaskSnapshot?> =
        Result.success(tasks.find { it.id == id })
}
