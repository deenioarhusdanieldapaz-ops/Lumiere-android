package com.lumiere.app.core.engine

import com.lumiere.app.core.contract.HabitContract
import com.lumiere.app.core.contract.HabitSnapshot
import com.lumiere.app.core.contract.TaskContract
import com.lumiere.app.core.contract.TaskSnapshot
import com.lumiere.app.core.contract.TaskState
import com.lumiere.app.core.memory.CoreMemory
import com.lumiere.app.core.memory.CoreMemoryDao
import com.lumiere.app.core.memory.CoreMemoryRepository
import com.lumiere.app.core.time.TimeProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IntelligenceEngineResilienceTest {
    private val fixedNow = 1_700_000_000_000L
    private val timeProvider = object : TimeProvider {
        override fun currentTimeMillis(): Long = fixedNow
    }

    private fun failingTaskContract(): TaskContract = object : TaskContract {
        override val contractVersion = "test"
        override suspend fun getActiveTasks(windowDays: Int): Result<List<TaskSnapshot>> =
            Result.failure(RuntimeException("task source down"))
        override suspend fun getTask(id: String): Result<TaskSnapshot?> =
            Result.failure(RuntimeException("task source down"))
        override suspend fun updateTaskDueDate(id: String, newDueDate: Long): Result<Unit> =
            Result.failure(RuntimeException("task source down"))
    }

    private fun failingHabitContract(): HabitContract = object : HabitContract {
        override val contractVersion = "test"
        override suspend fun getActiveHabits(): Result<List<HabitSnapshot>> =
            Result.failure(RuntimeException("habit source down"))
        override suspend fun getHabit(id: String): Result<HabitSnapshot?> =
            Result.failure(RuntimeException("habit source down"))
    }

    private fun workingTaskContract(): TaskContract =
        FakeTaskContract(listOf(TaskSnapshot(
            id = "1", titleShort = "Tarefa Atrasada", state = TaskState.ACTIVE,
            deadlineMillis = fixedNow - 1000, priorityDeclared = 1,
            estimatedEffortMinutes = 30, dependencyIds = emptyList(),
            lastUpdatedMillis = 0L
        )))

    private fun memoryWithFailingDao(): CoreMemoryRepository = CoreMemoryRepository(
        object : CoreMemoryDao {
            override suspend fun save(memory: CoreMemory) { throw RuntimeException("memory down") }
            override suspend fun get(key: String): CoreMemory? = null
            override suspend fun getAllDecisions(): List<CoreMemory> = emptyList()
            override suspend fun deleteExpired(now: Long) {}
        }, timeProvider
    )

    private fun buildEngine(
        taskContract: TaskContract = workingTaskContract(),
        habitContract: HabitContract = FakeHabitContract(emptyList()),
        memory: CoreMemoryRepository = CoreMemoryRepository(
            object : CoreMemoryDao {
                override suspend fun save(memory: CoreMemory) {}
                override suspend fun get(key: String): CoreMemory? = null
                override suspend fun getAllDecisions(): List<CoreMemory> = emptyList()
                override suspend fun deleteExpired(now: Long) {}
            }, timeProvider
        )
    ) = IntelligenceEngine(
        taskContract = taskContract,
        habitContract = habitContract,
        rules = RecommendationRules(timeProvider),
        strictChecker = StrictConflictChecker(timeProvider),
        approvalGateway = ApprovalGateway(InMemoryApprovalStore(), timeProvider),
        memory = memory,
        timeProvider = timeProvider
    )

    @Test
    fun `analyzeWeek propaga erro quando taskContract falha`() = runBlocking {
        val engine = buildEngine(taskContract = failingTaskContract())
        val result = engine.analyzeWeek()
        assertTrue(result.isFailure)
    }

    @Test
    fun `analyzeWeek propaga erro quando habitContract falha`() = runBlocking {
        val engine = buildEngine(habitContract = failingHabitContract())
        val result = engine.analyzeWeek()
        assertTrue(result.isFailure)
    }

    @Test
    fun `analyzeWeek devolve sucesso quando memory put falha`() = runBlocking {
        val engine = buildEngine(memory = memoryWithFailingDao())
        val result = engine.analyzeWeek()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `analyzeWeek devolve recomendacao valida mesmo com memory put a falhar`() = runBlocking {
        val engine = buildEngine(memory = memoryWithFailingDao())
        val recommendation = engine.analyzeWeek().getOrThrow()
        assertEquals("Organizar a semana", recommendation.goal)
        assertTrue(recommendation.suggestions.isNotEmpty())
    }
}
