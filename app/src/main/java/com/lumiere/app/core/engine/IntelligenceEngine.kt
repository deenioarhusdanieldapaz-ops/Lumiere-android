package com.lumiere.app.core.engine

import com.lumiere.app.core.contract.HabitContract
import com.lumiere.app.core.contract.TaskContract
import com.lumiere.app.core.memory.CoreMemoryRepository
import com.lumiere.app.core.memory.MemoryType
import com.lumiere.app.core.model.*
import com.lumiere.app.core.time.TimeProvider
import com.lumiere.app.data.local.dao.HabitDao
import com.lumiere.app.data.local.dao.TaskDao

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
    suspend fun analyzeWeek(): Result<Recommendation> = runCatching {
        val now = timeProvider.currentTimeMillis()
        val tasks = taskContract.getActiveTasks(windowDays = 14).getOrThrow()
        val habits = habitContract.getActiveHabits().getOrThrow()
        val facts = rules.buildFacts(tasks, habits)
        val inferences = rules.buildInferences(tasks, habits, facts)
        val suggestions = rules.buildSuggestions(tasks, habits, inferences)
        
        memory.put("last_analysis_at", now.toString(), MemoryType.SESSION, Sensitivity.NORMAL, 2 * 3_600_000L)
        
        Recommendation(
            kind = RecommendationKind.OPTIMIZATION,
            goal = "Organizar a semana",
            facts = facts,
            inferences = inferences,
            suggestions = suggestions,
            affectedModules = setOf("tasks", "habits"),
            createdAtMillis = now
        )
    }

    suspend fun detectConflicts(): Result<List<TemporalConflict>> = runCatching {
        val tasks = taskContract.getActiveTasks(windowDays = 14).getOrThrow()
        val habits = habitContract.getActiveHabits().getOrThrow()
        val facts = rules.buildFacts(tasks, habits)
        strictChecker.detect(tasks, habits, facts)
    }

    suspend fun approveSuggestion(suggestion: Suggestion): Result<Unit> = runCatching {
        val req = approvalGateway.request(suggestion).getOrThrow()
        val decision = ApprovalDecision(req.id, ApprovalDecisionKind.APPROVED, "user", timeProvider.currentTimeMillis())
        approvalGateway.recordDecision(decision).getOrThrow()

        when (val act = suggestion.action) {
            is SuggestionAction.DeferTask -> {
                val id = act.taskId.toLongOrNull()
                if (id != null) {
                    val t = taskDao.getTaskById(id)
                    if (t != null) {
                        val newDate = (t.dueDate ?: timeProvider.currentTimeMillis()) + (act.days * 86_400_000L)
                        taskDao.updateTask(t.copy(dueDate = newDate, updatedAt = timeProvider.currentTimeMillis()))
                    }
                }
            }
            is SuggestionAction.RescheduleHabit -> {
                val id = act.habitId.toLongOrNull()
                if (id != null) {
                    val h = habitDao.getHabitById(id)
                    if (h != null) habitDao.updateHabit(h.copy(preferredTime = act.newTime))
                }
            }
            is SuggestionAction.AdjustEstimate -> {
                val id = act.taskId.toLongOrNull()
                if (id != null) {
                    val t = taskDao.getTaskById(id)
                    if (t != null) taskDao.updateTask(t.copy(estimatedDurationMinutes = act.newEstimateMinutes, updatedAt = timeProvider.currentTimeMillis()))
                }
            }
            null -> {}
        }
    }

    suspend fun rejectSuggestion(suggestion: Suggestion): Result<Unit> = runCatching {
        val req = approvalGateway.request(suggestion).getOrThrow()
        val decision = ApprovalDecision(req.id, ApprovalDecisionKind.REJECTED, "user", timeProvider.currentTimeMillis())
        approvalGateway.recordDecision(decision).getOrThrow()
    }
}
