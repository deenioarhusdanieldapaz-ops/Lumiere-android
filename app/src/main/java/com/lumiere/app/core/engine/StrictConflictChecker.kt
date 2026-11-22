package com.lumiere.app.core.engine

import com.lumiere.app.core.contract.HabitSnapshot
import com.lumiere.app.core.contract.TaskSnapshot
import com.lumiere.app.core.contract.TaskState
import com.lumiere.app.core.model.Fact
import com.lumiere.app.core.time.TimeProvider
import java.util.Calendar
import java.util.UUID

enum class ConflictKind { TEMPORAL_OVERLAP, DEPENDENCY_BLOCK, CAPACITY_EXCEEDED_DAILY, CAPACITY_EXCEEDED_WEEKLY, STRICT_VIOLATION, HABIT_CLASH }
enum class ResolutionStatus { UNRESOLVED, RESOLVED_BY_USER, RESOLVED_BY_EXCEPTION, NOT_CONFIRMED }

data class TemporalConflict(
    val id: String = UUID.randomUUID().toString(),
    val kind: ConflictKind,
    val severity: Int, // 1=baixa, 2=média, 3=alta
    val title: String,
    val details: String,
    val involvedRecordIds: List<String>,
    val evidence: List<Fact> = emptyList(),
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
        val activeTasks = tasks.filter { it.state == TaskState.ACTIVE }

        // 1. Capacidade Semanal e Diária
        val totalMin = activeTasks.sumOf { (it.estimatedEffortMinutes ?: 0).toLong() }
        if (totalMin > WEEKLY_CAPACITY_MINUTES) {
            out.add(TemporalConflict(kind = ConflictKind.CAPACITY_EXCEEDED_WEEKLY, severity = 3, title = "Semana Sobrecarregada", details = "Excede a capacidade semanal em ${totalMin - WEEKLY_CAPACITY_MINUTES} min.", involvedRecordIds = activeTasks.map { it.id }))
        }

        activeTasks.filter { it.deadlineMillis != null }.groupBy { it.deadlineMillis!! / dayMs }.forEach { (_, dayTasks) ->
            val dailyMin = dayTasks.sumOf { it.estimatedEffortMinutes ?: 0 }
            if (dailyMin > DAILY_CAPACITY_MINUTES) {
                out.add(TemporalConflict(kind = ConflictKind.CAPACITY_EXCEEDED_DAILY, severity = 2, title = "Dia Sobrecarregado", details = "Tens mais de 8h de esforço num único dia.", involvedRecordIds = dayTasks.map { it.id }))
            }
        }

        // 2. Prazos Ultrapassados
        activeTasks.filter { it.deadlineMillis != null && it.deadlineMillis < now }.forEach { t ->
            val daysLate = (now - t.deadlineMillis!!) / dayMs
            out.add(TemporalConflict(kind = ConflictKind.STRICT_VIOLATION, severity = if (daysLate > 3) 3 else 2, title = "Prazo Ultrapassado", details = "'${t.titleShort}' está atrasada há $daysLate dia(s).", involvedRecordIds = listOf(t.id)))
        }

        // 3. Colisão Intra-Hábitos
        habits.filter { it.preferredTime != null }.groupBy { it.preferredTime }.forEach { (time, habitList) ->
            if (habitList.size > 1) {
                val names = habitList.joinToString(" e ") { "'${it.nameShort}'" }
                out.add(TemporalConflict(kind = ConflictKind.HABIT_CLASH, severity = 1, title = "Colisão de Hábitos", details = "$names estão agendados para as $time.", involvedRecordIds = habitList.map { it.id }))
            }
        }

        // 4. O CROSS-MODULE (Tarefas vs Hábitos)
        val calendar = Calendar.getInstance()
        activeTasks.filter { it.deadlineMillis != null }.forEach { task ->
            calendar.timeInMillis = task.deadlineMillis!!
            val taskHour = calendar.get(Calendar.HOUR_OF_DAY)
            
            habits.filter { it.preferredTime != null }.forEach { habit ->
                val habitHour = habit.preferredTime!!.take(2).toIntOrNull() ?: -1
                if (taskHour == habitHour) {
                    out.add(TemporalConflict(
                        kind = ConflictKind.TEMPORAL_OVERLAP,
                        severity = 3,
                        title = "Cruzamento de Módulos",
                        details = "A tarefa '${task.titleShort}' coincide com o hábito '${habit.nameShort}' por volta das ${habitHour}h.",
                        involvedRecordIds = listOf(task.id, habit.id)
                    ))
                }
            }
        }

        return out.sortedByDescending { it.severity }
    }
}
