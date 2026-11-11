package com.lumiere.app.core.adapters

import com.lumiere.app.core.contracts.LumiereModule
import com.lumiere.app.core.contracts.ModuleSummary
import com.lumiere.app.data.repository.HabitRepository
import kotlinx.coroutines.flow.first

class HabitModuleAdapter(private val repository: HabitRepository) : LumiereModule {
    override val moduleId = "M02_HABITS"
    override val moduleName = "Hábitos"

    override suspend fun getSummary(): ModuleSummary {
        val habits = try { repository.allHabits.first() } catch (e: Exception) { emptyList() }
        return ModuleSummary(
            moduleId = moduleId,
            activeItemsCount = habits.count { it.status == 0 },
            completedItemsCount = habits.count { it.status == 2 },
            lastActivityTimestamp = habits.maxOfOrNull { it.createdAt }
        )
    }

    override suspend fun canPerformAction(actionId: String): Boolean = true
}
