package com.lumiere.app.core.contract

import com.lumiere.app.core.time.TimeProvider
import com.lumiere.app.data.local.dao.TaskDao
import com.lumiere.app.data.local.entity.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomTaskContractTest {
    private val fixedNow = 1_700_000_000_000L
    private val dayMs = 86_400_000L
    private val timeProvider = object : TimeProvider {
        override fun currentTimeMillis(): Long = fixedNow
    }

    private class ControlledTaskDao(private val tasks: List<Task>) : TaskDao {
        override fun getAllActiveTasks(): Flow<List<Task>> = flowOf(tasks)
        override suspend fun getTaskById(id: Long): Task? = tasks.find { it.id == id }
        override suspend fun completionTimestampsSince(since: Long): List<Long> = emptyList()
        override suspend fun insertTask(task: Task): Long = 0L
        override suspend fun updateTask(task: Task) {}
        override suspend fun deleteTask(task: Task) {}
    }

    @Test
    fun `getActiveTasks nao inclui tarefas DONE`() = runBlocking {
        val done = Task(id = 1, title = "Feita", status = 2, dueDate = fixedNow - dayMs)
        val active = Task(id = 2, title = "Ativa", status = 0, dueDate = fixedNow + dayMs)
        val contract = RoomTaskContract(ControlledTaskDao(listOf(done, active)), timeProvider)

        val result = contract.getActiveTasks(14).getOrThrow()
        assertEquals(1, result.size)
        assertEquals("2", result[0].id)
    }

    @Test
    fun `getActiveTasks nao inclui tarefas CANCELLED`() = runBlocking {
        val cancelled = Task(id = 1, title = "Cancelada", status = 3, dueDate = fixedNow + dayMs)
        val contract = RoomTaskContract(ControlledTaskDao(listOf(cancelled)), timeProvider)

        assertTrue(contract.getActiveTasks(14).getOrThrow().isEmpty())
    }

    @Test
    fun `getActiveTasks respeita windowDays para prazos futuros`() = runBlocking {
        val farFuture = Task(id = 1, title = "Longe", status = 0, dueDate = fixedNow + 20 * dayMs)
        val nearFuture = Task(id = 2, title = "Amanha", status = 0, dueDate = fixedNow + dayMs)
        val contract = RoomTaskContract(ControlledTaskDao(listOf(farFuture, nearFuture)), timeProvider)

        val result = contract.getActiveTasks(14).getOrThrow()
        assertEquals(1, result.size)
        assertEquals("2", result[0].id)
    }

    @Test
    fun `getActiveTasks inclui tarefas atrasadas mesmo fora da janela`() = runBlocking {
        val overdue = Task(id = 1, title = "Muito atrasada", status = 0, dueDate = fixedNow - 60 * dayMs)
        val contract = RoomTaskContract(ControlledTaskDao(listOf(overdue)), timeProvider)

        assertEquals(1, contract.getActiveTasks(14).getOrThrow().size)
    }
}
