package com.lumiere.app.core.engine

import com.lumiere.app.core.contract.HabitSnapshot
import com.lumiere.app.core.contract.TaskSnapshot
import com.lumiere.app.core.contract.TaskState
import com.lumiere.app.core.model.Fact
import com.lumiere.app.core.model.FactSource
import com.lumiere.app.core.time.TimeProvider
import java.util.UUID

enum class ConflictKind { TEMPORAL_OVERLAP, DEPENDENCY_BLOCK, CAPACITY_EXCEEDED_DAILY, CAPACITY_EXCEEDED_WEEKLY, STRICT_VIOLATION, HABIT_CLASH }
enum class ResolutionStatus { UNRESOLVED, RESOLVED_BY_USER, RESOLVED_BY_EXCEPTION, NOT_CONFIRMED }

data class TemporalConflict(
    val id: String,
    val kind: ConflictKind,
    val severity: Int, // 1=baixa, 2=média, 3=alta
    val title: String,
    val details: String,
    val involvedRecordIds: List<String>,
    val evidence: List<Fact>,
    val status: ResolutionStatus = ResolutionStatus.NOT_CONFIRMED
)

class StrictConflictChecker(private val timeProvider: TimeProvider) {
    companion object {
        const val WEEKLY_CAPACITY_MINUTES = 40 * 60
        const val DAILY_CAPACITY_MINUTES = 8 * 60
    }

    fun detect(tasks: List<TaskSnapshot>, habits: List<HabitSnapshot>, facts: List<Fact>): List<TemporalConflict> {
        val out = mutableListOf<TemporalConflict>()
        val now = timeProvider.currentTimeMillis()
        val dayMs = 86_400_000L
        val active = tasks.filter { it.state == TaskState.ACTIVE }

        val totalMin = active.sumOf { (it.estimatedEffortMinutes ?: 0).toLong() }
        if (totalMin > WEEKLY_CAPACITY_MINUTES) {
            out.add(TemporalConflict(UUID.randomUUID().toString(), ConflictKind.CAPACITY_EXCEEDED_WEEKLY, 3, "Semana Sobrecarregada", "Excede a capacidade semanal em ${totalMin - WEEKLY_CAPACITY_MINUTES} min.", active.map { it.id }, emptyList()))
        }

        active.filter { it.deadlineMillis != null }.groupBy { it.deadlineMillis!! / dayMs }.forEach { (day, dayTasks) ->
            val dailyMin = dayTasks.sumOf { it.estimatedEffortMinutes ?: 0 }
            if (dailyMin > DAILY_CAPACITY_MINUTES) {
                out.add(TemporalConflict(UUID.randomUUID().toString(), ConflictKind.CAPACITY_EXCEEDED_DAILY, 2, "Dia Sobrecarregado", "Tens mais de 8h de esforço no dia ${day * dayMs}.", dayTasks.map { it.id }, emptyList()))
            }
        }

        active.filter { it.deadlineMillis != null && it.deadlineMillis < now }.forEach { t ->
            val daysLate = (now - t.deadlineMillis!!) / dayMs
            val severity = if (daysLate > 3) 3 else 2
            out.add(TemporalConflict(UUID.randomUUID().toString(), ConflictKind.STRICT_VIOLATION, severity, "Prazo Ultrapassado", "'${t.titleShort}' está atrasada há $daysLate dia(s).", listOf(t.id), emptyList()))
        }

        val timeMap = habits.filter { it.preferredTime != null }.groupBy { it.preferredTime }
        timeMap.forEach { (time, habitList) ->
            if (habitList.size > 1) {
                val names = habitList.joinToString(" e ") { "'${it.nameShort}'" }
                out.add(TemporalConflict(UUID.randomUUID().toString(), ConflictKind.HABIT_CLASH, 1, "Colisão de Horário", "$names estão agendados para o mesmo período ($time).", habitList.map { it.id }, emptyList()))
            }
        }

        return out.sortedByDescending { it.severity }
    }
}
