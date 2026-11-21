package com.lumiere.app.core.engine

import com.lumiere.app.core.contract.HabitSnapshot
import com.lumiere.app.core.contract.TaskSnapshot
import com.lumiere.app.core.contract.TaskState
import com.lumiere.app.core.model.Fact
import com.lumiere.app.core.model.FactSource
import com.lumiere.app.core.time.TimeProvider
import java.util.UUID

enum class ConflictKind { TEMPORAL_OVERLAP, DEPENDENCY_BLOCK, CAPACITY_EXCEEDED, STRICT_VIOLATION }
enum class ResolutionStatus { UNRESOLVED, RESOLVED_BY_USER, RESOLVED_BY_EXCEPTION, NOT_CONFIRMED }

data class TemporalConflict(
    val id: String,
    val kind: ConflictKind,
    val involvedRecordIds: List<String>,
    val evidence: List<Fact>,
    val status: ResolutionStatus = ResolutionStatus.NOT_CONFIRMED
)

class StrictConflictChecker(
    private val timeProvider: TimeProvider
) {

    companion object {
        const val WEEKLY_CAPACITY_MINUTES = 40 * 60
    }

    fun detect(
        tasks: List<TaskSnapshot>,
        habits: List<HabitSnapshot>,
        facts: List<Fact>
    ): List<TemporalConflict> {
        val out = mutableListOf<TemporalConflict>()
        val now = timeProvider.currentTimeMillis()
        val active = tasks.filter { it.state == TaskState.ACTIVE }

        val totalMin = active.sumOf { (it.estimatedEffortMinutes ?: 0).toLong() }
        if (totalMin > WEEKLY_CAPACITY_MINUTES) {
            out += TemporalConflict(
                id = UUID.randomUUID().toString(),
                kind = ConflictKind.CAPACITY_EXCEEDED,
                involvedRecordIds = active.map { it.id },
                evidence = facts.filter { (it.source as? FactSource.FromModule)?.moduleId == "tasks" }
            )
        }

        val byId = tasks.associateBy { it.id }
        active.forEach { t ->
            t.dependencyIds.forEach { depId ->
                val dep = byId[depId]
                if (dep != null && dep.state != TaskState.DONE) {
                    out += TemporalConflict(
                        id = UUID.randomUUID().toString(),
                        kind = ConflictKind.DEPENDENCY_BLOCK,
                        involvedRecordIds = listOf(t.id, depId),
                        evidence = emptyList()
                    )
                }
            }
        }

        active.forEach { t ->
            if (t.deadlineMillis != null && t.deadlineMillis < now) {
                out += TemporalConflict(
                    id = UUID.randomUUID().toString(),
                    kind = ConflictKind.STRICT_VIOLATION,
                    involvedRecordIds = listOf(t.id),
                    evidence = emptyList(),
                    status = ResolutionStatus.NOT_CONFIRMED
                )
            }
        }

        return out
    }
}
