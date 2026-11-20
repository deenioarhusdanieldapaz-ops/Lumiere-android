package com.lumiere.app.core.adapters

import com.lumiere.app.core.contracts.LumiereModule
import com.lumiere.app.core.contracts.ModuleCommand
import com.lumiere.app.core.contracts.ModuleCommandResult
import com.lumiere.app.core.contracts.ModuleSummary
import com.lumiere.app.data.repository.TaskRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

class TaskModuleAdapter(private val repository: TaskRepository) : LumiereModule {
    override val moduleId = "M01_TASKS"
    override val moduleName = "Tarefas"

    override suspend fun getSummary(): ModuleSummary {
        val tasks = try { repository.allTasks.first() }
        catch (e: CancellationException) { throw e }
        catch (e: Exception) { emptyList() }
        return ModuleSummary(
            moduleId = moduleId,
            activeItemsCount = tasks.count { !it.isCompleted },
            completedItemsCount = tasks.count { it.isCompleted },
            lastActivityTimestamp = tasks.maxOfOrNull { it.updatedAt }
        )
    }

    override suspend fun canPerformAction(actionId: String): Boolean = true

    override suspend fun executeCommand(command: ModuleCommand): ModuleCommandResult {
        return try {
            when (command) {
                is ModuleCommand.GetActiveTasksCount -> {
                    val count = repository.allTasks.first().count { !it.isCompleted }
                    ModuleCommandResult.Success(count)
                }
            }
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) {
            ModuleCommandResult.Failure(e.message ?: "erro desconhecido")
        }
    }
}
