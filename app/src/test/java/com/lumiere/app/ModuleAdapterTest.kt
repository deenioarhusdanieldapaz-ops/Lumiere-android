package com.lumiere.app

import com.lumiere.app.core.adapters.TaskModuleAdapter
import com.lumiere.app.core.adapters.HabitModuleAdapter
import com.lumiere.app.data.local.entity.Task
import com.lumiere.app.data.repository.HabitRepository
import com.lumiere.app.data.repository.TaskRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class ModuleAdapterTest {

    @Test
    @DisplayName("TaskModuleAdapter devolve o resumo correto ao Core")
    fun `TaskModuleAdapter returns correct summary`() = runTest {
        val mockRepo = mockk<TaskRepository>()
        val tasks = listOf(
            Task(id = 1, title = "T1", status = 0), // Ativa
            Task(id = 2, title = "T2", status = 2), // Concluída
            Task(id = 3, title = "T3", status = 2)  // Concluída
        )
        coEvery { mockRepo.allTasks } returns flowOf(tasks)

        val adapter = TaskModuleAdapter(mockRepo)
        val summary = adapter.getSummary()

        assertEquals("M01_TASKS", summary.moduleId)
        assertEquals(1, summary.activeItemsCount)
        assertEquals(2, summary.completedItemsCount)
    }

    @Test
    @DisplayName("TaskModuleAdapter nao crasha se o repositorio falhar")
    fun `TaskModuleAdapter handles repository failure gracefully`() = runTest {
        val mockRepo = mockk<TaskRepository>()
        coEvery { mockRepo.allTasks } throws RuntimeException("Falha de BD")

        val adapter = TaskModuleAdapter(mockRepo)
        val summary = adapter.getSummary()

        assertEquals(0, summary.activeItemsCount) // Devolve 0 em vez de crashar
    }
}
