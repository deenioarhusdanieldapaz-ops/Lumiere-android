package com.lumiere.app.core.engine

import com.lumiere.app.core.contract.HabitContract
import com.lumiere.app.core.contract.TaskContract
import com.lumiere.app.core.memory.CoreMemoryRepository
import com.lumiere.app.core.memory.MemoryType
import com.lumiere.app.core.model.*
import com.lumiere.app.core.time.TimeProvider
import com.lumiere.app.data.local.dao.HabitDao
import com.lumiere.app.data.local.dao.TaskDao
import java.util.Calendar

class IntelligenceEngine(
    private val taskContract: TaskContract,
    private val habitContract: HabitContract,
    private val rules: RecommendationRules,
    private val strictChecker: StrictConflictChecker,
    private val approvalGateway: ApprovalGateway,
    private val memory: CoreMemoryRepository,
    private val timeProvider: TimeProvider,
    private val taskDao: TaskDao,
    private val habitDao: HabitDao
) {
    private suspend fun getContext(): List<Schedulable> {
        val tasks = taskContract.getActiveTasks(14).getOrElse { emptyList() }
        val habits = habitContract.getActiveHabits().getOrElse { emptyList() }
        return tasks + habits
    }

    suspend fun analyzeWeek(): Result<Recommendation> = runCatching {
        val now = timeProvider.currentTimeMillis()
        val context = getContext()
        val facts = rules.buildFacts(context)
        val inferences = rules.buildInferences(context, facts)
        val suggestions = rules.buildSuggestions(context, inferences)
        
        memory.put("last_analysis_at", now.toString(), MemoryType.SESSION, Sensitivity.NORMAL, 2 * 3_600_000L)
        
        Recommendation(
            kind = RecommendationKind.OPTIMIZATION,
            goal = "Organizar a semana",
            facts = facts,
            inferences = inferences,
            suggestions = suggestions,
            affectedModules = context.map { it.moduleId }.toSet(),
            createdAtMillis = now
        )
    }

    suspend fun detectConflicts(): Result<List<TemporalConflict>> = runCatching {
        val context = getContext()
        val facts = rules.buildFacts(context)
        strictChecker.detect(context, facts)
    }

    suspend fun approveSuggestion(suggestion: Suggestion): Result<Unit> = runCatching {
        val req = approvalGateway.request(suggestion).getOrThrow()
        val decision = ApprovalDecision(req.id, ApprovalDecisionKind.APPROVED, "user", timeProvider.currentTimeMillis())
        approvalGateway.recordDecision(decision).getOrThrow()

        when (val act = suggestion.action) {
            is SuggestionAction.DeferItem -> {
                if (act.moduleId == "M01_TASKS") {
                    val id = act.itemId.toLongOrNull()
                    if (id != null) {
                        val t = taskDao.getTaskById(id)
                        if (t != null) {
                            val newDate = (t.dueDate ?: timeProvider.currentTimeMillis()) + (act.days * 86_400_000L)
                            taskDao.updateTask(t.copy(dueDate = newDate, updatedAt = timeProvider.currentTimeMillis()))
                        }
                    }
                }
            }
            is SuggestionAction.RescheduleItem -> {
                if (act.moduleId == "M02_HABITS") {
                    val id = act.itemId.toLongOrNull()
                    if (id != null) {
                        val h = habitDao.getHabitById(id)
                        if (h != null) {
                            val cal = Calendar.getInstance()
                            cal.timeInMillis = act.newTimeMillis
                            val newTime = String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
                            habitDao.updateHabit(h.copy(preferredTime = newTime))
                        }
                    }
                }
            }
            is SuggestionAction.ShortenDuration -> {
                if (act.moduleId == "M02_HABITS") {
                    val id = act.itemId.toLongOrNull()
                    if (id != null) {
                        val h = habitDao.getHabitById(id)
                        if (h != null) habitDao.updateHabit(h.copy(durationMinutes = act.newDurationMinutes))
                    }
                }
            }
            is SuggestionAction.ChangeFrequency -> {
                if (act.moduleId == "M02_HABITS") {
                    val id = act.itemId.toLongOrNull()
                    if (id != null) {
                        val h = habitDao.getHabitById(id)
                        if (h != null) habitDao.updateHabit(h.copy(frequencyType = act.newFrequency))
                    }
                }
            }
            else -> {}
        }
    }

    suspend fun rejectSuggestion(suggestion: Suggestion): Result<Unit> = runCatching {
        val req = approvalGateway.request(suggestion).getOrThrow()
        val decision = ApprovalDecision(req.id, ApprovalDecisionKind.REJECTED, "user", timeProvider.currentTimeMillis())
        approvalGateway.recordDecision(decision).getOrThrow()
    }
}
