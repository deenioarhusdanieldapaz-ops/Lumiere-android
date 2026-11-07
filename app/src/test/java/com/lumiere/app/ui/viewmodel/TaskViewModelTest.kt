package com.lumiere.app.ui.viewmodel

import com.lumiere.app.data.local.entity.Subtask
import com.lumiere.app.data.local.entity.Task
import com.lumiere.app.data.local.entity.TaskHistory
import com.lumiere.app.data.repository.TaskRepository
import com.lumiere.app.shared.MainDispatcherRule
import com.lumiere.app.shared.TestDataFactory
import io.mockk.coEvery
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
class TaskViewModelTest {

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
    @DisplayName("addTask insere tarefa e regista histórico 'Tarefa Criada'")
    fun addTask_insertsTaskAndHistory() = runTest(mainDispatcherRule.testDispatcher) {
        val task = TestDataFactory.task(title = "QA addTask")
        coEvery { repository.insertTask(task) } returns 1L

        viewModel.addTask(task)
        advanceUntilIdle()

        coVerify { repository.insertTask(task) }
        coVerify { repository.insertHistory(match { it.taskId == 1L && it.action == "Tarefa Criada" }) }
    }

    @Test
    @DisplayName("updateTask atualiza e regista 'Tarefa Editada'")
    fun updateTask_updatesAndLogsHistory() = runTest(mainDispatcherRule.testDispatcher) {
        val task = TestDataFactory.task(id = 5L, title = "Original")

        viewModel.updateTask(task)
        advanceUntilIdle()

        coVerify {
            repository.updateTask(match { it.id == 5L && it.updatedAt >= task.updatedAt })
        }
        coVerify { repository.insertHistory(match { it.taskId == 5L && it.action == "Tarefa Editada" }) }
    }

    @Test
    @DisplayName("toggleTaskCompletion pendente->concluída, status 2, histórico 'Concluída'")
    fun toggleTaskCompletion_pendingToCompleted() = runTest(mainDispatcherRule.testDispatcher) {
        val task = TestDataFactory.task(id = 7L, status = 0)

        viewModel.toggleTaskCompletion(task)
        advanceUntilIdle()

        coVerify { repository.updateTask(match { it.id == 7L && it.status == 2 }) }
        coVerify { repository.insertHistory(match { it.taskId == 7L && it.action == "Concluída" }) }
    }

    @Test
    @DisplayName("toggleTaskCompletion concluída->pendente, status 0, histórico 'Reaberta'")
    fun toggleTaskCompletion_completedToPending() = runTest(mainDispatcherRule.testDispatcher) {
        val task = TestDataFactory.completedTask(id = 8L)

        viewModel.toggleTaskCompletion(task)
        advanceUntilIdle()

        coVerify { repository.updateTask(match { it.id == 8L && it.status == 0 }) }
        coVerify { repository.insertHistory(match { it.taskId == 8L && it.action == "Reaberta" }) }
    }

    @Test
    @DisplayName("cancelTask define status 3, motivo, limpa timer, regista histórico")
    fun cancelTask_setsStatusAndReason() = runTest(mainDispatcherRule.testDispatcher) {
        val task = TestDataFactory.task(id = 10L, timerStartedAt = 1000L)

        viewModel.cancelTask(task, "Motivo QA")
        advanceUntilIdle()

        coVerify {
            repository.updateTask(match {
                it.id == 10L &&
                it.status == 3 &&
                it.cancellationReason == "Motivo QA" &&
                it.timerStartedAt == null
            })
        }
        coVerify { repository.insertHistory(match { it.action == "Cancelada: Motivo QA" }) }
    }

    @Test
    @DisplayName("resumeTask: status 0, motivo null, histórico 'Retomada'")
    fun resumeTask_clearsStatusAndReason() = runTest(mainDispatcherRule.testDispatcher) {
        val task = TestDataFactory.task(id = 11L, status = 3, cancellationReason = "Motivo antigo")

        viewModel.resumeTask(task)
        advanceUntilIdle()

        coVerify {
            repository.updateTask(match {
                it.id == 11L && it.status == 0 && it.cancellationReason == null
            })
        }
        coVerify { repository.insertHistory(match { it.action == "Retomada" }) }
    }

    @Test
    @DisplayName("duplicateTask cria cópia com título '(Cópia)' e status 0")
    fun duplicateTask_createsCopyWithSuffix() = runTest(mainDispatcherRule.testDispatcher) {
        val task = TestDataFactory.completedTask(id = 12L, title = "Original")

        viewModel.duplicateTask(task)
        advanceUntilIdle()

        coVerify {
            repository.insertTask(match {
                it.id == 0L &&
                it.title == "Original (Cópia)" &&
                it.status == 0
            })
        }
    }

    @Test
    @DisplayName("archiveTask define status 4 e histórico 'Arquivada'")
    fun archiveTask_setsArchivedStatus() = runTest(mainDispatcherRule.testDispatcher) {
        val task = TestDataFactory.task(id = 13L)

        viewModel.archiveTask(task)
        advanceUntilIdle()

        coVerify { repository.updateTask(match { it.id == 13L && it.status == 4 }) }
        coVerify { repository.insertHistory(match { it.action == "Arquivada" }) }
    }

    @Test
    @DisplayName("deleteTask chama deleteTask do repositório")
    fun deleteTask_callsRepository() = runTest(mainDispatcherRule.testDispatcher) {
        val task = TestDataFactory.task(id = 14L)

        viewModel.deleteTask(task)
        advanceUntilIdle()

        coVerify { repository.deleteTask(match { it.id == 14L }) }
    }

    @Test
    @DisplayName("addSubtask cria subtarefa com title e regista histórico")
    fun addSubtask_insertsWithTitle() = runTest(mainDispatcherRule.testDispatcher) {
        viewModel.addSubtask(taskId = 20L, title = "Subtarefa QA")
        advanceUntilIdle()

        coVerify {
            repository.insertSubtask(match {
                it.taskId == 20L && it.title == "Subtarefa QA"
            })
        }
        coVerify { repository.insertHistory(match { it.action.contains("Subtarefa Adicionada") }) }
    }

    @Test
    @DisplayName("toggleSubtask inverte estado isCompleted")
    fun toggleSubtask_flipsCompleted() = runTest(mainDispatcherRule.testDispatcher) {
        val subtask = Subtask(id = 1L, taskId = 20L, title = "QA", isCompleted = false)

        viewModel.toggleSubtask(subtask)
        advanceUntilIdle()

        coVerify { repository.updateSubtask(match { it.id == 1L && it.isCompleted == true }) }
    }
}
