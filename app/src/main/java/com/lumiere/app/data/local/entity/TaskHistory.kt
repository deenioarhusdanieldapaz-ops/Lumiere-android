package com.lumiere.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_history")
data class TaskHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long,
    val action: String, // Ex: "Criada", "Concluída", "Cancelada", "Editada"
    val timestamp: Long = System.currentTimeMillis()
)
