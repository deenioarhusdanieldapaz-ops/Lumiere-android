package com.lumiere.app.core.memory

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "core_memory")
data class CoreMemory(
    @PrimaryKey val key: String,
    val value: String,
    val type: String, // PATTERN, PREFERENCE, OPERATIONAL, SESSION
    val sensitivity: String, // NORMAL, SENSITIVE, CRITICAL
    val createdAt: Long,
    val updatedAt: Long,
    val expiresAt: Long? // null = nunca expira (ex: PREFERENCE_DECLARED)
)
