package com.lumiere.app.core.engine

import com.lumiere.app.core.model.Fact
import com.lumiere.app.core.model.Schedulable
import com.lumiere.app.core.time.TimeProvider
import java.util.UUID

enum class ConflictKind { TEMPORAL_OVERLAP, CAPACITY_EXCEEDED_DAILY, CAPACITY_EXCEEDED_WEEKLY, STRICT_VIOLATION }
enum class ResolutionStatus { UNRESOLVED, RESOLVED_BY_USER, NOT_CONFIRMED }

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

    // Recebe APENAS List<Schedulable>, não sabe se é Tarefa ou Hábito!
    fun detect(items: List<Schedulable>, facts: List<Fact>): List<TemporalConflict> {
        val out = mutableListOf<TemporalConflict>()
        val now = timeProvider.currentTimeMillis()
        val dayMs = 86_400_000L

        // 1. Capacidade Total Semanal (Tarefas + Hábitos)
        val totalMin = items.sumOf { (it.durationMinutes ?: 0).toLong() }
        if (totalMin > WEEKLY_CAPACITY_MINUTES) {
            out.add(TemporalConflict(
                kind = ConflictKind.CAPACITY_EXCEEDED_WEEKLY, 
                severity = 3, 
                title = "Semana Sobrecarregada", 
                details = "Tens ${totalMin / 60}h de compromissos esta semana. Excede o limite de 40h.", 
                involvedRecordIds = items.map { it.id }
            ))
        }

        // 2. Capacidade Total Diária
        items.filter { it.startTimeMillis != null }.groupBy { it.startTimeMillis!! / dayMs }.forEach { (_, dayItems) ->
            val dailyMin = dayItems.sumOf { it.durationMinutes ?: 0 }
            if (dailyMin > DAILY_CAPACITY_MINUTES) {
                out.add(TemporalConflict(
                    kind = ConflictKind.CAPACITY_EXCEEDED_DAILY, 
                    severity = 2, 
                    title = "Dia Sobrecarregado", 
                    details = "Tens ${dailyMin / 60}h de esforço num único dia (Limite: 8h).", 
                    involvedRecordIds = dayItems.map { it.id }
                ))
            }
        }

        // 3. Colisão de Horários e Energia (O Verdadeiro Cross-Module)
        val scheduledItems = items.filter { it.startTimeMillis != null }
        scheduledItems.forEachIndexed { i, itemA ->
            for (j in i + 1 until scheduledItems.size) {
                val itemB = scheduledItems[j]
                
                // Diferença de horário em minutos
                val diffMins = Math.abs(itemA.startTimeMillis!! - itemB.startTimeMillis!!) / 60_000
                
                // Se começam com menos de 30min de diferença, há sobreposição
                if (diffMins < 30) {
                    val combinedEnergy = itemA.energyCost + itemB.energyCost
                    val isCrossModule = itemA.moduleId != itemB.moduleId
                    
                    if (combinedEnergy >= 5) { // Alta fadiga
                        out.add(TemporalConflict(
                            kind = ConflictKind.TEMPORAL_OVERLAP, 
                            severity = 3, 
                            title = if (isCrossModule) "Fadiga Cross-Module" else "Sobreposição de Alta Energia", 
                            details = "'${itemA.title}' e '${itemB.title}' exigem demasiada energia ao mesmo tempo.", 
                            involvedRecordIds = listOf(itemA.id, itemB.id)
                        ))
                    } else if (isCrossModule) {
                         out.add(TemporalConflict(
                            kind = ConflictKind.TEMPORAL_OVERLAP, 
                            severity = 2, 
                            title = "Cruzamento de Módulos", 
                            details = "'${itemA.title}' (${itemA.moduleId}) coincide com '${itemB.title}' (${itemB.moduleId}).", 
                            involvedRecordIds = listOf(itemA.id, itemB.id)
                        ))
                    }
                }
            }
        }

        return out.sortedByDescending { it.severity }
    }
}
