package com.lumiere.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit_exceptions",
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE, deferred = false
        )
    ],
    indices = [Index(value = ["habitId"])]
)
data class HabitException(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val startDate: Long,
    val endDate: Long,
    val type: String,
    val reason: String? = null
)
