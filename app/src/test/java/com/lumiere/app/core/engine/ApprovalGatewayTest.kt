package com.lumiere.app.core.engine

import com.lumiere.app.core.time.TimeProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApprovalGatewayTest {
    private val fixedNow = 1_700_000_000_000L
    private val timeProvider = object : TimeProvider {
        override fun currentTimeMillis(): Long = fixedNow
    }

    private fun request(id: String, expiresAt: Long) = ApprovalRequest(
        id = id, suggestionId = "s1",
        approvalClass = com.lumiere.app.core.model.ApprovalClass.A3_RELEVANT,
        whatChanges = emptyList(), alternatives = emptyList(),
        scope = ApprovalScope(emptySet(), null, false),
        reversibility = com.lumiere.app.core.model.Reversibility.REVERSIBLE,
        expiresAtMillis = expiresAt, whatItDoesNotAuthorize = emptyList()
    )

    @Test
    fun `isApproved true apos APPROVED dentro do prazo`() = runBlocking {
        val store = InMemoryApprovalStore()
        val gw = ApprovalGateway(store, timeProvider)
        val req = request("r1", fixedNow + 60_000L)
        store.save(req)
        store.saveDecision(ApprovalDecision("r1", ApprovalDecisionKind.APPROVED, "user", fixedNow))

        assertTrue(gw.isApproved("r1").getOrThrow())
    }

    @Test
    fun `isApproved false apos expiracao`() = runBlocking {
        val store = InMemoryApprovalStore()
        val gw = ApprovalGateway(store, timeProvider)
        val req = request("r1", fixedNow - 1L) // expirado
        store.save(req)
        store.saveDecision(ApprovalDecision("r1", ApprovalDecisionKind.APPROVED, "user", fixedNow - 100L))

        assertFalse(gw.isApproved("r1").getOrThrow())
    }

    @Test
    fun `isApproved false sem decisao`() = runBlocking {
        val store = InMemoryApprovalStore()
        val gw = ApprovalGateway(store, timeProvider)
        store.save(request("r1", fixedNow + 60_000L))

        assertFalse(gw.isApproved("r1").getOrThrow())
    }

    @Test
    fun `getAllDecisions devolve historico completo`() = runBlocking {
        val store = InMemoryApprovalStore()
        val gw = ApprovalGateway(store, timeProvider)
        store.saveDecision(ApprovalDecision("a", ApprovalDecisionKind.APPROVED, "user", fixedNow))
        store.saveDecision(ApprovalDecision("b", ApprovalDecisionKind.REJECTED, "user", fixedNow))
        store.saveDecision(ApprovalDecision("c", ApprovalDecisionKind.PARTIAL, "user", fixedNow))

        val all = gw.getAllDecisions().getOrThrow()
        assertEquals(3, all.size)
    }
}
