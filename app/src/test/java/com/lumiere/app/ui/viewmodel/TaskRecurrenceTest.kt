package com.lumiere.app.ui.viewmodel

import com.lumiere.app.data.local.entity.Task
import com.lumiere.app.data.repository.TaskRepository
import com.lumiere.app.shared.MainDispatcherRule
import com.lumiere.app.shared.TestDataFactory
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class TaskRecurrenceTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: TaskRepository
    private lateinit var viewModel: TaskViewModel

    private val baseDate = Calendar.getInstance().apply {
        set(2026, Calendar.NOVEMBER, 7, 10, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun expectedDate(addField: Int, amount: Int): Long {
        return Calendar.getInstance().apply {
            timeInMillis = baseDate
            add(addField, amount)
        }.timeInMillis
    }

    @BeforeEach
    fun setup() {
        repository = mockk(relaxed = true)
        viewModel = TaskViewModel(repository)
    }

    @Test
    @DisplayName("Recorrência DAILY gera tarefa com dueDate +1 dia")
    fun daily_recurrence_createsTaskTomorrow() = runTest(mainDispatcherRule.testDispatcher) {
        val task = TestDataFactory.task(
            id = 1L, status = 0, recurrenceRule = "DAILY", dueDate = baseDate
        )
        val captured = slot<Task>()

        viewModel.toggleTaskCompletion(task)
        advanceUntilIdle()

        coVerify { repository.insertTask(capture(captured)) }
        assertEquals(expectedDate(Calendar.DAY_OF_YEAR, 1), captured.captured.dueDate)
    }

    @Test
    @DisplayName("Recorrência WEEKLY gera tarefa com dueDate +7 dias")
    fun weekly_recurrence_createsTaskNextWeek() = runTest(mainDispatcherRule.testDispatcher) {
        val task = TestDataFactory.task(
            id = 2L, status = 0, recurrenceRule = "WEEKLY", dueDate = baseDate
        )
        val captured = slot<Task>()

        viewModel.toggleTaskCompletion(task)
        advanceUntilIdle()

        coVerify { repository.insertTask(capture(captured)) }
        assertEquals(expectedDate(Calendar.WEEK_OF_YEAR, 1), captured.captured.dueDate)
    }

    @Test
    @DisplayName("Recorrência MONTHLY gera tarefa com dueDate +1 mês")
    fun monthly_recurrence_createsTaskNextMonth() = runTest(mainDispatcherRule.testDispatcher) {
        val task = TestDataFactory.task(
            id = 3L, status = 0, recurrenceRule = "MONTHLY", dueDate = baseDate
        )
        val captured = slot<Task>()

        viewModel.toggleTaskCompletion(task)
        advanceUntilIdle()

        coVerify { repository.insertTask(capture(captured)) }
        assertEquals(expectedDate(Calendar.MONTH, 1), captured.captured.dueDate)
    }

    @Test
    @DisplayName("Recorrência YEARLY gera tarefa com dueDate +1 ano")
    fun yearly_recurrence_createsTaskNextYear() = runTest(mainDispatcherRule.testDispatcher) {
        val task = TestDataFactory.task(
            id = 4L, status = 0, recurrenceRule = "YEARLY", dueDate = baseDate
        )
        val captured = slot<Task>()

        viewModel.toggleTaskCompletion(task)
        advanceUntilIdle()

        coVerify { repository.insertTask(capture(captured)) }
        assertEquals(expectedDate(Calendar.YEAR, 1), captured.captured.dueDate)
    }

    @Test
    @DisplayName("Recorrência NONE não gera nova tarefa")
    fun noRecurrence_doesNotCreateTask() = runTest(mainDispatcherRule.testDispatcher) {
        val task = TestDataFactory.task(
            id = 5L, status = 0, recurrenceRule = "NONE", dueDate = baseDate
        )

        viewModel.toggleTaskCompletion(task)
        advanceUntilIdle()

        coVerify(exactly = 0) { repository.insertTask(any()) }
    }

    @Test
    @DisplayName("Tarefa sem recurrenceRule não gera nova")
    fun nullRecurrence_doesNotCreateTask() = runTest(mainDispatcherRule.testDispatcher) {
        val task = TestDataFactory.task(
            id = 6L, status = 0, recurrenceRule = null, dueDate = baseDate
        )

        viewModel.toggleTaskCompletion(task)
        advanceUntilIdle()

        coVerify(exactly = 0) { repository.insertTask(any()) }
    }
}
