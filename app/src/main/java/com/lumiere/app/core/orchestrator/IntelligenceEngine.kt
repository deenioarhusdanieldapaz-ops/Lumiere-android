package com.lumiere.app.core.orchestrator

import com.lumiere.app.core.contracts.AnalysisResult
import com.lumiere.app.core.contracts.CoreContext
import com.lumiere.app.core.contracts.Recommendation

/**
 * Motor de Inteligência da Lumière (Fase 5 - Determinístico).
 * Regras:
 * - STRICT: Identifica conflitos temporais (mock de lógica temporal).
 * - APPROVAL: Recomendações nunca são auto-executadas, exigem autorização.
 * - Explicabilidade: Cada sugestão tem uma justificação explícita.
 */
class IntelligenceEngine {

    fun analyze(context: CoreContext): AnalysisResult {
        val risks = mutableListOf<String>()
        val recommendations = mutableListOf<Recommendation>()

        // 1. Análise de Tarefas (Prioridades e Atrasos)
        val incompleteTasks = context.tasks.filter { !it.isCompleted }
        val highPriorityTasks = incompleteTasks.filter { it.priority == 1 } // Assumindo 1 = Alta prioridade

        if (highPriorityTasks.isNotEmpty()) {
            risks.add("Existem ${highPriorityTasks.size} tarefas de alta prioridade pendentes.")
            recommendations.add(
                Recommendation(
                    id = "REC-TASK-01",
                    title = "Focar em tarefas críticas",
                    reasoning = "O sistema detetou tarefas de alta prioridade não concluídas que podem bloquear o progresso.",
                    requiresApproval = true
                )
            )
        }

        // 2. Análise de Hábitos (Consistência)
        val activeHabits = context.habits.filter { it.status == 0 } // Assumindo 0 = Ativo
        val completedHabits = context.habits.filter { it.status == 2 } // Assumindo 2 = Concluído

        if (activeHabits.size > 5) {
            risks.add("Sobrecarga de hábitos: ${activeHabits.size} ativos simultaneamente.")
            recommendations.add(
                Recommendation(
                    id = "REC-HAB-01",
                    title = "Reduzir carga de hábitos",
                    reasoning = "A ciência comportamental indica que muitos hábitos ativos reduzem a consistência. Sugere-se focar em menos.",
                    requiresApproval = true
                )
            )
        }

        // 3. Regra STRICT (Conflitos Temporais / Cruzamento de Dados)
        // Simulação de lógica: Se houver tarefas atrasadas E hábitos não concluídos
        if (incompleteTasks.isNotEmpty() && activeHabits.isNotEmpty()) {
            recommendations.add(
                Recommendation(
                    id = "REC-CORE-01",
                    title = "Planear o dia (STRICT)",
                    reasoning = "Há tarefas pendentes e hábitos para cumprir. Sugere-se uma revisão do plano para evitar sobreposição temporal.",
                    requiresApproval = true
                )
            )
        }

        return AnalysisResult(risks, recommendations)
    }
}
