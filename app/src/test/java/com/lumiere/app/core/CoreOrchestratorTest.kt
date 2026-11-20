package com.lumiere.app.core

import com.lumiere.app.core.contracts.LumiereModule
import com.lumiere.app.core.contracts.ModuleCommand
import com.lumiere.app.core.contracts.ModuleCommandResult
import com.lumiere.app.core.contracts.ModuleSummary
import com.lumiere.app.core.orchestrator.CoreOrchestrator
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreOrchestratorTest {

    private fun fakeModule(id: String, name: String, activeCount: Int) = object : LumiereModule {
        override val moduleId = id
        override val moduleName = name
        override suspend fun getSummary() = ModuleSummary(id, activeCount, 0, null)
        override suspend fun canPerformAction(actionId: String) = true
        override suspend fun executeCommand(command: ModuleCommand) = when (command) {
            is ModuleCommand.GetActiveTasksCount -> ModuleCommandResult.Success(activeCount)
        }
    }

    @Test
    fun register_and_listModules() {
        val core = CoreOrchestrator()
        core.register(fakeModule("M01_TASKS", "Tarefas", 3))
        core.register(fakeModule("M02_HABITS", "Hábitos", 5))
        assertEquals(2, core.listModules().size)
    }

    @Test
    fun getSummary_returnsRegisteredModuleSummary() = runTest {
        val core = CoreOrchestrator()
        core.register(fakeModule("M01_TASKS", "Tarefas", 3))
        val summary = core.getSummary("M01_TASKS")
        assertEquals(3, summary?.activeItemsCount)
    }

    @Test
    fun dispatch_routesCommandToCorrectModule() = runTest {
        val core = CoreOrchestrator()
        core.register(fakeModule("M01_TASKS", "Tarefas", 7))
        val result = core.dispatch("M01_TASKS", ModuleCommand.GetActiveTasksCount)
        assertTrue(result is ModuleCommandResult.Success)
        assertEquals(7, (result as ModuleCommandResult.Success).data)
    }

    @Test
    fun dispatch_unknownModule_returnsFailure() = runTest {
        val core = CoreOrchestrator()
        val result = core.dispatch("M99_UNKNOWN", ModuleCommand.GetActiveTasksCount)
        assertTrue(result is ModuleCommandResult.Failure)
    }
}
