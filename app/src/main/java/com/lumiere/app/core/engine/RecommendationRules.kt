package com.lumiere.app.core.engine

import com.lumiere.app.core.contract.HabitFrequency
import com.lumiere.app.core.contract.HabitSnapshot
import com.lumiere.app.core.contract.TaskSnapshot
import com.lumiere.app.core.contract.TaskState
import com.lumiere.app.core.model.*
import com.lumiere.app.core.time.TimeProvider

class RecommendationRules(
    private val timeProvider: TimeProvider
) {

    private val dayMillis = 86_400_000L

    fun buildFacts(tasks: List<TaskSnapshot>, habits: List<HabitSnapshot>): List<Fact> {
        val facts = mutableListOf<Fact>()
        tasks.forEach { t ->
            facts += Fact(
                statement = "Tarefa '${t.titleShort}' — estado ${t.state}",
                source = FactSource.FromModule("tasks", t.id, TASK_CONTRACT_VERSION, t.lastUpdatedMillis),
                sensitivity = t.sensitivity
            )
        }
        habits.forEach { h ->
            facts += Fact(
                statement = "Hábito '${h.nameShort}' — ${h.checkInsLast7d} check-ins nos últimos 7d, streak ${h.streakCurrent}",
                source = FactSource.FromModule("habits", h.id, HABIT_CONTRACT_VERSION, h.lastUpdatedMillis),
                sensitivity = h.sensitivity
            )
        }
        return facts
    }

    fun buildInferences(
        tasks: List<TaskSnapshot>,
        habits: List<HabitSnapshot>,
        facts: List<Fact>
    ): List<Inference> {
        val now = timeProvider.currentTimeMillis()
        val out = mutableListOf<Inference>()

        tasks.filter { it.state == TaskState.ACTIVE && it.deadlineMillis != null }.forEach { t ->
            val hoursLeft = (t.deadlineMillis!! - now) / 3_600_000L
            val effort = t.estimatedEffortMinutes ?: 0
            if (hoursLeft in 0..72 && effort >= 120 && t.priorityDeclared >= 2) {
                out += Inference(
                    statement = "Risco de atraso em '${t.titleShort}'",
                    method = InferenceMethod.RULE,
                    confidence = Confidence.HEURISTIC,
                    supportingFactIds = facts.filter {
                        (it.source as? FactSource.FromModule)?.recordId == t.id
                    }.map { it.id }
                )
            }
        }

        habits.filter { it.frequency == HabitFrequency.DAILY }.forEach { h ->
            if (h.checkInsLast7d <= 2 && h.streakCurrent == 0) {
                out += Inference(
                    statement = "Hábito '${h.nameShort}' em risco de abandono",
                    method = InferenceMethod.PATTERN_7D,
                    confidence = Confidence.HEURISTIC,
                    supportingFactIds = facts.filter {
                        (it.source as? FactSource.FromModule)?.recordId == h.id
                    }.map { it.id }
                )
            }
        }

        val totalEffort = tasks.filter { it.state == TaskState.ACTIVE }
            .sumOf { (it.estimatedEffortMinutes ?: 0).toLong() }
        if (totalEffort > 0) {
            out += Inference(
                statement = "Esforço total estimado: ${totalEffort}min (~${totalEffort / 60}h)",
                method = InferenceMethod.RULE,
                confidence = Confidence.HEURISTIC,
                supportingFactIds = emptyList()
            )
        }

        return out
    }

    fun buildSuggestions(
        tasks: List<TaskSnapshot>,
        habits: List<HabitSnapshot>,
        inferences: List<Inference>
    ): List<Suggestion> {
        val now = timeProvider.currentTimeMillis()
        val out = mutableListOf<Suggestion>()

        tasks.filter { it.state == TaskState.ACTIVE && it.priorityDeclared >= 2 && it.deadlineMillis != null }
            .sortedBy { it.deadlineMillis }
            .take(3)
            .forEach { t ->
                out += Suggestion(
                    statement = "Reservar bloco para '${t.titleShort}' antes do deadline",
                    approvalClass = ApprovalClass.A3_RELEVANT,
                    targetModules = setOf("tasks"),
                    impact = Impact.MEDIUM,
                    alternatives = listOf("Reduzir âmbito", "Negociar deadline"),
                    reversibility = Reversibility.REVERSIBLE,
                    expiresAtMillis = t.deadlineMillis
                )
            }

        habits.filter { it.frequency == HabitFrequency.DAILY && it.checkInsLast7d <= 2 && it.streakCurrent == 0 }
            .forEach { h ->
                out += Suggestion(
                    statement = "Reduzir '${h.nameShort}' para 3x/semana esta semana",
                    approvalClass = ApprovalClass.A3_RELEVANT,
                    targetModules = setOf("habits"),
                    impact = Impact.MEDIUM,
                    alternatives = listOf("Manter frequência", "Pausar 7 dias"),
                    reversibility = Reversibility.REVERSIBLE,
                    expiresAtMillis = now + 7 * dayMillis
                )
            }

        return out
    }

    companion object {
        const val TASK_CONTRACT_VERSION = "1.0.0"
        const val HABIT_CONTRACT_VERSION = "1.0.0"
    }
}
