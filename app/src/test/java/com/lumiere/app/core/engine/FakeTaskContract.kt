package com.lumiere.app.core.engine

import com.lumiere.app.core.contract.TaskContract
import com.lumiere.app.core.contract.TaskSnapshot

class FakeTaskContract(
    private var tasks: List<TaskSnapshot> = emptyList()
) : TaskContract {
    override val contractVersion: String = "fake-1.0.0"
    
    // Guarda as datas adiadas para os testes poderem verificar
    val updatedDueDates = mutableMapOf<String, Long>()

    override suspend fun getActiveTasks(windowDays: Int): Result<List<TaskSnapshot>> =
        Result.success(tasks)
        
    override suspend fun getTask(id: String): Result<TaskSnapshot?> =
        Result.success(tasks.find { it.id == id })
        
    override suspend fun updateTaskDueDate(id: String, newDueDate: Long): Result<Unit> {
        updatedDueDates[id] = newDueDate
        tasks = tasks.map { if (it.id == id) it.copy(deadlineMillis = newDueDate) else it }
        return Result.success(Unit)
    }
}
