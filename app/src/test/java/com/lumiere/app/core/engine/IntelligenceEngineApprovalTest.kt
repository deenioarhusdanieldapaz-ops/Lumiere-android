package com.lumiere.app.core.engine

import com.lumiere.app.core.contract.TaskState
import com.lumiere.app.core.contract.TaskSnapshot
import com.lumiere.app.core.memory.CoreMemory
import com.lumiere.app.core.memory.CoreMemoryDao
import com.lumiere.app.core.memory.CoreMemoryRepository
import com.lumiere.app.core.model.*
import com.lumiere.app.core.time.TimeProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IntelligenceEngineApprovalTest {
    private val fixedNow = 1_700_000_000_000L
    private val timeProvider = object : TimeProvider {
        override fun currentTimeMillis(): Long = fixedNow
    }

    private class CountingMemoryDao : CoreMemoryDao {
        var deleteExpiredCalls = 0
        override suspend fun save(memory: CoreMemory) {}
        override suspend fun get(key: String): CoreMemory? = null
        override suspend fun getAllDecisions(): List<CoreMemory> = emptyList()
        override suspend fun deleteExpired(now: Long) { deleteExpiredCalls++ }
    }

    private fun buildEngine(
        taskContract: FakeTaskContract = FakeTaskContract(),
        memoryDao: CoreMemoryDao = CountingMemoryDao(),
        store: ApprovalStore = InMemoryApprovalStore()
    ) = IntelligenceEngine(
        taskContract = taskContract,
        habitContract = FakeHabitContract(emptyList()),
        rules = RecommendationRules(timeProvider),
        strictChecker = StrictConflictChecker(timeProvider),
        approvalGateway = ApprovalGateway(store, timeProvider),
        memory = CoreMemoryRepository(memoryDao, timeProvider),
        timeProvider = timeProvider
    )

    private fun suggestion(
        action: SuggestionAction,
        expiresAtMillis: Long? = fixedNow + 3_600_000L
    ) = Suggestion(
        id = "s1", statement = "Test", why = "Why",
        impactDescription = "Impact", approvalClass = ApprovalClass.A3_RELEVANT,
        targetModules = setOf("M01_TASKS"), impact = Impact.MEDIUM,
        alternatives = emptyList(), reversibility = Reversibility.REVERSIBLE,
        expiresAtMillis = expiresAtMillis, action = action
    )

    @Test
    fun `approveSuggestion rejeita sugestao expirada sem efeitos`() = runBlocking {
        val task = TaskSnapshot(id = "1", titleShort = "A", state = TaskState.ACTIVE, deadlineMillis = fixedNow, priorityDeclared = 1, estimatedEffortMinutes = 30, dependencyIds = emptyList(), lastUpdatedMillis = 0L)
        val fakeContract = FakeTaskContract(listOf(task))
        val store = InMemoryApprovalStore()
        val engine = buildEngine(taskContract = fakeContract, store = store)

        val s = suggestion(action = SuggestionAction.DeferItem("1", "M01_TASKS", 1), expiresAtMillis = fixedNow - 1)
        val result = engine.approveSuggestion(s)

        assertTrue("Deve falhar", result.isFailure)
        assertTrue("Tarefa nao deve ser alterada", fakeContract.updatedDueDates.isEmpty())
        assertTrue("Sem decisao gravada", store.getAllDecisions().getOrThrow().isEmpty())
    }

    @Test
    fun `approveSuggestion grava APPROVED quando todas as mutacoes passam`() = runBlocking {
        val task1 = TaskSnapshot(id = "1", titleShort = "A", state = TaskState.ACTIVE, deadlineMillis = fixedNow, priorityDeclared = 1, estimatedEffortMinutes = 30, dependencyIds = emptyList(), lastUpdatedMillis = 0L)
        val task2 = TaskSnapshot(id = "2", titleShort = "B", state = TaskState.ACTIVE, deadlineMillis = fixedNow, priorityDeclared = 1, estimatedEffortMinutes = 30, dependencyIds = emptyList(), lastUpdatedMillis = 0L)
        val fakeContract = FakeTaskContract(listOf(task1, task2))
        val store = InMemoryApprovalStore()
        val engine = buildEngine(taskContract = fakeContract, store = store)

        val result = engine.approveSuggestion(suggestion(SuggestionAction.BatchDeferItems(listOf("1", "2"), "M01_TASKS", 1)))

        assertTrue(result.isSuccess)
        val decisions = store.getAllDecisions().getOrThrow()
        assertEquals(1, decisions.size)
        assertEquals(ApprovalDecisionKind.APPROVED, decisions[0].decision)
        assertEquals(fixedNow + 86_400_000L, fakeContract.updatedDueDates["1"])
        assertEquals(fixedNow + 86_400_000L, fakeContract.updatedDueDates["2"])
    }

    @Test
    fun `approveSuggestion grava PARTIAL quando algumas mutacoes falham`() = runBlocking {
        val task1 = TaskSnapshot(id = "1", titleShort = "A", state = TaskState.ACTIVE, deadlineMillis = fixedNow, priorityDeclared = 1, estimatedEffortMinutes = 30, dependencyIds = emptyList(), lastUpdatedMillis = 0L)
        val fakeContract = FakeTaskContract(listOf(task1))
        val store = InMemoryApprovalStore()
        val engine = buildEngine(taskContract = fakeContract, store = store)

        val result = engine.approveSuggestion(suggestion(SuggestionAction.BatchDeferItems(listOf("1", "2", "3"), "M01_TASKS", 1)))

        assertTrue(result.isSuccess)
        val decisions = store.getAllDecisions().getOrThrow()
        assertEquals(1, decisions.size)
        assertEquals(ApprovalDecisionKind.PARTIAL, decisions[0].decision)
    }

    @Test
    fun `approveSuggestion grava FAILED quando todas as mutacoes falham`() = runBlocking {
        val store = InMemoryApprovalStore()
        val engine = buildEngine(store = store) // FakeTaskContract empty
        val result = engine.approveSuggestion(suggestion(SuggestionAction.BatchDeferItems(listOf("99", "100"), "M01_TASKS", 1)))

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
}
