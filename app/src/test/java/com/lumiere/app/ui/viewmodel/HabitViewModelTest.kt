package com.lumiere.app.ui.viewmodel

import com.lumiere.app.data.local.entity.Habit
import com.lumiere.app.data.local.entity.HabitCheckIn
import com.lumiere.app.data.repository.HabitRepository
import com.lumiere.app.domain.usecase.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class HabitViewModelTest {

    private lateinit var repository: HabitRepository
    private lateinit var viewModel: HabitViewModel

    @BeforeEach
    fun setup() {
        repository = mockk(relaxed = true)
        viewModel = HabitViewModel(repository)
    }

    // ===== BUG 400% — quantitativo NÃO deve somar todos os check-ins =====
    @Test
    fun `getTodayValueForHabit returns last checkin value not sum`() {
        val habitId = 1L
        val now = System.currentTimeMillis()
        val checkIns = listOf(
            HabitCheckIn(id = 1, habitId = habitId, value = 1, timestamp = now - 3000),
            HabitCheckIn(id = 2, habitId = habitId, value = 2, timestamp = now - 2000),
            HabitCheckIn(id = 3, habitId = habitId, value = 3, timestamp = now - 1000),
            HabitCheckIn(id = 4, habitId = habitId, value = 4, timestamp = now)
        )
        val result = viewModel.getTodayValueForHabit(habitId, checkIns)
        assertEquals(4, result, "Deve devolver o ÚLTIMO valor (4), não a soma (10)")
    }

    // ===== QUANTITATIVE — deve fazer UPDATE, não INSERT =====
    @Test
    fun `addCheckInValue updates existing checkin when already exists`() = runTest {
        val habitId = 1L
        val existing = HabitCheckIn(id = 10, habitId = habitId, value = 2, type = "PARTIAL")
        coEvery { repository.getCheckInForDay(any(), any(), any()) } returns existing

        val useCase = AddCheckInValueUseCase(repository)
        useCase(habitId, currentValue = 2, targetValue = 4)

        coVerify(exactly = 1) { repository.updateCheckIn(any()) }
        coVerify(exactly = 0) { repository.insertCheckIn(any()) }
    }

    @Test
    fun `addCheckInValue inserts when no checkin for today`() = runTest {
        coEvery { repository.getCheckInForDay(any(), any(), any()) } returns null

        val useCase = AddCheckInValueUseCase(repository)
        useCase(habitId = 1L, currentValue = 0, targetValue = 4)

        coVerify(exactly = 1) { repository.insertCheckIn(any()) }
        coVerify(exactly = 0) { repository.updateCheckIn(any()) }
    }

    @Test
    fun `addCheckInValue marks SUCCESS only when reaching target`() = runTest {
        val slots = mutableListOf<HabitCheckIn>()
        coEvery { repository.getCheckInForDay(any(), any(), any()) } returns null
        coEvery { repository.insertCheckIn(capture(slots)) } returns 1L

        val useCase = AddCheckInValueUseCase(repository)
        useCase(habitId = 1L, currentValue = 0, targetValue = 4)

        assertEquals("PARTIAL", slots.first().type)
    }

    // ===== DURATION — timer =====
    @Test
    fun `startTimer sets timerStartedAt on habit`() = runTest {
        val habit = Habit(id = 1L, name = "Meditar", durationMinutes = 30)
        val useCase = StartHabitTimerUseCase(repository)
        useCase(habit)

        coVerify(exactly = 1) {
            repository.updateHabit(match { it.timerStartedAt != null })
        }
    }

    @Test
    fun `pauseTimer accumulates minutes and clears timerStartedAt`() = runTest {
        val startedAt = System.currentTimeMillis() - 5 * 60 * 1000L
        val habit = Habit(
            id = 1L, name = "Meditar", durationMinutes = 30,
            timerStartedAt = startedAt, timerAccumulatedMinutes = 0
        )
        val useCase = PauseHabitTimerUseCase(repository)
        useCase(habit)

        coVerify(exactly = 1) {
            repository.updateHabit(match {
                it.timerStartedAt == null && it.timerAccumulatedMinutes >= 4
            })
        }
    }

    @Test
    fun `completeTimer creates checkin with duration and clears timer`() = runTest {
        val startedAt = System.currentTimeMillis() - 30 * 60 * 1000L
        val habit = Habit(
            id = 1L, name = "Meditar", durationMinutes = 30,
            timerStartedAt = startedAt, timerAccumulatedMinutes = 0
        )
        coEvery { repository.getCheckInForDay(any(), any(), any()) } returns null
        coEvery { repository.insertCheckIn(any()) } returns 1L

        val useCase = CompleteHabitTimerUseCase(repository)
        useCase(habit)

        coVerify {
            repository.insertCheckIn(match {
                it.durationMinutes != null && it.durationMinutes!! >= 29 && it.type == "SUCCESS"
            })
            repository.updateHabit(match {
                it.timerStartedAt == null && it.timerAccumulatedMinutes == 0
            })
        }
    }

    // ===== KIND detection =====
    @Test
    fun `kind returns DURATION when durationMinutes is set`() {
        val h = Habit(name = "x", durationMinutes = 30)
        assertEquals(HabitKind.DURATION, h.kind())
    }

    @Test
    fun `kind returns QUANTITATIVE when targetValue is set`() {
        val h = Habit(name = "x", targetValue = 4)
        assertEquals(HabitKind.QUANTITATIVE, h.kind())
    }

    @Test
    fun `kind returns BOOLEAN when nothing specific set`() {
        val h = Habit(name = "x")
        assertEquals(HabitKind.BOOLEAN, h.kind())
    }
}
