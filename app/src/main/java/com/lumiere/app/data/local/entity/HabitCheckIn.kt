package com.lumiere.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit_checkins",
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE, deferred = false
        )
    ],
    indices = [Index(value = ["habitId", "timestamp"])]
)
data class HabitCheckIn(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val value: Int? = null,
    val durationMinutes: Int? = null,
    val observation: String? = null,
    val origin: String = "MANUAL",
    val type: String = "SUCCESS"
)
