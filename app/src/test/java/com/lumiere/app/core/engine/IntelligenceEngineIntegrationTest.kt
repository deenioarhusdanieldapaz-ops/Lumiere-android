package com.lumiere.app.core.engine

import com.lumiere.app.core.contract.HabitContract
import com.lumiere.app.core.contract.HabitSnapshot
import com.lumiere.app.core.contract.TaskContract
import com.lumiere.app.core.contract.TaskSnapshot
import com.lumiere.app.core.contract.TaskState
import com.lumiere.app.core.memory.CoreMemory
import com.lumiere.app.core.memory.CoreMemoryDao
import com.lumiere.app.core.memory.CoreMemoryRepository
import com.lumiere.app.core.model.*
import com.lumiere.app.core.time.FixedTimeProvider
import com.lumiere.app.data.local.dao.HabitDao
import com.lumiere.app.data.local.dao.TaskDao
import com.lumiere.app.data.local.entity.Habit
import com.lumiere.app.data.local.entity.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

class IntelligenceEngineIntegrationTest {
    private val fixedNow = 1_700_000_000_000L
    private val dayMs = 86_400_000L
    private val tp = FixedTimeProvider(fixedNow, TimeZone.getTimeZone("UTC"))

    // ---------- Fakes ----------

    private class MutableTaskDao : TaskDao {
        val tasks = mutableMapOf<Long, Task>()
        override fun getAllActiveTasks(): Flow<List<Task>> = flowOf(tasks.values.toList())
        override suspend fun getTaskById(id: Long): Task? = tasks[id]
        override suspend fun completionTimestampsSince(since: Long): List<Long> = emptyList()
        override suspend fun insertTask(task: Task): Long { tasks[task.id] = task; return task.id }
        override suspend fun updateTask(task: Task) { tasks[task.id] = task }
        override suspend fun deleteTask(task: Task) { tasks.remove(task.id) }
    }

    private class MutableHabitDao : HabitDao {
        val habits = mutableMapOf<Long, Habit>()
        override fun getAllActiveHabits(): Flow<List<Habit>> = flowOf(habits.values.toList())
        override suspend fun getHabitById(id: Long): Habit? = habits[id]
        override suspend fun insertHabit(habit: Habit): Long { habits[habit.id] = habit; return habit.id }
        override suspend fun updateHabit(habit: Habit) { habits[habit.id] = habit }
        override suspend fun deleteHabit(habit: Habit) { habits.remove(habit.id) }
    }

    private class NoopMemoryDao : CoreMemoryDao {
        override suspend fun save(memory: CoreMemory) {}
        override suspend fun get(key: String): CoreMemory? = null
        override suspend fun getAllDecisions(): List<CoreMemory> = emptyList()
        override suspend fun deleteExpired(now: Long) {}
    }

    private fun buildEngine(
        taskContract: TaskContract,
        habitContract: HabitContract,
        taskDao: TaskDao = MutableTaskDao(),
        habitDao: HabitDao = MutableHabitDao(),
        store: ApprovalStore = InMemoryApprovalStore()
    ) = IntelligenceEngine(
        taskContract = taskContract,
        habitContract = habitContract,
        rules = RecommendationRules(tp),
        strictChecker = StrictConflictChecker(tp),
        approvalGateway = ApprovalGateway(store, tp),
        memory = CoreMemoryRepository(NoopMemoryDao(), tp),
        timeProvider = tp,
        taskDao = taskDao,
        habitDao = habitDao
    )

    // ---------- Testes ----------

    /**
     * Teste 1 — analyzeWeek sobre tarefa atrasada produz recomendação completa.
     */
    @Test
    fun `analyzeWeek produz recomendacao completa com facts e suggestions`() = runBlocking {
        val task = TaskSnapshot(
            id = "1", titleShort = "Atrasada", state = TaskState.ACTIVE,
            deadlineMillis = fixedNow - dayMs,
            priorityDeclared = 2, estimatedEffortMinutes = 30,
            dependencyIds = emptyList(), lastUpdatedMillis = 0L
        )
        val engine = buildEngine(FakeTaskContract(listOf(task)), FakeHabitContract(emptyList()))

        val rec = engine.analyzeWeek().getOrThrow()

        assertNotNull(rec)
        assertTrue("Deve ter facts", rec.facts.isNotEmpty())
        assertTrue("Deve ter suggestions", rec.suggestions.isNotEmpty())
        assertTrue(
            "Deve produzir BatchDeferItems para tarefas atrasadas",
            rec.suggestions.any { it.action is SuggestionAction.BatchDeferItems }
        )
        assertEquals(setOf("M01_TASKS"), rec.affectedModules)
    }

