package com.lumiere.app.core.engine

import com.lumiere.app.core.model.*
import com.lumiere.app.core.time.TimeProvider
import java.util.Calendar

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

        // 1. Resolver Prazos Ultrapassados (STRICT_VIOLATION)
        val overdueTasks = items.filter { item ->
            val time = item.startTimeMillis
            item.moduleId == "M01_TASKS" && time != null && time < now
        }
        overdueTasks.forEach { task ->
            suggestions.add(Suggestion(
                statement = "Reagendar a tarefa atrasada '${task.title}' para amanhã",
                why = "Esta tarefa já ultrapassou o prazo definido.",
                impactDescription = "Remove o aviso de atraso e dá-te um novo foco.",
                approvalClass = ApprovalClass.A2_LOW_IMPACT,
                targetModules = setOf(task.moduleId),
                impact = Impact.LOW,
                alternatives = listOf("Concluir hoje", "Ignorar"),
                reversibility = Reversibility.REVERSIBLE,
                expiresAtMillis = now + dayMs,
                action = SuggestionAction.DeferItem(task.id, task.moduleId, 1)
            ))
        }

        // 2. Resolver Cruzamento de Módulos (TEMPORAL_OVERLAP)
        val scheduledItems = items.filter { it.startTimeMillis != null }
        scheduledItems.forEachIndexed { i, itemA ->
            val timeA = itemA.startTimeMillis ?: return@forEachIndexed
            for (j in i + 1 until scheduledItems.size) {
                val itemB = scheduledItems[j]
                val timeB = itemB.startTimeMillis ?: continue
                val diffMins = Math.abs(timeA - timeB) / 60_000
                
                if (diffMins < 30 && itemA.moduleId != itemB.moduleId) {
                    val habit = if (itemA.moduleId == "M02_HABITS") itemA else if (itemB.moduleId == "M02_HABITS") itemB else null
                    if (habit != null) {
                        val habitTime = habit.startTimeMillis ?: continue
                        val cal = Calendar.getInstance()
                        cal.timeInMillis = habitTime
                        cal.add(Calendar.HOUR_OF_DAY, 2)
                        suggestions.add(Suggestion(
                            statement = "Mudar o horário do hábito '${habit.title}'",
                            why = "Este hábito está a colidir com uma tarefa de outro módulo.",
                            impactDescription = "Evita interrupções e conflitos de agenda.",
                            approvalClass = ApprovalClass.A3_RELEVANT,
                            targetModules = setOf(habit.moduleId),
                            impact = Impact.MEDIUM,
                            alternatives = listOf("Manter", "Mover tarefa"),
                            reversibility = Reversibility.REVERSIBLE,
                            expiresAtMillis = now + dayMs,
                            action = SuggestionAction.RescheduleItem(habit.id, habit.moduleId, cal.timeInMillis)
                        ))
                    }
                }
            }
        }

        // 3. Resolver Sobrecarga Diária
        val overloadedDays = items.filter { it.startTimeMillis != null }.groupBy { (it.startTimeMillis ?: 0L) / dayMs }
        overloadedDays.forEach { (_, dayItems) ->
            if (dayItems.sumOf { it.durationMinutes ?: 0 } > 480) {
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

        return suggestions.distinctBy { it.statement }.shuffled().take(5)
    }
}
