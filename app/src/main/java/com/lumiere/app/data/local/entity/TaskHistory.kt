package com.lumiere.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "task_history",
    foreignKeys = [
        ForeignKey(
            entity = Task::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE, deferred = false
        )
    ],
    indices = [Index(value = ["taskId"])]
)
data class TaskHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val action: String,
    val timestamp: Long = System.currentTimeMillis()
)
