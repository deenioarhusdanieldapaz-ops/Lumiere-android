package com.lumiere.app.core.engine

import com.lumiere.app.core.contract.*
import com.lumiere.app.core.model.*
import com.lumiere.app.core.time.TimeProvider

class RecommendationRules(private val timeProvider: TimeProvider) {
    private val dayMs = 86_400_000L

    fun buildFacts(tasks: List<TaskSnapshot>, habits: List<HabitSnapshot>): List<Fact> {
        val facts = mutableListOf<Fact>()
        tasks.forEach { t -> 
            facts.add(Fact(
                statement = "Tarefa '${t.titleShort}' [${t.state}]",
                source = FactSource.FromModule("tasks", t.id, "1.1.0", t.lastUpdatedMillis),
                sensitivity = t.sensitivity
            )) 
        }
        habits.forEach { h -> 
            facts.add(Fact(
                statement = "Hábito '${h.nameShort}' (${h.frequency})",
                source = FactSource.FromModule("habits", h.id, "1.1.0", h.lastUpdatedMillis),
                sensitivity = h.sensitivity
            )) 
        }
        return facts
    }

    fun buildInferences(tasks: List<TaskSnapshot>, habits: List<HabitSnapshot>, facts: List<Fact>): List<Inference> {
        val inferences = mutableListOf<Inference>()

        val overloadedDays = tasks.filter { it.state == TaskState.ACTIVE && it.deadlineMillis != null }
            .groupBy { it.deadlineMillis!! / dayMs }
            .filter { (_, dayTasks) -> dayTasks.sumOf { it.estimatedEffortMinutes ?: 0 } > 480 }
        if (overloadedDays.isNotEmpty()) {
            inferences.add(Inference(
                statement = "Dias sobrecarregados detetados: ${overloadedDays.size} dias com >8h de esforço.",
                method = InferenceMethod.CORRELATION,
                confidence = Confidence.CALIBRATED,
                supportingFactIds = emptyList()
            ))
        }

        val strugglingHabits = habits.filter { it.frequency == HabitFrequency.DAILY && it.checkInsLast7d <= 2 && !it.isAdaptationPeriod }
        if (strugglingHabits.isNotEmpty()) {
            inferences.add(Inference(
                statement = "Hábitos em risco de abandono: ${strugglingHabits.size} (excluindo período de adaptação).",
                method = InferenceMethod.PATTERN_7D,
                confidence = Confidence.HEURISTIC,
                supportingFactIds = emptyList()
            ))
        }

        val recurringTasks = tasks.filter { it.state == TaskState.DONE && it.actualDurationMinutes > (it.estimatedEffortMinutes ?: 0) * 1.5 }
        if (recurringTasks.isNotEmpty()) {
            inferences.add(Inference(
                statement = "As estimativas de tempo estão a falhar em ${recurringTasks.size} tarefas (demoram >50% do previsto).",
                method = InferenceMethod.RULE,
                confidence = Confidence.CALIBRATED,
                supportingFactIds = emptyList()
            ))
        }

        return inferences
    }

