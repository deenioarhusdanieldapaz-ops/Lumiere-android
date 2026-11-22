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

    suspend fun analyzeWeek(): Result<Recommendation> = runCatching {
        val now = timeProvider.currentTimeMillis()
        val tasks = taskContract.getActiveTasks(windowDays = 14).getOrThrow()
        val habits = habitContract.getActiveHabits().getOrThrow()
        val facts = rules.buildFacts(tasks, habits)
        val inferences = rules.buildInferences(tasks, habits, facts)
        val suggestions = rules.buildSuggestions(tasks, habits, inferences)
        memory.put(
            key = "last_analysis_at",
            value = now.toString(),
            type = MemoryType.SESSION,
            sensitivity = Sensitivity.NORMAL,
            ttlMillis = 2 * 3_600_000L
        )
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

    suspend fun executeApproved(
        suggestion: Suggestion,
        decision: ApprovalDecision
    ): Result<Unit> = runCatching {
        require(decision.decision == ApprovalDecisionKind.APPROVED) { "Not approved" }
        approvalGateway.recordDecision(decision).getOrThrow()
    }

    suspend fun requestApproval(suggestion: Suggestion): Result<ApprovalRequest> =
        approvalGateway.request(suggestion)

    suspend fun approveSuggestion(suggestion: Suggestion): Result<Unit> = runCatching {
        val req = approvalGateway.request(suggestion).getOrThrow()
        val decision = ApprovalDecision(
            approvalId = req.id,
            decision = ApprovalDecisionKind.APPROVED,
            decidedBy = "user",
            decidedAtMillis = timeProvider.currentTimeMillis()
        )
        executeApproved(suggestion, decision).getOrThrow()
    }

    suspend fun rejectSuggestion(suggestion: Suggestion): Result<Unit> = runCatching {
        val req = approvalGateway.request(suggestion).getOrThrow()
        val decision = ApprovalDecision(
            approvalId = req.id,
            decision = ApprovalDecisionKind.REJECTED,
            decidedBy = "user",
            decidedAtMillis = timeProvider.currentTimeMillis()
        )
        approvalGateway.recordDecision(decision).getOrThrow()
    }
}
