package com.lumiere.app.core.engine

import com.lumiere.app.core.model.Schedulable
import com.lumiere.app.core.time.TimeProvider
import org.junit.Assert.assertTrue
import org.junit.Test

class StrictConflictCheckerCapacityTest {
    private val fixedNow = 1_700_000_000_000L
    private val timeProvider = object : TimeProvider {
        override fun currentTimeMillis(): Long = fixedNow
    }
    private val checker = StrictConflictChecker(timeProvider)

    private class FakeSchedulable(
        override val id: String, override val moduleId: String, override val title: String,
        override val startTimeMillis: Long?, override val durationMinutes: Int?,
        override val energyCost: Int, override val priority: Int,
        override val deadlineMillis: Long? = null
    ) : Schedulable

    @Test
    fun `capacidade semanal conta tarefas sem durationMinutes como 30min`() {
        // 81 * 30 = 2430 min > 2400 (limite semanal).
        // Com ?:0 seria 0 min e nenhum conflito seria detetado.
        val tasks = (1..81).map { i ->
            FakeSchedulable("t$i", "M01_TASKS", "Task $i", null, null, 2, 2)
        }
        val conflicts = checker.detect(tasks, emptyList())
        assertTrue(conflicts.any { it.kind == ConflictKind.CAPACITY_EXCEEDED_WEEKLY })
    }

    @Test
    fun `capacidade diaria conta tarefas sem durationMinutes como 30min`() {
        // 17 * 30 = 510 min > 480 (limite diario). Mesmo dia.
        val tasks = (1..17).map { i ->
            FakeSchedulable("t$i", "M01_TASKS", "Task $i", fixedNow, null, 2, 2)
        }
        val conflicts = checker.detect(tasks, emptyList())
        assertTrue(conflicts.any { it.kind == ConflictKind.CAPACITY_EXCEEDED_DAILY })
    }
}
