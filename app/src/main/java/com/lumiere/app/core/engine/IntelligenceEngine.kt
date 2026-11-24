package com.lumiere.app.core.engine

import com.lumiere.app.core.contract.HabitContract
import com.lumiere.app.core.contract.TaskContract
import com.lumiere.app.core.memory.CoreMemoryRepository
import com.lumiere.app.core.memory.MemoryType
import com.lumiere.app.core.model.*
import com.lumiere.app.core.time.TimeProvider

class IntelligenceEngine(
    private val taskContract: TaskContract,
    private val habitContract: HabitContract,
    private val rules: RecommendationRules,
    private val strictChecker: StrictConflictChecker,
    private val approvalGateway: ApprovalGateway,
    private val memory: CoreMemoryRepository,
    private val timeProvider: TimeProvider
) {
    private suspend fun getContext(): List<Schedulable> {
        val tasks = taskContract.getActiveTasks(14).getOrThrow()
        val habits = habitContract.getActiveHabits().getOrThrow()
        return tasks + habits
    }

    suspend fun analyzeWeek(): Result<Recommendation> = runCatching {
        val now = timeProvider.currentTimeMillis()
        val context = getContext()
        val facts = rules.buildFacts(context)
        val inferences = rules.buildInferences(context, facts)
        val suggestions = rules.buildSuggestions(context, inferences)
        Recommendation(
            kind = RecommendationKind.OPTIMIZATION,
            goal = "Organizar a semana",
            facts = facts,
            inferences = inferences,
            suggestions = suggestions,
            affectedModules = context.map { it.moduleId }.toSet(),
            createdAtMillis = now
        )
    }.onSuccess { result ->
        runCatching { memory.put("last_analysis_at", result.createdAtMillis.toString(), MemoryType.SESSION, Sensitivity.NORMAL, 2 * 3_600_000L) }
    }

    suspend fun detectConflicts(): Result<List<TemporalConflict>> = runCatching {
        val context = getContext()
        val facts = rules.buildFacts(context)
        strictChecker.detect(context, facts)
    }

    suspend fun approveSuggestion(suggestion: Suggestion): Result<Unit> = runCatching {
        val now = timeProvider.currentTimeMillis()
        if (suggestion.expiresAtMillis != null && suggestion.expiresAtMillis < now) throw IllegalStateException("Suggestion expired")
        val req = approvalGateway.request(suggestion).getOrThrow()
        val (applied, failed) = applyAction(suggestion.action)
        val kind = when {
            failed == 0 -> ApprovalDecisionKind.APPROVED
            applied == 0 -> ApprovalDecisionKind.FAILED
            else -> ApprovalDecisionKind.PARTIAL
        }
        val decision = ApprovalDecision(req.id, kind, "user", timeProvider.currentTimeMillis())
        approvalGateway.recordDecision(decision).getOrThrow()
    }

    private suspend fun applyAction(action: SuggestionAction?): Pair<Int, Int> {
        if (action == null) return 0 to 0
        var applied = 0
        var failed = 0
        when (action) {
            is SuggestionAction.DeferItem -> {
                if (action.moduleId == "M01_TASKS") {
                    val taskResult = taskContract.getTask(action.itemId)
                    val task = taskResult.getOrNull()
                    if (task != null) {
                        val now = timeProvider.currentTimeMillis()
                        val newDate = (task.deadlineMillis ?: now) + (action.days * 86_400_000L)
                        if (taskContract.updateTaskDueDate(action.itemId, newDate).isSuccess) applied++ else failed++
                    } else failed++
                }
            }
            is SuggestionAction.BatchDeferItems -> {
                if (action.moduleId == "M01_TASKS") {
                    action.itemIds.forEach { rawId ->
                        val taskResult = taskContract.getTask(rawId)
                        val task = taskResult.getOrNull()
                        if (task != null) {
                            val now = timeProvider.currentTimeMillis()
                            val newDate = (task.deadlineMillis ?: now) + (action.days * 86_400_000L)
                            if (taskContract.updateTaskDueDate(rawId, newDate).isSuccess) applied++ else failed++
                        } else failed++
                    }
                }
            }
            is SuggestionAction.RescheduleItem -> { }
            is SuggestionAction.ShortenDuration -> { }
            is SuggestionAction.ChangeFrequency -> { }
        }
        return applied to failed
    }

    suspend fun getAllDecisions(): Result<List<ApprovalDecision>> = approvalGateway.getAllDecisions()
    suspend fun sweepExpired(): Result<Unit> = memory.sweepExpired()

    suspend fun rejectSuggestion(suggestion: Suggestion): Result<Unit> = runCatching {
        val req = approvalGateway.request(suggestion).getOrThrow()
        val decision = ApprovalDecision(req.id, ApprovalDecisionKind.REJECTED, "user", timeProvider.currentTimeMillis())
        approvalGateway.recordDecision(decision).getOrThrow()
    }
}
