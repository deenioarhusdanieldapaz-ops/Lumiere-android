package com.lumiere.app.core.memory

import com.lumiere.app.core.model.Sensitivity
import com.lumiere.app.core.time.TimeProvider

enum class MemoryType { SESSION, PATTERN, PREFERENCE, OPERATIONAL }

class CoreMemoryRepository(
    private val dao: CoreMemoryDao,
    private val timeProvider: TimeProvider
) {

    suspend fun put(
        key: String,
        value: String,
        type: MemoryType,
        sensitivity: Sensitivity,
        ttlMillis: Long?
    ): Result<Unit> = runCatching {
        require(sensitivity != Sensitivity.CRITICAL) { "CRITICAL memory not persisted by policy" }
        val now = timeProvider.currentTimeMillis()
        dao.save(
            CoreMemory(
                key = key,
                value = value,
                type = type.name,
                sensitivity = sensitivity.name,
                createdAt = now,
                updatedAt = now,
                expiresAt = ttlMillis?.let { now + it }
            )
        )
    }

    suspend fun get(key: String): Result<CoreMemory?> = runCatching {
        val entry = dao.get(key) ?: return@runCatching null
        if (entry.expiresAt != null && entry.expiresAt < timeProvider.currentTimeMillis()) null
        else entry
    }

    suspend fun sweepExpired(): Result<Unit> = runCatching {
        dao.deleteExpired(timeProvider.currentTimeMillis())
    }
}
