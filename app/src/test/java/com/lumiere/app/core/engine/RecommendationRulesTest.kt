package com.lumiere.app.core.engine

import com.lumiere.app.core.model.Schedulable
import com.lumiere.app.core.model.SuggestionAction
import com.lumiere.app.core.time.TimeProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecommendationRulesTest {
    private val fixedNow = 1_700_000_000_000L
    private val timeProvider = object : TimeProvider {
        override fun currentTimeMillis(): Long = fixedNow
    }

    class FakeSchedulable(
        override val id: String, override val moduleId: String, override val title: String,
        override val startTimeMillis: Long?, override val durationMinutes: Int?,
        override val energyCost: Int, override val priority: Int,
        override val deadlineMillis: Long?
    ) : Schedulable

    @Test
    fun `deve gerar BatchDeferItems para tarefas atrasadas`() {
        val rules = RecommendationRules(timeProvider)
        
        // Duas tarefas com deadline no passado
        val t1 = FakeSchedulable("t1", "M01_TASKS", "Atrasada 1", fixedNow - 1000, 60, 1, 1, fixedNow - 1000)
        val t2 = FakeSchedulable("t2", "M01_TASKS", "Atrasada 2", fixedNow - 2000, 60, 1, 1, fixedNow - 2000)

        val suggestions = rules.buildSuggestions(listOf(t1, t2), emptyList())
        
        val overdueSuggestion = suggestions.find { it.statement.contains("atrasada") }
        assertTrue(overdueSuggestion != null)
        assertTrue(overdueSuggestion!!.action is SuggestionAction.BatchDeferItems)
        
        val action = overdueSuggestion.action as SuggestionAction.BatchDeferItems
        assertEquals(2, action.itemIds.size)
    }

    @Test
    fun `deve gerar sugestao de mudar horario do habito em colisao`() {
        val rules = RecommendationRules(timeProvider)
        
        val task = FakeSchedulable("t1", "M01_TASKS", "Reuniao", fixedNow, 60, 2, 2, fixedNow + 60 * 60_000L)
        val habit = FakeSchedulable("h1", "M02_HABITS", "Ler", fixedNow + 30 * 60_000L, 30, 2, 1, fixedNow + 60 * 60_000L)

        val suggestions = rules.buildSuggestions(listOf(task, habit), emptyList())
        
        val habitSuggestion = suggestions.find { it.statement.contains("Mudar o horário") }
        assertTrue(habitSuggestion != null)
        assertTrue(habitSuggestion!!.action is SuggestionAction.RescheduleItem)
    }
}
