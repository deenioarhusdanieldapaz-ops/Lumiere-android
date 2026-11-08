package com.lumiere.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habit_exceptions")
data class HabitException(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val startDate: Long,
    val endDate: Long,
    val type: String, // PAUSE, EXCEPTION, REST
    val reason: String? = null
)
