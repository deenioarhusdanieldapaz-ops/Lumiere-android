package com.lumiere.app.core.orchestrator

import com.lumiere.app.core.contracts.LumiereModule
import com.lumiere.app.core.contracts.ModuleCommand
import com.lumiere.app.core.contracts.ModuleCommandResult
import com.lumiere.app.core.contracts.ModuleSummary

/**
 * Core mínimo funcional — Fase 5.
 * Regras:
 *  - Não acede a tabelas de módulos. Só chama contratos (LumiereModule).
 *  - Não decide sozinho: pede resumos e despacha comandos ao módulo competente.
 *  - Não persiste dados de módulos (fundação; memória do Core fica para Fase 6).
 */
class CoreOrchestrator {
    private val modules = mutableMapOf<String, LumiereModule>()

    fun register(module: LumiereModule) {
        require(module.moduleId.isNotBlank()) { "moduleId vazio" }
        modules[module.moduleId] = module
    }

    fun listModules(): List<Pair<String, String>> =
        modules.values.map { it.moduleId to it.moduleName }

    suspend fun getSummary(moduleId: String): ModuleSummary? =
        modules[moduleId]?.getSummary()

    suspend fun getAllSummaries(): List<ModuleSummary> =
        modules.values.mapNotNull { runCatching { it.getSummary() }.getOrNull() }

    suspend fun dispatch(moduleId: String, command: ModuleCommand): ModuleCommandResult {
        val module = modules[moduleId]
            ?: return ModuleCommandResult.Failure("Módulo não registado: $moduleId")
        return module.executeCommand(command)
    }
}
