package com.lumiere.app.core.engine

import com.lumiere.app.core.contract.HabitFrequency
import com.lumiere.app.core.contract.HabitSnapshot
import com.lumiere.app.core.contract.TaskSnapshot
import com.lumiere.app.core.contract.TaskState
import com.lumiere.app.core.model.*
import com.lumiere.app.core.time.FixedTimeProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeterministicIdsAndBehaviorTest {
    private val fixedNow = 1_700_000_000_000L
    private val tp = FixedTimeProvider(fixedNow)
    private val rules = RecommendationRules(tp)

    @Test
    fun `Suggestion id e estavel entre chamadas com mesmos argumentos`() {
        val a = suggestionWith(SuggestionAction.DeferItem("1", "M01_TASKS", 1))
        val b = suggestionWith(SuggestionAction.DeferItem("1", "M01_TASKS", 1))
        assertEquals(a.id, b.id)
    }

    @Test
    fun `Suggestion id e diferente entre argumentos diferentes`() {
        val a = suggestionWith(SuggestionAction.DeferItem("1", "M01_TASKS", 1))
        val b = suggestionWith(SuggestionAction.DeferItem("2", "M01_TASKS", 1))
        assertNotEquals(a.id, b.id)
    }

    @Test
    fun `Fact id e estavel`() {
        val src = FactSource.FromModule("M01", "r1", "1.0", 0)
        val a = Fact(statement = "S", source = src)
        val b = Fact(statement = "S", source = src)
        assertEquals(a.id, b.id)
    }

    @Test
    fun `Inference id e estavel`() {
        val a = Inference("S", InferenceMethod.CORRELATION, Confidence.CALIBRATED, emptyList())
        val b = Inference("S", InferenceMethod.CORRELATION, Confidence.CALIBRATED, emptyList())
        assertEquals(a.id, b.id)
    }

    @Test
    fun `ShortenDuration gerado quando successRate30d menor que 0_4`() {
        val habit = habitSnapshot("h1", "Ler", 0.3f, HabitFrequency.DAILY)
        val suggestions = rules.buildSuggestions(listOf(habit), emptyList())
        assertTrue(suggestions.any { it.action is SuggestionAction.ShortenDuration })
    }

    @Test
    fun `ShortenDuration nao gerado quando successRate30d maior ou igual a 0_4`() {
        val habit = habitSnapshot("h1", "Ler", 0.5f, HabitFrequency.DAILY)
        val suggestions = rules.buildSuggestions(listOf(habit), emptyList())
        assertTrue(suggestions.none { it.action is SuggestionAction.ShortenDuration })
    }

    @Test
    fun `ChangeFrequency gerado quando successRate30d menor que 0_2 e DAILY`() {
        val habit = habitSnapshot("h1", "Correr", 0.1f, HabitFrequency.DAILY)
        val suggestions = rules.buildSuggestions(listOf(habit), emptyList())
        assertTrue(suggestions.any { it.action is SuggestionAction.ChangeFrequency })
    }

    @Test
    fun `ChangeFrequency nao gerado quando frequency nao e DAILY`() {
        val habit = habitSnapshot("h1", "Correr", 0.1f, HabitFrequency.WEEKLY)
        val suggestions = rules.buildSuggestions(listOf(habit), emptyList())
        assertTrue(suggestions.none { it.action is SuggestionAction.ChangeFrequency })
    }

    @Test
    fun `Burnout inference produz sugestao BatchDeferItems`() {
        val tasks = (1L..3L).map { id ->
            TaskSnapshot(
                id = id.toString(), titleShort = "T$id", state = TaskState.ACTIVE,
                deadlineMillis = null, priorityDeclared = 1, estimatedEffortMinutes = 30,
                dependencyIds = emptyList(), lastUpdatedMillis = 0L,
                energy = 4, importance = 1
            )
        }
        val facts = rules.buildFacts(tasks)
        val inferences = rules.buildInferences(tasks, facts)
        assertTrue("Deve ter inferencia Burnout", inferences.any { it.statement.contains("Burnout") })
        val suggestions = rules.buildSuggestions(tasks, inferences)
        assertTrue("Deve gerar sugestao de aliviar", suggestions.any { it.statement.contains("Aliviar carga") })
    }

    @Test
    fun `actionKey diferencia acoes mesmo com statement igual`() {
        val keyA = DeterministicIds.actionKey(SuggestionAction.DeferItem("1", "M01_TASKS", 1))
        val keyB = DeterministicIds.actionKey(SuggestionAction.RescheduleItem("1", "M02_HABITS", 0L))
        assertNotEquals(keyA, keyB)
        val idA = DeterministicIds.forSuggestion("S", setOf("M01_TASKS"), keyA)
        val idB = DeterministicIds.forSuggestion("S", setOf("M02_HABITS"), keyB)
        assertNotEquals(idA, idB)
    }

    private fun suggestionWith(action: SuggestionAction) = Suggestion(
        statement = "S",
        why = "Y",
        impactDescription = "Z",
        approvalClass = ApprovalClass.A3_RELEVANT,
        targetModules = setOf("M01_TASKS"),
        impact = Impact.MEDIUM,
        alternatives = emptyList(),
        reversibility = Reversibility.REVERSIBLE,
        expiresAtMillis = null,
        action = action
    )

    private fun habitSnapshot(
        id: String, name: String, successRate: Float, freq: HabitFrequency
    ): HabitSnapshot = HabitSnapshot(
        id = id, nameShort = name, frequency = freq,
        targetPerPeriod = 1, checkInsLast7d = 0, streakCurrent = 0,
        successRate30d = successRate, lastUpdatedMillis = 0L,
        durationMinutes = 30
    )
}
