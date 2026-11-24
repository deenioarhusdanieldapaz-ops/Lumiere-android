package com.lumiere.app.core.engine

import com.lumiere.app.core.contract.*
import com.lumiere.app.core.model.Sensitivity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class IntelligenceEngineTest {
    private val timeProvider = object : com.lumiere.app.core.time.TimeProvider {
        override fun currentTimeMillis(): Long = 1_700_000_000_000L // Tempo fixo para testes
    }

    @Test
    fun `motor deve gerar sugestao para tarefa atrasada`() = runBlocking {
        val rules = RecommendationRules(timeProvider)
        val engine = IntelligenceEngine(
            taskContract = FakeTaskContract(listOf(
                TaskSnapshot(
                    id = "1", titleShort = "Atrasada", state = TaskState.ACTIVE,
                    deadlineMillis = 1_600_000_000_000L, // No passado
                    priorityDeclared = 1, estimatedEffortMinutes = 30,
                    dependencyIds = emptyList(), lastUpdatedMillis = 0L
                )
            )),
            habitContract = FakeHabitContract(emptyList()),
            rules = rules,
            strictChecker = StrictConflictChecker(timeProvider),
            approvalGateway = ApprovalGateway(InMemoryApprovalStore(), timeProvider),
            memory = com.lumiere.app.core.memory.CoreMemoryRepository(
                object : com.lumiere.app.core.memory.CoreMemoryDao {
                    override suspend fun save(memory: com.lumiere.app.core.memory.CoreMemory) {}
                    override suspend fun get(key: String): com.lumiere.app.core.memory.CoreMemory? = null
                    override suspend fun getAllDecisions(): List<com.lumiere.app.core.memory.CoreMemory> = emptyList()
                    override suspend fun deleteExpired(now: Long) {}
                }, timeProvider
            ),
            timeProvider = timeProvider,
        )

        val result = engine.analyzeWeek().getOrThrow()
        assertTrue("Deve gerar pelo menos uma sugestão", result.suggestions.isNotEmpty())
    }
}
