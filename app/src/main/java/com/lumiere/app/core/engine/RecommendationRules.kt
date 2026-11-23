package com.lumiere.app.core.engine

import com.lumiere.app.core.contract.HabitFrequency
import com.lumiere.app.core.contract.HabitSnapshot
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

        // 1. Prazos Ultrapassados
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

        // 2. M2 — Hábitos com baixa adesão (ShortenDuration)
        val habits = items.filterIsInstance<HabitSnapshot>()
        habits.filter { it.successRate30d < 0.4f }.forEach { habit ->
            val currentDur = habit.durationMinutes ?: 30
            val newDur = maxOf(5, currentDur - 15)
            suggestions.add(Suggestion(
                statement = "Encurtar '${habit.nameShort}' de ${currentDur}min para ${newDur}min",
                why = "Este hábito tem uma taxa de sucesso de ${(habit.successRate30d * 100).toInt()}% nos últimos 30 dias.",
                impactDescription = "Reduz a fricção para aumentar a consistência.",
                approvalClass = ApprovalClass.A3_RELEVANT,
                targetModules = setOf("M02_HABITS"),
                impact = Impact.MEDIUM,
                alternatives = listOf("Manter", "Mudar frequência"),
                reversibility = Reversibility.REVERSIBLE,
                expiresAtMillis = now + dayMs,
                action = SuggestionAction.ShortenDuration(habit.id, "M02_HABITS", newDur)
            ))
        }

        // 3. M2 — Mudança de frequência para hábitos em falha crítica
        habits.filter { it.successRate30d < 0.2f && it.frequency == HabitFrequency.DAILY }
            .forEach { habit ->
                suggestions.add(Suggestion(
                    statement = "Mudar '${habit.nameShort}' de diário para semanal",
                    why = "Taxa de sucesso de ${(habit.successRate30d * 100).toInt()}% é demasiado baixa para manter diário.",
                    impactDescription = "Frequência mais realista aumenta a chance de criar o hábito.",
                    approvalClass = ApprovalClass.A3_RELEVANT,
                    targetModules = setOf("M02_HABITS"),
                    impact = Impact.HIGH,
                    alternatives = listOf("Manter diário", "Reduzir duração"),
                    reversibility = Reversibility.REVERSIBLE,
                    expiresAtMillis = now + dayMs,
                    action = SuggestionAction.ChangeFrequency(habit.id, "M02_HABITS", "WEEKLY")
                ))
            }

        // 4. R7 — Inferência de burnout gera sugestão concreta
        val hasBurnoutRisk = inferences.any { it.statement.contains("Burnout") }
        if (hasBurnoutRisk) {
            val lowPriorityTasks = items.filter { it.moduleId == "M01_TASKS" }
                .sortedBy { it.priority }
                .take(3)
            if (lowPriorityTasks.isNotEmpty()) {
                suggestions.add(Suggestion(
                    statement = "Aliviar carga semanal reagendando ${lowPriorityTasks.size} tarefa(s) de menor prioridade",
                    why = "Detetado risco de burnout por excesso de itens de alta energia.",
                    impactDescription = "Reduz a pressão da semana sem comprometer o essencial.",
                    approvalClass = ApprovalClass.A3_RELEVANT,
                    targetModules = setOf("M01_TASKS"),
                    impact = Impact.HIGH,
                    alternatives = listOf("Manter", "Adiar uma"),
                    reversibility = Reversibility.REVERSIBLE,
                    expiresAtMillis = now + dayMs,
                    action = SuggestionAction.BatchDeferItems(lowPriorityTasks.map { it.id }, "M01_TASKS", 3)
                ))
            }
        }

        // 5. Cruzamento de Módulos
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

        // 6. Sobrecarga Diária
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

        // R3 — dedup robusto: só colapsa se (statement, modules, action) forem iguais
        return suggestions
            .distinctBy { Triple(it.statement, it.targetModules, DeterministicIds.actionKey(it.action)) }
            .take(5)
    }
}
