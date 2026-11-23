package com.lumiere.app.core.engine

import com.lumiere.app.core.model.Schedulable
import com.lumiere.app.core.time.TimeProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StrictConflictCheckerTest {
    private val fixedNow = 1_700_000_000_000L
    private val timeProvider = object : TimeProvider {
        override fun currentTimeMillis(): Long = fixedNow
    }

    class FakeSchedulable(
        override val id: String, override val moduleId: String, override val title: String,
        override val startTimeMillis: Long?, override val durationMinutes: Int?,
        override val energyCost: Int, override val priority: Int,
        override val deadlineMillis: Long? = null
    ) : Schedulable

    @Test
    fun `deve detetar sobreposicao cross-module`() {
        val checker = StrictConflictChecker(timeProvider)
        
        // Tarefa das 10:00 às 11:00 (60 min)
        val task = FakeSchedulable("t1", "M01_TASKS", "Programar", fixedNow, 60, 2, 2)
        // Habito das 10:30 às 11:00 (30 min)
        val habit = FakeSchedulable("h1", "M02_HABITS", "Ler", fixedNow + 30 * 60_000L, 30, 2, 1)

        val conflicts = checker.detect(listOf(task, habit), emptyList())
        
        assertEquals(1, conflicts.size)
        assertEquals(ConflictKind.TEMPORAL_OVERLAP, conflicts[0].kind)
        assertTrue(conflicts[0].details.contains("sobrepõe-se"))
    }

    @Test
    fun `nao deve detetar sobreposicao em horarios diferentes`() {
        val checker = StrictConflictChecker(timeProvider)
        
        val task = FakeSchedulable("t1", "M01_TASKS", "Programar", fixedNow, 60, 2, 2)
        // Habito comeca quando a tarefa termina
        val habit = FakeSchedulable("h1", "M02_HABITS", "Ler", fixedNow + 60 * 60_000L, 30, 2, 1)

        val conflicts = checker.detect(listOf(task, habit), emptyList())
        
        assertTrue(conflicts.isEmpty())
    }

    @Test
    fun `deve detetar sobrecarga diaria`() {
        val checker = StrictConflictChecker(timeProvider)
        
        // 3 tarefas de 4 horas cada = 12h
        val t1 = FakeSchedulable("t1", "M01_TASKS", "Trabalho 1", fixedNow, 240, 2, 2)
        val t2 = FakeSchedulable("t2", "M01_TASKS", "Trabalho 2", fixedNow, 240, 2, 2)
        val t3 = FakeSchedulable("t3", "M01_TASKS", "Trabalho 3", fixedNow, 240, 2, 2)

        val conflicts = checker.detect(listOf(t1, t2, t3), emptyList())
        
        assertTrue(conflicts.any { it.kind == ConflictKind.CAPACITY_EXCEEDED_DAILY })
    }
}
