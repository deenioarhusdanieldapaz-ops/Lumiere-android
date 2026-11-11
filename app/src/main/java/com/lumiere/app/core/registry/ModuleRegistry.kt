package com.lumiere.app.core.registry

import com.lumiere.app.core.contracts.LumiereModule
import com.lumiere.app.core.contracts.ModuleSummary

object ModuleRegistry {
    private val modules = mutableMapOf<String, LumiereModule>()

    // Regista um módulo para o Core o conhecer
    fun register(module: LumiereModule) {
        modules[module.moduleId] = module
    }

    // O Core pede um resumo de todos os módulos
    suspend fun getAllSummaries(): List<ModuleSummary> {
        return modules.values.map { it.getSummary() }
    }
}
