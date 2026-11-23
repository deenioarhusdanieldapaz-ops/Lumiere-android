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
import java.util.Locale

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
        val tasks = taskContract.getActiveTasks(14).getOrThrow()
        val habits = habitContract.getActiveHabits().getOrThrow()
        return tasks + habits
    }

    suspend fun analyzeWeek(): Result<Recommendation> {
        val result = runCatching {
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
        }
        if (result.isSuccess) {
            runCatching {
                memory.put("last_analysis_at", result.getOrThrow().createdAtMillis.toString(), MemoryType.SESSION, Sensitivity.NORMAL, 2 * 3_600_000L)
            }
        }
        return result
    }

    suspend fun detectConflicts(): Result<List<TemporalConflict>> = runCatching {
        val context = getContext()
        val facts = rules.buildFacts(context)
        strictChecker.detect(context, facts)
    }

    /**
     * Aprova uma sugestão seguindo a ordem: validar -> mutar -> gravar decisão.
     * Nunca grava APPROVED antes de saber se a mutação correu.
     */
    suspend fun approveSuggestion(suggestion: Suggestion): Result<Unit> = runCatching {
        // F11: validar expiração antes de qualquer efeito colateral
        val now = timeProvider.currentTimeMillis()
        val expiresAt = suggestion.expiresAtMillis
        if (expiresAt != null && expiresAt < now) {
            throw IllegalStateException("Suggestion expired at $expiresAt (now=$now)")
        }

        // Criar request (a UI já foi informada; se falhar aqui, nada foi mutado)
        val req = approvalGateway.request(suggestion).getOrThrow()

        // Aplicar mutações com contagem (F1)
        val (applied, failed) = applyAction(suggestion.action)

        // Determinar o resultado real (F1)
        val kind = when {
            failed == 0 -> ApprovalDecisionKind.APPROVED
            applied == 0 -> ApprovalDecisionKind.FAILED
            else -> ApprovalDecisionKind.PARTIAL
        }

        // Gravar decisão com o resultado real
        val decision = ApprovalDecision(req.id, kind, "user", timeProvider.currentTimeMillis())
        approvalGateway.recordDecision(decision).getOrThrow()
        Unit
    }

    private suspend fun applyAction(action: SuggestionAction?): Pair<Int, Int> {
        if (action == null) return 0 to 0
        var applied = 0
        var failed = 0
        when (action) {
            is SuggestionAction.DeferItem -> {
                if (action.moduleId == "M01_TASKS") {
                    if (applyDeferTask(action.itemId, action.days)) applied++ else failed++
                }
            }
            is SuggestionAction.BatchDeferItems -> {
                if (action.moduleId == "M01_TASKS") {
                    action.itemIds.forEach { rawId ->
                        if (applyDeferTask(rawId, action.days)) applied++ else failed++
                    }
                }
            }
            is SuggestionAction.RescheduleItem -> {
                if (action.moduleId == "M02_HABITS") {
                    if (applyRescheduleHabit(action.itemId, action.newTimeMillis)) applied++ else failed++
                }
            }
            is SuggestionAction.ShortenDuration -> {
                if (action.moduleId == "M02_HABITS") {
                    if (applyShortenDuration(action.itemId, action.newDurationMinutes)) applied++ else failed++
                }
            }
            is SuggestionAction.ChangeFrequency -> {
                if (action.moduleId == "M02_HABITS") {
                    if (applyChangeFrequency(action.itemId, action.newFrequency)) applied++ else failed++
                }
            }
        }
        return applied to failed
    }

    private suspend fun applyDeferTask(rawId: String, days: Int): Boolean = try {
        val id = rawId.toLongOrNull()
        if (id == null) false
        else {
            val t = taskDao.getTaskById(id)
            if (t == null) false
            else {
                val now = timeProvider.currentTimeMillis()
                val newDate = (t.dueDate ?: now) + (days * 86_400_000L)
                taskDao.updateTask(t.copy(dueDate = newDate, updatedAt = now))
                true
            }
        }
    } catch (e: Exception) { false }

    private suspend fun applyRescheduleHabit(rawId: String, newTimeMillis: Long): Boolean = try {
        val id = rawId.toLongOrNull()
        if (id == null) false
        else {
            val h = habitDao.getHabitById(id)
            if (h == null) false
            else {
                val cal = Calendar.getInstance()
                cal.timeInMillis = newTimeMillis
                val newTime = String.format(Locale.ROOT, "%02d:%02d",
                    cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
                habitDao.updateHabit(h.copy(preferredTime = newTime))
                true
            }
        }
    } catch (e: Exception) { false }

    private suspend fun applyShortenDuration(rawId: String, newDurationMinutes: Int): Boolean = try {
        val id = rawId.toLongOrNull()
        if (id == null) false
        else {
            val h = habitDao.getHabitById(id)
            if (h == null) false
            else {
                habitDao.updateHabit(h.copy(durationMinutes = newDurationMinutes))
                true
            }
        }
    } catch (e: Exception) { false }

    private suspend fun applyChangeFrequency(rawId: String, newFrequency: String): Boolean = try {
        val id = rawId.toLongOrNull()
        if (id == null) false
        else {
            val h = habitDao.getHabitById(id)
            if (h == null) false
            else {
                habitDao.updateHabit(h.copy(frequencyType = newFrequency))
                true
            }
        }
    } catch (e: Exception) { false }

    suspend fun getAllDecisions(): Result<List<ApprovalDecision>> = approvalGateway.getAllDecisions()

    /**
     * R6: apaga decisões expiradas do armazenamento persistente.
     * Chamado em IntelligenceViewModel.init, sem bloquear o arranque.
     */
    suspend fun sweepExpired(): Result<Unit> = memory.sweepExpired()

    suspend fun rejectSuggestion(suggestion: Suggestion): Result<Unit> = runCatching {
        val req = approvalGateway.request(suggestion).getOrThrow()
        val decision = ApprovalDecision(req.id, ApprovalDecisionKind.REJECTED, "user", timeProvider.currentTimeMillis())
        approvalGateway.recordDecision(decision).getOrThrow()
        Unit
    }
}
