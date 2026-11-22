package com.lumiere.app.core.engine

import com.lumiere.app.core.model.*
import com.lumiere.app.core.time.TimeProvider

class RecommendationRules(private val timeProvider: TimeProvider) {
    private val dayMs = 86_400_000L

    fun buildFacts(items: List<Schedulable>): List<Fact> {
        return items.map { item ->
            Fact(
                statement = "Item '${item.title}' (${item.moduleId})",
                source = FactSource.FromModule(item.moduleId, item.id, "1.1.0", timeProvider.currentTimeMillis()),
                sensitivity = Sensitivity.NORMAL
            )
        }
    }

    fun buildInferences(items: List<Schedulable>, facts: List<Fact>): List<Inference> {
        val inferences = mutableListOf<Inference>()
        
        val highEnergyItems = items.count { it.energyCost >= 3 }
        if (highEnergyItems > 2) {
            inferences.add(Inference(
                statement = "Tens $highEnergyItems itens de alta energia esta semana. Risco de Burnout.",
                method = InferenceMethod.CORRELATION,
                confidence = Confidence.CALIBRATED,
                supportingFactIds = emptyList()
            ))
        }

        return inferences
    }

    fun buildSuggestions(items: List<Schedulable>, inferences: List<Inference>): List<Suggestion> {
        val now = timeProvider.currentTimeMillis()
        val suggestions = mutableListOf<Suggestion>()

        // Regra 1: Aliviar Sobrecarga (Se um dia tiver mais de 8h)
        val overloadedDays = items.filter { it.startTimeMillis != null }.groupBy { it.startTimeMillis!! / dayMs }
        overloadedDays.forEach { (_, dayItems) ->
            if (dayItems.sumOf { it.durationMinutes ?: 0 } > 480) {
                // Encontra a tarefa (M01) menos prioritária ou o hábito (M02) mais curto
                val itemToMove = dayItems.filter { it.moduleId == "M01_TASKS" }.minByOrNull { it.priority }
                    ?: dayItems.minByOrNull { it.durationMinutes ?: 0 }
                    
                if (itemToMove != null) {
                    suggestions.add(Suggestion(
                        statement = "Aliviar a carga do dia movendo '${itemToMove.title}'",
                        why = "Este dia tem mais de 8h de compromissos entre módulos.",
                        impactDescription = "Liberta cerca de ${itemToMove.durationMinutes} min para descanso.",
                        approvalClass = ApprovalClass.A3_RELEVANT,
                        targetModules = setOf(itemToMove.moduleId),
                        impact = Impact.HIGH,
                        alternatives = listOf("Manter", "Encurtar duração"),
                        reversibility = Reversibility.REVERSIBLE,
                        expiresAtMillis = now + dayMs,
                        action = SuggestionAction.DeferItem(itemToMove.id, itemToMove.moduleId, 1)
                    ))
                }
            }
        }

        // Regra 2: Conflito de Alta Energia
        val highEnergyConflicts = items.filter { it.energyCost >= 3 && it.startTimeMillis != null }
        if (highEnergyConflicts.size >= 2) {
            val habitToShorten = highEnergyConflicts.firstOrNull { it.moduleId == "M02_HABITS" }
            if (habitToShorten != null) {
                suggestions.add(Suggestion(
                    statement = "Encurtar o hábito '${habitToShorten.title}' hoje",
                    why = "Tens várias tarefas de alta energia hoje e este hábito também desgasta.",
                    impactDescription = "Poupa energia mental para as tarefas críticas.",
                    approvalClass = ApprovalClass.A2_LOW_IMPACT,
                    targetModules = setOf(habitToShorten.moduleId),
                    impact = Impact.MEDIUM,
                    alternatives = listOf("Manter", "Mudar para amanhã"),
                    reversibility = Reversibility.REVERSIBLE,
                    expiresAtMillis = now + dayMs,
                    action = SuggestionAction.ShortenDuration(habitToShorten.id, habitToShorten.moduleId, 15)
                ))
            }
        }

        return suggestions.shuffled().take(5)
    }
}
