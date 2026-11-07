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
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class TaskTimerTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: TaskRepository
    private lateinit var viewModel: TaskViewModel

    @BeforeEach
    fun setup() {
        repository = mockk(relaxed = true)
        viewModel = TaskViewModel(repository)
    }

    @Test
    @DisplayName("toggleTimer em tarefa parada inicia cronómetro")
    fun toggleTimer_stoppedTask_startsTimer() = runTest(mainDispatcherRule.testDispatcher) {
        val task = TestDataFactory.task(id = 1L, timerStartedAt = null)
        val captured = slot<Task>()

        viewModel.toggleTimer(task)
        advanceUntilIdle()

        coVerify { repository.updateTask(capture(captured)) }
        assertNotNull(captured.captured.timerStartedAt)
        coVerify { repository.insertHistory(match { it.action == "Cronómetro Iniciado" }) }
    }

    @Test
    @DisplayName("toggleTimer em tarefa a correr para e acumula minutos")
    fun toggleTimer_runningTask_stopsAndAccumulates() = runTest(mainDispatcherRule.testDispatcher) {
        val startedTwoMinutesAgo = System.currentTimeMillis() - (2 * 60_000L)
        val task = TestDataFactory.task(
            id = 2L,
            timerStartedAt = startedTwoMinutesAgo,
            actualDurationMinutes = 5
        )
        val captured = slot<Task>()

        viewModel.toggleTimer(task)
        advanceUntilIdle()

        coVerify { repository.updateTask(capture(captured)) }
        val finalTask = captured.captured
        assertNull(finalTask.timerStartedAt)
        // Tolerância: 2 min ± 1 (execução real)
        assertTrue(finalTask.actualDurationMinutes >= 7, "Esperado ≥7, recebido ${finalTask.actualDurationMinutes}")
        coVerify { repository.insertHistory(match { it.action.startsWith("Cronómetro Parado") }) }
    }

    @Test
    @DisplayName("toggleTimer para sem somar se elapsed < 1 minuto")
    fun toggleTimer_shortRun_accumulatesZero() = runTest(mainDispatcherRule.testDispatcher) {
        val startedTenSecondsAgo = System.currentTimeMillis() - 10_000L
        val task = TestDataFactory.task(
            id = 3L,
            timerStartedAt = startedTenSecondsAgo,
            actualDurationMinutes = 0
        )
        val captured = slot<Task>()

        viewModel.toggleTimer(task)
        advanceUntilIdle()

        coVerify { repository.updateTask(capture(captured)) }
        // elapsed < 1 min → 0 minutos adicionados
        assertEquals(0, captured.captured.actualDurationMinutes)
        assertNull(captured.captured.timerStartedAt)
    }
}