    fun buildSuggestions(tasks: List<TaskSnapshot>, habits: List<HabitSnapshot>, inferences: List<Inference>): List<Suggestion> {
        val now = timeProvider.currentTimeMillis()
        val suggestions = mutableListOf<Suggestion>()

        val urgentTasks = tasks.filter { it.state == TaskState.ACTIVE && it.deadlineMillis != null && it.deadlineMillis!! - now in 0..(3 * dayMs) && it.timeReservedMinutes == null }
        if (urgentTasks.size >= 2) {
            val names = urgentTasks.take(2).joinToString(", ") { "'${it.titleShort}'" }
            // Ação: Adiar a tarefa com menor importância + urgência, ou a de maior estimativa
            val taskToDefer = urgentTasks.minByOrNull { it.importance + it.urgency } ?: urgentTasks.maxByOrNull { it.estimatedEffortMinutes ?: 30 }
            suggestions.add(Suggestion(
                statement = "Proteger tempo para ${urgentTasks.size} tarefas críticas",
                why = "Faltam menos de 3 dias para o prazo de $names e nenhuma tem tempo reservado.",
                impactDescription = "Reduz o risco de atraso em cerca de 70%.",
                approvalClass = ApprovalClass.A3_RELEVANT, targetModules = setOf("tasks"),
                impact = Impact.HIGH, alternatives = listOf("Reduzir âmbito", "Delegar"),
                reversibility = Reversibility.REVERSIBLE, expiresAtMillis = now + dayMs,
                action = taskToDefer?.let { SuggestionAction.DeferTask(it.id, 1) }
            ))
        }

        val overloadedCategory = tasks.filter { it.state == TaskState.ACTIVE && it.category != null }
            .groupBy { it.category }.filter { it.value.size > 3 }.keys.firstOrNull()
        if (overloadedCategory != null) {
            val taskToMove = tasks.filter { it.category == overloadedCategory && it.state == TaskState.ACTIVE }
                .minByOrNull { it.importance + it.urgency } 
            suggestions.add(Suggestion(
                statement = "Equilibrar a categoria '$overloadedCategory'",
                why = "Tens demasiadas tarefas acumuladas nesta área e outras categorias estão paradas.",
                impactDescription = "Evita o esgotamento numa única área da tua vida.",
                approvalClass = ApprovalClass.A2_LOW_IMPACT, targetModules = setOf("tasks"),
                impact = Impact.MEDIUM, alternatives = listOf("Pausar novas tarefas", "Focar numa por dia"),
                reversibility = Reversibility.REVERSIBLE, expiresAtMillis = now + 3 * dayMs,
                action = taskToMove?.let { SuggestionAction.DeferTask(it.id, 2) }
            ))
        }

        val habitRisk = habits.filter { it.frequency == HabitFrequency.DAILY && it.checkInsLast7d <= 2 && !it.isAdaptationPeriod }
        if (habitRisk.isNotEmpty()) {
            val names = habitRisk.take(2).joinToString(", ") { "'${it.nameShort}'" }
            val alt = if (habitRisk.first().preferredTime != null) "Mudar horário preferido" else "Definir dias fixos"
            val habitToChange = habitRisk.first()
            val newTime = if (habitToChange.preferredTime != null) "13:00" else "08:00"
            suggestions.add(Suggestion(
                statement = "Rever consistência de ${habitRisk.size} hábito(s)",
                why = "Hábitos como $names tiveram poucos check-ins esta semana.",
                impactDescription = "Aumenta a consistência sem gerar culpa.",
                approvalClass = ApprovalClass.A3_RELEVANT, targetModules = setOf("habits"),
                impact = Impact.MEDIUM, alternatives = listOf(alt, "Pausar temporariamente"),
                reversibility = Reversibility.REVERSIBLE, expiresAtMillis = now + 7 * dayMs,
                action = SuggestionAction.RescheduleHabit(habitToChange.id, newTime)
            ))
        }

        val energyTasks = tasks.filter { it.state == TaskState.ACTIVE && it.energy >= 3 && it.deadlineMillis != null && it.deadlineMillis!! - now in 0..(2 * dayMs) }
        if (energyTasks.size >= 3) {
            suggestions.add(Suggestion(
                statement = "Espaçar tarefas de alta energia",
                why = "Tens ${energyTasks.size} tarefas de alta energia para os próximos 2 dias.",
                impactDescription = "Melhora o foco e evita fadiga mental.",
                approvalClass = ApprovalClass.A2_LOW_IMPACT, targetModules = setOf("tasks"),
                impact = Impact.MEDIUM, alternatives = listOf("Mover para a semana seguinte", "Dividir em partes"),
                reversibility = Reversibility.REVERSIBLE, expiresAtMillis = now + dayMs
            ))
        }

        val completedTasks = tasks.filter { it.state == TaskState.DONE }
        val timeSpent = completedTasks.sumOf { it.actualDurationMinutes }
        val timeEstimated = completedTasks.sumOf { it.estimatedEffortMinutes ?: 0 }
        if (timeEstimated > 0 && timeSpent > timeEstimated * 1.3 && completedTasks.isNotEmpty()) {
            suggestions.add(Suggestion(
                statement = "Recalibrar as tuas estimativas",
                why = "Tens demorado em média ${timeSpent / completedTasks.size} min em vez de ${timeEstimated / completedTasks.size} min.",
                impactDescription = "Planeamento mais realista e menos frustração.",
                approvalClass = ApprovalClass.A1_PREPARE, targetModules = setOf("tasks"),
                impact = Impact.MEDIUM, alternatives = listOf("Adicionar 20% de buffer", "Ignorar por agora"),
                reversibility = Reversibility.REVERSIBLE, expiresAtMillis = now + 7 * dayMs
            ))
        }

        return suggestions.shuffled().take(5)
    }
}
