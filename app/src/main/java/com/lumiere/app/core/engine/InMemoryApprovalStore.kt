package com.lumiere.app.core.engine

class InMemoryApprovalStore : ApprovalStore {
    private val requests = mutableMapOf<String, ApprovalRequest>()
    private val decisions = mutableMapOf<String, ApprovalDecision>()

    override suspend fun save(request: ApprovalRequest): Result<Unit> = runCatching {
        requests[request.id] = request
    }
    override suspend fun get(requestId: String): Result<ApprovalRequest?> =
        Result.success(requests[requestId])
    override suspend fun saveDecision(decision: ApprovalDecision): Result<Unit> = runCatching {
        decisions[decision.approvalId] = decision
    }
    override suspend fun getDecision(approvalId: String): Result<ApprovalDecision?> =
        Result.success(decisions[approvalId])
}
