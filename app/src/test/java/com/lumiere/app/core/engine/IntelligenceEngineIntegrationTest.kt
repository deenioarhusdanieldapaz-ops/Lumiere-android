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

    private class NoopMemoryDao : CoreMemoryDao {
        override suspend fun save(memory: CoreMemory) {}
        override suspend fun get(key: String): CoreMemory? = null
        override suspend fun getAllDecisions(): List<CoreMemory> = emptyList()
        override suspend fun deleteExpired(now: Long) {}
    }

    private fun buildEngine(
        taskContract: TaskContract,
        habitContract: HabitContract,
        store: ApprovalStore = InMemoryApprovalStore()
    ) = IntelligenceEngine(
        taskContract = taskContract,
        habitContract = habitContract,
        rules = RecommendationRules(tp),
        strictChecker = StrictConflictChecker(tp),
        approvalGateway = ApprovalGateway(store, tp),
        memory = CoreMemoryRepository(NoopMemoryDao(), tp),
        timeProvider = tp
    )

    @Test
    fun `analyzeWeek produz recomendacao completa com facts e suggestions`() = runBlocking {
        val task = TaskSnapshot(
            id = "1", titleShort = "Atrasada", state = TaskState.ACTIVE,
            deadlineMillis = fixedNow - dayMs, priorityDeclared = 2, estimatedEffortMinutes = 30,
            dependencyIds = emptyList(), lastUpdatedMillis = 0L
        )
        val engine = buildEngine(FakeTaskContract(listOf(task)), FakeHabitContract(emptyList()))
        val rec = engine.analyzeWeek().getOrThrow()

        assertNotNull(rec)
        assertTrue("Deve ter facts", rec.facts.isNotEmpty())
        assertTrue("Deve ter suggestions", rec.suggestions.isNotEmpty())
        assertTrue("Deve produzir BatchDeferItems", rec.suggestions.any { it.action is SuggestionAction.BatchDeferItems })
        assertEquals(setOf("M01_TASKS"), rec.affectedModules)
    }

    @Test
    fun `approveSuggestion aplica mutacao em task e grava historico`() = runBlocking {
        val taskSnapshot = TaskSnapshot(
            id = "1", titleShort = "Tarefa", state = TaskState.ACTIVE,
            deadlineMillis = fixedNow, priorityDeclared = 1, estimatedEffortMinutes = 30,
            dependencyIds = emptyList(), lastUpdatedMillis = 0L
        )
        val fakeContract = FakeTaskContract(listOf(taskSnapshot))
        val store = InMemoryApprovalStore()
        val engine = buildEngine(fakeContract, FakeHabitContract(emptyList()), store)

        val suggestion = Suggestion(
            statement = "Reagendar", why = "Y", impactDescription = "Z",
            approvalClass = ApprovalClass.A3_RELEVANT, targetModules = setOf("M01_TASKS"),
            impact = Impact.MEDIUM, alternatives = emptyList(), reversibility = Reversibility.REVERSIBLE,
            expiresAtMillis = fixedNow + 3_600_000L, action = SuggestionAction.DeferItem("1", "M01_TASKS", 1)
        )

        val result = engine.approveSuggestion(suggestion)
        assertTrue("Deve ter sucesso", result.isSuccess)

        val updatedDate = fakeContract.updatedDueDates["1"]
        assertNotNull(updatedDate)
        assertEquals("DueDate deve ter +1 dia", fixedNow + dayMs, updatedDate)

        val decisions = store.getAllDecisions().getOrThrow()
        assertEquals(1, decisions.size)
        assertEquals(ApprovalDecisionKind.APPROVED, decisions[0].decision)
    }

    @Test
    fun `detectConflicts deteta sobreposicao de duas tarefas`() = runBlocking {
        val taskA = TaskSnapshot(
            id = "a", titleShort = "A", state = TaskState.ACTIVE, deadlineMillis = fixedNow + 30 * 60_000L,
            priorityDeclared = 2, estimatedEffortMinutes = 60, dependencyIds = emptyList(), lastUpdatedMillis = 0L, energy = 3
        )
        val taskB = TaskSnapshot(
            id = "b", titleShort = "B", state = TaskState.ACTIVE, deadlineMillis = fixedNow + 30 * 60_000L,
            priorityDeclared = 2, estimatedEffortMinutes = 60, dependencyIds = emptyList(), lastUpdatedMillis = 0L, energy = 3
        )
        val engine = buildEngine(FakeTaskContract(listOf(taskA, taskB)), FakeHabitContract(emptyList()))
        val conflicts = engine.detectConflicts().getOrThrow()

        assertTrue("Deve ter conflito", conflicts.isNotEmpty())
        assertTrue("Deve ser TEMPORAL_OVERLAP", conflicts.any { it.kind == ConflictKind.TEMPORAL_OVERLAP })
        assertTrue("Deve ser severity 3", conflicts.any { it.kind == ConflictKind.TEMPORAL_OVERLAP && it.severity == 3 })
    }

    @Test
    fun `pipeline completo analyze aprova e consulta historico`() = runBlocking {
        val taskSnapshot = TaskSnapshot(
            id = "1", titleShort = "Atrasada", state = TaskState.ACTIVE,
            deadlineMillis = fixedNow - dayMs, priorityDeclared = 2, estimatedEffortMinutes = 30,
            dependencyIds = emptyList(), lastUpdatedMillis = 0L
        )
        val fakeContract = FakeTaskContract(listOf(taskSnapshot))
        val store = InMemoryApprovalStore()
        val engine = buildEngine(fakeContract, FakeHabitContract(emptyList()), store)

        val rec = engine.analyzeWeek().getOrThrow()
        val suggestion = rec.suggestions.first { it.action is SuggestionAction.BatchDeferItems }
        val result = engine.approveSuggestion(suggestion)

        assertTrue(result.isSuccess)
        val decisions = engine.getAllDecisions().getOrThrow()
        assertEquals(1, decisions.size)
        assertEquals(ApprovalDecisionKind.APPROVED, decisions[0].decision)
        assertTrue(fakeContract.updatedDueDates.containsKey("1"))
    }
}
