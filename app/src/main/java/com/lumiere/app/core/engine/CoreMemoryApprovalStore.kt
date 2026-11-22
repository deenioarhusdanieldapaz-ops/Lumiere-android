package com.lumiere.app.core.engine

import com.lumiere.app.core.memory.CoreMemory
import com.lumiere.app.core.memory.CoreMemoryDao

class CoreMemoryApprovalStore(private val memoryDao: CoreMemoryDao) : ApprovalStore {
    override suspend fun saveApproval(id: String, isApproved: Boolean) {
        val memory = CoreMemory(
            key = "approval_$id",
            value = isApproved.toString(),
            type = "OPERATIONAL",
            sensitivity = "NORMAL",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + (24 * 60 * 60 * 1000) // Expira em 24h
        )
        memoryDao.save(memory)
    }

    override suspend fun isApproved(id: String): Boolean {
        val mem = memoryDao.get("approval_$id")
        return mem?.value?.toBoolean() ?: false
    }
}
