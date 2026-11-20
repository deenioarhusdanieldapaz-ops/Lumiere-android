package com.lumiere.app.core.orchestrator

import com.lumiere.app.core.contracts.*
import kotlinx.coroutines.flow.first

class CoreOrchestrator {
    private val modules = mutableMapOf<String, LumiereModule>()
    private val intelligence = IntelligenceEngine()

    fun register(module: LumiereModule) {
        require(module.moduleId.isNotBlank()) { "moduleId vazio" }
        modules[module.moduleId] = module
    }

    suspend fun runIntelligenceCycle(): AnalysisResult {
        // 1. Recolher Contexto (Context Builder)
        // Na Fase 5, os adapters fornecem os dados. Num futuro, podemos usar repositórios diretamente aqui.
        val taskAdapter = modules["M01_TASKS"]
        val habitAdapter = modules["M02_HABITS"]
        
        // Simulação de extração de contexto - no código real passaríamos os objetos de domínio
        val mockTasks = listOf<Task>() 
        val mockHabits = listOf<Habit>()
        
        val context = CoreContext(mockTasks, mockHabits)

        // 2. Analisar
        return intelligence.analyze(context)
    }
    
    suspend fun dispatch(moduleId: String, command: ModuleCommand): ModuleCommandResult {
        val module = modules[moduleId]
            ?: return ModuleCommandResult.Failure("Módulo não registado: $moduleId")
        return module.executeCommand(command)
    }
}
