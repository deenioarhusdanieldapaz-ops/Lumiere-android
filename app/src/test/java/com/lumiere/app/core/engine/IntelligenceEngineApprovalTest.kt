package com.lumiere.app.core.engine

import com.lumiere.app.core.memory.CoreMemory
import com.lumiere.app.core.memory.CoreMemoryDao
import com.lumiere.app.core.memory.CoreMemoryRepository
import com.lumiere.app.core.model.*
import com.lumiere.app.core.time.TimeProvider
import com.lumiere.app.data.local.dao.HabitDao
import com.lumiere.app.data.local.dao.TaskDao
import com.lumiere.app.data.local.entity.Habit
import com.lumiere.app.data.local.entity.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IntelligenceEngineApprovalTest {
    private val fixedNow = 1_700_000_000_000L
    private val timeProvider = object : TimeProvider {
        override fun currentTimeMillis(): Long = fixedNow
    }

    private class MutableTaskDao(private val tasks: MutableMap<Long, Task> = mutableMapOf()) : TaskDao {
        override fun getAllActiveTasks(): Flow<List<Task>> = flowOf(tasks.values.toList())
        override suspend fun getTaskById(id: Long): Task? = tasks[id]
        override suspend fun completionTimestampsSince(since: Long): List<Long> = emptyList()
        override suspend fun insertTask(task: Task): Long { tasks[task.id] = task; return task.id }
        override suspend fun updateTask(task: Task) { tasks[task.id] = task }
        override suspend fun deleteTask(task: Task) { tasks.remove(task.id) }
    }

    private class MutableHabitDao(private val habits: MutableMap<Long, Habit> = mutableMapOf()) : HabitDao {
        override fun getAllActiveHabits(): Flow<List<Habit>> = flowOf(habits.values.toList())
        override suspend fun getHabitById(id: Long): Habit? = habits[id]
        override suspend fun insertHabit(habit: Habit): Long { habits[habit.id] = habit; return habit.id }
        override suspend fun updateHabit(habit: Habit) { habits[habit.id] = habit }
        override suspend fun deleteHabit(habit: Habit) { habits.remove(habit.id) }
    }

    private class CountingMemoryDao : CoreMemoryDao {
        var deleteExpiredCalls = 0
        override suspend fun save(memory: CoreMemory) {}
        override suspend fun get(key: String): CoreMemory? = null
        override suspend fun getAllDecisions(): List<CoreMemory> = emptyList()
        override suspend fun deleteExpired(now: Long) { deleteExpiredCalls++ }
    }

    private fun buildEngine(
        taskDao: TaskDao = MutableTaskDao(),
        habitDao: HabitDao = MutableHabitDao(),
        memoryDao: CoreMemoryDao = CountingMemoryDao(),
        store: ApprovalStore = InMemoryApprovalStore()
    ) = IntelligenceEngine(
        taskContract = FakeTaskContract(emptyList()),
        habitContract = FakeHabitContract(emptyList()),
        rules = RecommendationRules(timeProvider),
        strictChecker = StrictConflictChecker(timeProvider),
        approvalGateway = ApprovalGateway(store, timeProvider),
        memory = CoreMemoryRepository(memoryDao, timeProvider),
        timeProvider = timeProvider,
        taskDao = taskDao,
        habitDao = habitDao
    )

    private fun suggestion(
        action: SuggestionAction,
        expiresAtMillis: Long? = fixedNow + 3_600_000L
    ) = Suggestion(
        id = "s1",
        statement = "Test",
        why = "Why",
        impactDescription = "Impact",
        approvalClass = ApprovalClass.A3_RELEVANT,
        targetModules = setOf("M01_TASKS"),
        impact = Impact.MEDIUM,
        alternatives = emptyList(),
        reversibility = Reversibility.REVERSIBLE,
        expiresAtMillis = expiresAtMillis,
        action = action
    )

    @Test
    fun `approveSuggestion rejeita sugestao expirada sem efeitos`() = runBlocking {
        val taskDao = MutableTaskDao(mutableMapOf(1L to Task(id = 1, title = "A")))
        val store = InMemoryApprovalStore()
        val engine = buildEngine(taskDao = taskDao, store = store)

        val s = suggestion(
            action = SuggestionAction.DeferItem("1", "M01_TASKS", 1),
            expiresAtMillis = fixedNow - 1
        )
        val result = engine.approveSuggestion(s)

        assertTrue("Deve falhar", result.isFailure)
        assertEquals("Tarefa nao deve ser alterada", 0L, taskDao.tasks_peek()!!.getValue(1L).dueDate ?: 0L)
        assertTrue("Sem decisao gravada", store.getAllDecisions().getOrThrow().isEmpty())
    }

    @Test
    fun `approveSuggestion grava APPROVED quando todas as mutacoes passam`() = runBlocking {
        val taskDao = MutableTaskDao(mutableMapOf(
            1L to Task(id = 1, title = "A", dueDate = fixedNow),
            2L to Task(id = 2, title = "B", dueDate = fixedNow)
        ))
        val store = InMemoryApprovalStore()
        val engine = buildEngine(taskDao = taskDao, store = store)

        val result = engine.approveSuggestion(suggestion(
            SuggestionAction.BatchDeferItems(listOf("1", "2"), "M01_TASKS", 1)
        ))

        assertTrue(result.isSuccess)
        val decisions = store.getAllDecisions().getOrThrow()
        assertEquals(1, decisions.size)
        assertEquals(ApprovalDecisionKind.APPROVED, decisions[0].decision)
    }

    @Test
    fun `approveSuggestion grava PARTIAL quando algumas mutacoes falham`() = runBlocking {
        val taskDao = MutableTaskDao(mutableMapOf(1L to Task(id = 1, title = "A")))
        val store = InMemoryApprovalStore()
        val engine = buildEngine(taskDao = taskDao, store = store)

        val result = engine.approveSuggestion(suggestion(
            SuggestionAction.BatchDeferItems(listOf("1", "2", "3"), "M01_TASKS", 1)
        ))

        assertTrue(result.isSuccess)
        val decisions = store.getAllDecisions().getOrThrow()
        assertEquals(1, decisions.size)
        assertEquals(ApprovalDecisionKind.PARTIAL, decisions[0].decision)
    }

    @Test
    fun `approveSuggestion grava FAILED quando todas as mutacoes falham`() = runBlocking {
        val store = InMemoryApprovalStore()
        val engine = buildEngine(taskDao = MutableTaskDao(), store = store)

        val result = engine.approveSuggestion(suggestion(
            SuggestionAction.BatchDeferItems(listOf("99", "100"), "M01_TASKS", 1)
        ))

        assertTrue(result.isSuccess)
        val decisions = store.getAllDecisions().getOrThrow()
        assertEquals(1, decisions.size)
        assertEquals(ApprovalDecisionKind.FAILED, decisions[0].decision)
    }

    @Test
    fun `rejectSuggestion grava REJECTED`() = runBlocking {
        val store = InMemoryApprovalStore()
        val engine = buildEngine(store = store)

        engine.rejectSuggestion(suggestion(SuggestionAction.DeferItem("1", "M01_TASKS", 1)))

        val decisions = store.getAllDecisions().getOrThrow()
        assertEquals(1, decisions.size)
        assertEquals(ApprovalDecisionKind.REJECTED, decisions[0].decision)
    }

    @Test
    fun `sweepExpired chama dao deleteExpired`() = runBlocking {
        val memoryDao = CountingMemoryDao()
        val engine = buildEngine(memoryDao = memoryDao)

        engine.sweepExpired()

        assertEquals(1, memoryDao.deleteExpiredCalls)
    }

    private fun MutableTaskDao.tasks_peek(): Map<Long, Task>? {
        val f = this.javaClass.getDeclaredField("tasks")
        f.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return f.get(this) as Map<Long, Task>
    }
}
