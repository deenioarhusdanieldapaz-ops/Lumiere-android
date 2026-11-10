package com.lumiere.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.SET_NULL, deferred = false
        ),
        ForeignKey(
            entity = Task::class,
            parentColumns = ["id"],
            childColumns = ["blockedByTaskId"],
            onDelete = ForeignKey.SET_NULL, deferred = false
        )
    ],
    indices = [
        Index(value = ["projectId"]),
        Index(value = ["blockedByTaskId"])
    ]
)
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String,
    val description: String? = null,

    val startDate: Long? = null,
    val dueDate: Long? = null,
    val estimatedDurationMinutes: Int? = null,
    val timeReservedMinutes: Int? = null,
    val actualDurationMinutes: Int = 0,
    val timerStartedAt: Long? = null,
    val urgency: Int = 1,
    val importance: Int = 1,
    val energy: Int = 1,
    val context: String? = null,
    val recurrenceRule: String? = null,

    val instructions: String? = null,
    val links: String? = null,
    val completionCriteria: String? = null,
    val cancellationReason: String? = null,
    val attachmentUris: String? = null,

    val status: Int = 0,
    val priority: Int = 1,
    val category: String? = null,
    val tags: String? = null,

    val projectId: Long? = null,
    val blockedByTaskId: Long? = null,
    val assignee: String? = null,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
) {
    val isCompleted: Boolean
        get() = status == 2
}
