package com.lumiere.app.core.engine

import com.lumiere.app.core.memory.CoreMemory
import com.lumiere.app.core.memory.CoreMemoryDao
import com.lumiere.app.core.model.ApprovalClass
import com.lumiere.app.core.model.Reversibility
import org.json.JSONArray
import org.json.JSONObject

class CoreMemoryApprovalStore(
    private val memoryDao: CoreMemoryDao,
    private val defaultTtlMillis: Long = 7L * 24 * 60 * 60 * 1000
) : ApprovalStore {

    override suspend fun save(request: ApprovalRequest): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        val expiresAt = minOf(request.expiresAtMillis, now + defaultTtlMillis)
        memoryDao.save(CoreMemory(
            key = KEY_REQ_PREFIX + request.id,
            value = requestToJson(request).toString(),
            type = "OPERATIONAL", sensitivity = "NORMAL",
            createdAt = now, updatedAt = now, expiresAt = expiresAt
        ))
        Unit
    }

    override suspend fun get(requestId: String): Result<ApprovalRequest?> = runCatching {
        val mem = memoryDao.get(KEY_REQ_PREFIX + requestId) ?: return@runCatching null
        if (isExpired(mem)) return@runCatching null
        jsonToRequest(JSONObject(mem.value))
    }

    override suspend fun saveDecision(decision: ApprovalDecision): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        memoryDao.save(CoreMemory(
            key = KEY_DEC_PREFIX + decision.approvalId,
            value = decisionToJson(decision).toString(),
            type = "OPERATIONAL", sensitivity = "NORMAL",
            createdAt = now, updatedAt = now, expiresAt = now + defaultTtlMillis
        ))
        Unit
    }

    override suspend fun getDecision(approvalId: String): Result<ApprovalDecision?> = runCatching {
        val mem = memoryDao.get(KEY_DEC_PREFIX + approvalId) ?: return@runCatching null
        if (isExpired(mem)) return@runCatching null
        jsonToDecision(JSONObject(mem.value))
    }

    override suspend fun getAllDecisions(): Result<List<ApprovalDecision>> = runCatching {
        memoryDao.getAllDecisions().mapNotNull { mem ->
            try { jsonToDecision(JSONObject(mem.value)) } catch (e: Exception) { null }
        }
    }

    private fun isExpired(mem: CoreMemory): Boolean {
        val exp = mem.expiresAt ?: return false
        return exp < System.currentTimeMillis()
    }

    private fun requestToJson(r: ApprovalRequest): JSONObject = JSONObject().apply {
        put("id", r.id)
        put("suggestionId", r.suggestionId)
        put("approvalClass", r.approvalClass.name)
        put("whatChanges", JSONArray(r.whatChanges))
        put("alternatives", JSONArray(r.alternatives))
        put("scopeModules", JSONArray(r.scope.modules.toList()))
        if (r.scope.durationMillis == null) put("scopeDurationMillis", JSONObject.NULL)
        else put("scopeDurationMillis", r.scope.durationMillis)
        put("scopeReusable", r.scope.reusable)
        put("reversibility", r.reversibility.name)
        put("expiresAtMillis", r.expiresAtMillis)
        put("whatItDoesNotAuthorize", JSONArray(r.whatItDoesNotAuthorize))
    }

    private fun jsonToRequest(o: JSONObject): ApprovalRequest = ApprovalRequest(
        id = o.getString("id"),
        suggestionId = o.getString("suggestionId"),
        approvalClass = ApprovalClass.valueOf(o.getString("approvalClass")),
        whatChanges = o.getJSONArray("whatChanges").toStringList(),
        alternatives = o.getJSONArray("alternatives").toStringList(),
        scope = ApprovalScope(
            modules = o.getJSONArray("scopeModules").toStringList().toSet(),
            durationMillis = if (o.isNull("scopeDurationMillis")) null else o.getLong("scopeDurationMillis"),
            reusable = o.getBoolean("scopeReusable")
        ),
        reversibility = Reversibility.valueOf(o.getString("reversibility")),
        expiresAtMillis = o.getLong("expiresAtMillis"),
        whatItDoesNotAuthorize = o.getJSONArray("whatItDoesNotAuthorize").toStringList()
    )

    private fun decisionToJson(d: ApprovalDecision): JSONObject = JSONObject().apply {
        put("approvalId", d.approvalId)
        put("decision", d.decision.name)
        put("decidedBy", d.decidedBy)
        put("decidedAtMillis", d.decidedAtMillis)
    }

    private fun jsonToDecision(o: JSONObject): ApprovalDecision = ApprovalDecision(
        approvalId = o.getString("approvalId"),
        decision = ApprovalDecisionKind.valueOf(o.getString("decision")),
        decidedBy = o.getString("decidedBy"),
        decidedAtMillis = o.getLong("decidedAtMillis")
    )

    private fun JSONArray.toStringList(): List<String> =
        (0 until length()).map { getString(it) }

    companion object {
        private const val KEY_REQ_PREFIX = "approval_req_"
        private const val KEY_DEC_PREFIX = "approval_dec_"
    }
}
