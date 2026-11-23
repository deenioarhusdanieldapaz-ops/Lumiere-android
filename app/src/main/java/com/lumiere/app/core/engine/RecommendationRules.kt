package com.lumiere.app.core.engine

import com.lumiere.app.core.model.*
import com.lumiere.app.core.time.TimeProvider
import com.lumiere.app.core.time.TimeUtils
import java.util.Calendar

class RecommendationRules(private val timeProvider: TimeProvider) {
    private val dayMs = 86_400_000L

    private fun overlaps(a: Schedulable, b: Schedulable): Boolean {
        val startA = a.startTimeMillis ?: return false
        val endA = startA + ((a.durationMinutes ?: 30) * 60_000L)
        val startB = b.startTimeMillis ?: return false
        val endB = startB + ((b.durationMinutes ?: 30) * 60_000L)
        return Math.max(startA, startB) < Math.min(endA, endB)
    }

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
                supportingFactIds = facts.filter { f ->
                    items.any { it.id == (f.source as FactSource.FromModule).recordId && it.energyCost >= 3 }
                }.map { it.id }
            ))
        }
        return inferences
    }

    fun buildSuggestions(items: List<Schedulable>, inferences: List<Inference>): List<Suggestion> {
        val now = timeProvider.currentTimeMillis()
        val tz = timeProvider.timeZone()
        val suggestions = mutableListOf<Suggestion>()

        val overdueTasks = items.filter { item ->
            val deadline = item.deadlineMillis
            item.moduleId == "M01_TASKS" && deadline != null && deadline < now
        }
        if (overdueTasks.isNotEmpty()) {
            val firstTask = overdueTasks.first()
            suggestions.add(Suggestion(
                statement = "Reagendar ${overdueTasks.size} tarefa(s) atrasada(s) para amanhã",
                why = "Tens tarefas que ultrapassaram o prazo (ex: '${firstTask.title}').",
                impactDescription = "Remove a sensação de culpa e limpa a agenda.",
                approvalClass = ApprovalClass.A3_RELEVANT,
                targetModules = setOf("M01_TASKS"),
                impact = Impact.MEDIUM,
                alternatives = listOf("Concluir hoje", "Ignorar"),
                reversibility = Reversibility.REVERSIBLE,
                expiresAtMillis = now + dayMs,
                action = SuggestionAction.BatchDeferItems(overdueTasks.map { it.id }, "M01_TASKS", 1)
            ))
        }

        val scheduledItems = items.filter { it.startTimeMillis != null }
        val habitsAlreadyScheduled = mutableSetOf<String>()
        for (i in scheduledItems.indices) {
            for (j in i + 1 until scheduledItems.size) {
                val itemA = scheduledItems[i]
                val itemB = scheduledItems[j]
                if (itemA.moduleId == itemB.moduleId) continue
                if (!overlaps(itemA, itemB)) continue

                val habit = if (itemA.moduleId == "M02_HABITS") itemA
                            else if (itemB.moduleId == "M02_HABITS") itemB
                            else null
                if (habit != null && habit.id !in habitsAlreadyScheduled) {
                    habitsAlreadyScheduled.add(habit.id)
                    val habitTime = habit.startTimeMillis ?: continue
                    val cal = Calendar.getInstance(tz)
                    cal.timeInMillis = habitTime
                    cal.add(Calendar.HOUR_OF_DAY, 2)
                    suggestions.add(Suggestion(
                        statement = "Mudar o horário do hábito '${habit.title}'",
                        why = "Este hábito está a colidir com uma tarefa de outro módulo.",
                        impactDescription = "Evita interrupções e fadiga.",
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

        val overloadedDays = items.filter { it.startTimeMillis != null }
            .groupBy { TimeUtils.dayKey(it.startTimeMillis!!, tz) }
        overloadedDays.forEach { (_, dayItems) ->
            if (dayItems.sumOf { it.durationMinutes ?: 30 } > 480) {
                val itemToMove = dayItems.filter { it.moduleId == "M01_TASKS" }
                        .minByOrNull { it.priority }
                    ?: dayItems.minByOrNull { it.durationMinutes ?: 0 }
                if (itemToMove != null) {
                    suggestions.add(Suggestion(
                        statement = "Aliviar a carga do dia movendo '${itemToMove.title}'",
                        why = "Este dia tem mais de 8h de compromissos entre módulos.",
                        impactDescription = "Liberta cerca de ${itemToMove.durationMinutes ?: 30} min para descanso.",
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

        return suggestions.distinctBy { it.statement }.take(5)
    }
}
