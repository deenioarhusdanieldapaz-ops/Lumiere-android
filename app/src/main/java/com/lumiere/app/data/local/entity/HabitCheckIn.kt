package com.lumiere.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habit_checkins")
data class HabitCheckIn(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val value: Int? = null, // Para metas quantitativas
    val durationMinutes: Int? = null, // Para hábitos baseados em tempo
    val observation: String? = null,
    val origin: String = "MANUAL", // MANUAL, INTEGRATION
    val type: String = "SUCCESS" // SUCCESS, FAIL, EXCEPTION
)
