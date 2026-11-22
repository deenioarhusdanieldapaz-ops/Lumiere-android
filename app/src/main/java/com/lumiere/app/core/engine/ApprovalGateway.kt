package com.lumiere.app.core.engine

import com.lumiere.app.core.model.ApprovalClass
import com.lumiere.app.core.model.Reversibility
import com.lumiere.app.core.model.Suggestion
import com.lumiere.app.core.time.TimeProvider
import java.util.UUID

data class ApprovalScope(
    val modules: Set<String>,
    val durationMillis: Long?,
    val reusable: Boolean
)

data class ApprovalRequest(
    val id: String,
    val suggestionId: String,
    val approvalClass: ApprovalClass,
    val whatChanges: List<String>,
    val alternatives: List<String>,
    val scope: ApprovalScope,
    val reversibility: Reversibility,
    val expiresAtMillis: Long,
    val whatItDoesNotAuthorize: List<String>
)

enum class ApprovalDecisionKind { APPROVED, REJECTED, EXPIRED }

data class ApprovalDecision(
    val approvalId: String,
    val decision: ApprovalDecisionKind,
    val decidedBy: String,
    val decidedAtMillis: Long
)

interface ApprovalStore {
    suspend fun save(request: ApprovalRequest): Result<Unit>
    suspend fun get(requestId: String): Result<ApprovalRequest?>
    suspend fun saveDecision(decision: ApprovalDecision): Result<Unit>
    suspend fun getDecision(approvalId: String): Result<ApprovalDecision?>
}

class ApprovalGateway(
    private val store: ApprovalStore,
    private val timeProvider: TimeProvider
) {
    suspend fun request(suggestion: Suggestion): Result<ApprovalRequest> = runCatching {
        require(
            suggestion.approvalClass == ApprovalClass.A3_RELEVANT ||
            suggestion.approvalClass == ApprovalClass.A4_HIGH_IMPACT
        ) { "A0/A1/A2 do not require approval gateway" }

        val now = timeProvider.currentTimeMillis()
        val req = ApprovalRequest(
            id = UUID.randomUUID().toString(),
            suggestionId = suggestion.id,
            approvalClass = suggestion.approvalClass,
            whatChanges = listOf(suggestion.statement),
            alternatives = suggestion.alternatives,
            scope = ApprovalScope(suggestion.targetModules, null, false),
            reversibility = suggestion.reversibility,
            expiresAtMillis = suggestion.expiresAtMillis ?: (now + 3_600_000L),
            whatItDoesNotAuthorize = listOf(
                "Outros modulos alem de ${suggestion.targetModules}",
                "Acoes de classe superior a ${suggestion.approvalClass}"
            )
        )
        store.save(req).getOrThrow()
        req
    }

    suspend fun recordDecision(decision: ApprovalDecision): Result<Unit> =
        store.saveDecision(decision)

    suspend fun isApproved(approvalId: String): Result<Boolean> = runCatching {
        val request = store.get(approvalId).getOrThrow() ?: return@runCatching false
        if (request.expiresAtMillis < timeProvider.currentTimeMillis()) return@runCatching false
        store.getDecision(approvalId).getOrThrow()?.decision == ApprovalDecisionKind.APPROVED
    }
}
