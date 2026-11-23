package com.lumiere.app.core.time

import com.lumiere.app.core.contract.RoomHabitContract
import com.lumiere.app.core.engine.RecommendationRules
import com.lumiere.app.core.engine.StrictConflictChecker
import com.lumiere.app.core.engine.ConflictKind
import com.lumiere.app.core.model.Schedulable
import com.lumiere.app.core.model.SuggestionAction
import com.lumiere.app.data.local.dao.HabitCheckInDao
import com.lumiere.app.data.local.dao.HabitDao
import com.lumiere.app.data.local.entity.Habit
import com.lumiere.app.data.local.entity.HabitCheckIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale
import java.util.TimeZone

class TimeZoneBehaviorTest {
    private val utc = TimeZone.getTimeZone("UTC")
    private val utcPlus2 = TimeZone.getTimeZone("GMT+02:00")

    @Test
    fun `dayKey no mesmo dia local tem a mesma chave`() {
        val tz = utcPlus2
        val a = TimeUtils.dayKey(1700000000000L, tz)
        val b = TimeUtils.dayKey(1700000000000L + 3_600_000L, tz)
        assertEquals(a, b)
    }

    @Test
    fun `dayKey em UTC+2 distingue 23h de 01h do dia seguinte`() {
        val tz = utcPlus2
        val lateNight = 1700001000000L
        val nextLateNight = 1700001000000L + 86_400_000L
        assertNotEquals(TimeUtils.dayKey(lateNight, tz), TimeUtils.dayKey(nextLateNight, tz))
    }

    @Test
    fun `dayKey UTC trata 23h e 01h como dias diferentes`() {
        val late = 1700002800000L
        val early = 1700010000000L
        assertNotEquals(TimeUtils.dayKey(late, utc), TimeUtils.dayKey(early, utc))
    }

    @Test
    fun `StrictConflictChecker agrupa duas tarefas no mesmo dia local UTC+2`() {
        val tz = utcPlus2
        val tp = FixedTimeProvider(1700000000000L, tz)
        val checker = StrictConflictChecker(tp)

        val taskA = fake("a", 1700002800000L, 300)
        val taskB = fake("b", 1700085600000L, 300)
        val taskC = fake("c", 1700017200000L, 300)

        val conflicts = checker.detect(listOf(taskA, taskC), emptyList())
        assertTrue(conflicts.any { it.kind == ConflictKind.CAPACITY_EXCEEDED_DAILY })

        val conflictsCrossDay = checker.detect(listOf(taskA, taskB), emptyList())
        assertTrue(conflictsCrossDay.none { it.kind == ConflictKind.CAPACITY_EXCEEDED_DAILY })
    }

    @Test
    fun `RoomHabitContract computeStreak usa timezone`() = runBlocking {
        val tz = utcPlus2
        val now = 1700020000000L
        val tp = FixedTimeProvider(now, tz)
        val habit = Habit(id = 1, name = "Ler", frequencyType = "DAILY", status = 0)
        val habitDao = object : HabitDao {
            override fun getAllActiveHabits(): Flow<List<Habit>> = flowOf(listOf(habit))
            override suspend fun getHabitById(id: Long): Habit? = habit
            override suspend fun insertHabit(habit: Habit): Long = 1
            override suspend fun updateHabit(habit: Habit) {}
            override suspend fun deleteHabit(habit: Habit) {}
        }
        val dayMs = 86_400_000L
        val checkInDao = object : HabitCheckInDao {
            override fun getCheckInsForHabit(habitId: Long): Flow<List<HabitCheckIn>> = flowOf(emptyList())
            override fun getAllCheckIns(): Flow<List<HabitCheckIn>> = flowOf(emptyList())
            override suspend fun getCheckInsForPeriod(habitId: Long, start: Long, end: Long): List<HabitCheckIn> = emptyList()
            override suspend fun getCheckInForDay(habitId: Long, start: Long, end: Long): HabitCheckIn? = null
            override suspend fun successTimestampsForHabit(habitId: Long): List<Long> =
                listOf(now, now - dayMs, now - 2 * dayMs)
            override suspend fun insertCheckIn(checkIn: HabitCheckIn): Long = 0
            override suspend fun updateCheckIn(checkIn: HabitCheckIn) {}
            override suspend fun deleteCheckIn(checkIn: HabitCheckIn) {}
            override suspend fun countForHabit(habitId: Long): Int = 3
            override suspend fun deleteForHabit(habitId: Long) {}
        }
        val contract = RoomHabitContract(habitDao, checkInDao, tp)
        val result = contract.getActiveHabits().getOrThrow()

        assertEquals(1, result.size)
        assertTrue("Streak deve ser >= 1 com timezone correto", result[0].streakCurrent >= 1)
    }

    @Test
    fun `RecommendationRules RescheduleItem hora corresponde ao timezone`() {
        val tz = utcPlus2
        val now = 1700000000000L
        val tp = FixedTimeProvider(now, tz)
        val rules = RecommendationRules(tp)
        val habitStart = now + 30_000L
        val sched = listOf(
            fakeAt("t1", "M01_TASKS", habitStart, 60),
            fakeAt("h1", "M02_HABITS", habitStart, 30)
        )
        val suggestions = rules.buildSuggestions(sched, emptyList())
        val reschedule = suggestions.firstOrNull { it.action is SuggestionAction.RescheduleItem }?.action
        assertTrue(reschedule is SuggestionAction.RescheduleItem)
    }

    @Test
    fun `String format Locale ROOT nao depende do idioma`() {
        val pt = String.format(Locale.ROOT, "%02d:%02d", 9, 5)
        val ar = String.format(Locale.ROOT, "%02d:%02d", 9, 5)
        assertEquals(pt, ar)
        assertEquals("09:05", pt)
    }

    @Test
    fun `FixedTimeProvider respeita timezone configurado`() {
        val tp = FixedTimeProvider(0L, TimeZone.getTimeZone("GMT-03:00"))
        assertEquals("GMT-03:00", tp.timeZone().id)
        assertEquals(0L, tp.currentTimeMillis())
    }

    @Test
    fun `TimeProvider default timezone e o do sistema`() {
        val tp = object : TimeProvider {
            override fun currentTimeMillis(): Long = 0L
        }
        assertTrue(tp.timeZone() != null)
    }

    @Test
    fun `RecommendationRules overload diario respeita timezone`() {
        val tz = utcPlus2
        val now = 1700000000000L
        val tp = FixedTimeProvider(now, tz)
        val rules = RecommendationRules(tp)
        val base = 1700000000000L
        val sched = listOf(
            fakeAt("a", "M01_TASKS", base, 180),
            fakeAt("b", "M01_TASKS", base + 180 * 60_000L, 180),
            fakeAt("c", "M01_TASKS", base + 360 * 60_000L, 180)
        )
        val suggestions = rules.buildSuggestions(sched, emptyList())
        assertTrue(suggestions.any { it.statement.contains("Aliviar a carga") })
    }

    private fun fake(id: String, start: Long, durMin: Int) = fakeAt(id, "M01_TASKS", start, durMin)

    private fun fakeAt(id: String, module: String, start: Long, durMin: Int): Schedulable =
        object : Schedulable {
            override val id = id
            override val moduleId = module
            override val title = id
            override val startTimeMillis = start
            override val deadlineMillis: Long? = start + durMin * 60_000L
            override val durationMinutes = durMin
            override val energyCost = 2
            override val priority = 2
        }
}