    /**
     * Teste 2 — approveSuggestion aplica mutação real + grava histórico.
     */
    @Test
    fun `approveSuggestion aplica mutacao em task e grava historico`() = runBlocking {
        val taskDao = MutableTaskDao()
        taskDao.tasks[1L] = Task(id = 1, title = "Tarefa", dueDate = fixedNow)
        val store = InMemoryApprovalStore()
        val engine = buildEngine(
            taskContract = FakeTaskContract(emptyList()),
            habitContract = FakeHabitContract(emptyList()),
            taskDao = taskDao,
            store = store
        )

        val suggestion = Suggestion(
            statement = "Reagendar", why = "Y", impactDescription = "Z",
            approvalClass = ApprovalClass.A3_RELEVANT,
            targetModules = setOf("M01_TASKS"),
            impact = Impact.MEDIUM, alternatives = emptyList(),
            reversibility = Reversibility.REVERSIBLE,
            expiresAtMillis = fixedNow + 3_600_000L,
            action = SuggestionAction.DeferItem("1", "M01_TASKS", 1)
        )

        val result = engine.approveSuggestion(suggestion)
        assertTrue("Deve ter sucesso", result.isSuccess)

        val updated = taskDao.getTaskById(1L)
        assertNotNull(updated)
        assertEquals("DueDate deve ter +1 dia", fixedNow + dayMs, updated!!.dueDate)

        val decisions = store.getAllDecisions().getOrThrow()
        assertEquals(1, decisions.size)
        assertEquals(ApprovalDecisionKind.APPROVED, decisions[0].decision)
    }

    /**
     * Teste 3 — detectConflicts deteta sobreposição real entre 2 tarefas de alta energia.
     */
    @Test
    fun `detectConflicts deteta sobreposicao de duas tarefas`() = runBlocking {
        // TaskSnapshot.startTime = deadline - effort; end = deadline
        // Tarefa A: deadline = now+30min, effort 60min -> start = now-30, end = now+30
        // Tarefa B: mesma janela -> overlap total
        val taskA = TaskSnapshot(
            id = "a", titleShort = "A", state = TaskState.ACTIVE,
            deadlineMillis = fixedNow + 30 * 60_000L,
            priorityDeclared = 2, estimatedEffortMinutes = 60,
            dependencyIds = emptyList(), lastUpdatedMillis = 0L,
            energy = 3
        )
        val taskB = TaskSnapshot(
            id = "b", titleShort = "B", state = TaskState.ACTIVE,
            deadlineMillis = fixedNow + 30 * 60_000L,
            priorityDeclared = 2, estimatedEffortMinutes = 60,
            dependencyIds = emptyList(), lastUpdatedMillis = 0L,
            energy = 3
        )
        val engine = buildEngine(
            FakeTaskContract(listOf(taskA, taskB)),
            FakeHabitContract(emptyList())
        )

        val conflicts = engine.detectConflicts().getOrThrow()

        assertTrue("Deve ter conflito", conflicts.isNotEmpty())
        assertTrue(
            "Deve ser TEMPORAL_OVERLAP",
            conflicts.any { it.kind == ConflictKind.TEMPORAL_OVERLAP }
        )
        assertTrue(
            "Deve ser severity 3 (energia combinada >= 5)",
            conflicts.any { it.kind == ConflictKind.TEMPORAL_OVERLAP && it.severity == 3 }
        )
    }

    /**
     * Teste 4 — pipeline completo: analyze -> approve -> getAllDecisions reflete o histórico.
     */
    @Test
    fun `pipeline completo analyze aprova e consulta historico`() = runBlocking {
        val taskDao = MutableTaskDao()
        taskDao.tasks[1L] = Task(id = 1, title = "Atrasada", dueDate = fixedNow - dayMs)
        val store = InMemoryApprovalStore()
        val task = TaskSnapshot(
            id = "1", titleShort = "Atrasada", state = TaskState.ACTIVE,
            deadlineMillis = fixedNow - dayMs,
            priorityDeclared = 2, estimatedEffortMinutes = 30,
            dependencyIds = emptyList(), lastUpdatedMillis = 0L
        )
        val engine = buildEngine(
            FakeTaskContract(listOf(task)),
            FakeHabitContract(emptyList()),
            taskDao = taskDao,
            store = store
        )

        // 1) analyze
        val rec = engine.analyzeWeek().getOrThrow()
        val suggestion = rec.suggestions.first { it.action is SuggestionAction.BatchDeferItems }

        // 2) approve
        val approved = engine.approveSuggestion(suggestion)
        assertTrue("Aprovação deve ter sucesso", approved.isSuccess)

        // 3) histórico reflete
        val history = engine.getAllDecisions().getOrThrow()
        assertEquals(1, history.size)
        assertEquals(ApprovalDecisionKind.APPROVED, history[0].decision)

        // 4) mutação foi aplicada
        val updated = taskDao.getTaskById(1L)
        assertNotNull(updated)
        assertEquals("dueDate deve ter avançado 1 dia", (fixedNow - dayMs) + dayMs, updated!!.dueDate)
    }
}
