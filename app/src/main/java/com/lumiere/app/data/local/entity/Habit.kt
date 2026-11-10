package com.lumiere.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String? = null,
    val notes: String? = null,
    val category: String? = null,
    val frequencyType: String = "DAILY",
    val targetValue: Int? = null,
    val unit: String? = null,
    val weeklyDays: Int = 127,
    val intervalDays: Int = 1,
    val preferredTime: String? = null,
    val durationMinutes: Int? = null,
    val completionCriteria: String? = null,
    val isAdaptationPeriod: Boolean = false,
    val restDays: Int = 0,
    val status: Int = 0,
    val startDate: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    // Cronómetro persistido (migration 14->15)
    val timerStartedAt: Long? = null,
    val timerAccumulatedMinutes: Int = 0
)
