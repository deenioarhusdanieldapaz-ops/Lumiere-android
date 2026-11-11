package com.lumiere.app.core.contracts

// O resumo dos dados que o Core pode "ver" sem mexer nas tabelas
data class ModuleSummary(
    val moduleId: String,
    val activeItemsCount: Int,
    val completedItemsCount: Int,
    val lastActivityTimestamp: Long?
)

// O contrato que todos os 13 módulos terão de implementar no futuro
interface LumiereModule {
    val moduleId: String
    val moduleName: String
    suspend fun getSummary(): ModuleSummary
    suspend fun canPerformAction(actionId: String): Boolean
}
