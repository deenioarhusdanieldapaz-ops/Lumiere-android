package com.lumiere.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subtasks")
data class Subtask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long, // Liga à Tarefa Mãe
    val title: String,
    val isCompleted: Boolean = false
)
