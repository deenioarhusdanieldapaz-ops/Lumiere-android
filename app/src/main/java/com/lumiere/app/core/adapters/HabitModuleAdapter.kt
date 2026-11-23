package com.lumiere.app.core.adapters

import com.lumiere.app.core.contracts.LumiereModule
import com.lumiere.app.core.contracts.ModuleCommand
import com.lumiere.app.core.contracts.ModuleCommandResult
import com.lumiere.app.core.contracts.ModuleSummary
import com.lumiere.app.data.repository.HabitRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

class HabitModuleAdapter(private val repository: HabitRepository) : LumiereModule {
    override val moduleId = "M02_HABITS"
    override val moduleName = "Hábitos"

    override suspend fun getSummary(): ModuleSummary {
        val habits = try { repository.allHabits.first() }
        catch (e: CancellationException) { throw e }
        catch (e: Exception) { emptyList() }
        return ModuleSummary(
            moduleId = moduleId,
            activeItemsCount = habits.count { it.status == 0 },
            completedItemsCount = habits.count { it.status == 2 },
            lastActivityTimestamp = habits.maxOfOrNull { it.createdAt }
        )
    }

    override suspend fun canPerformAction(actionId: String): Boolean = true

    override suspend fun executeCommand(command: ModuleCommand): ModuleCommandResult {
        return try {
            when (command) {
                is ModuleCommand.GetActiveTasksCount ->
                    ModuleCommandResult.Failure("Comando não aplicável ao módulo Habits")
            }
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) {
            ModuleCommandResult.Failure(e.message ?: "erro desconhecido")
        }
    }
}
