package com.lumiere.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Conteúdo base
    val title: String,
    val description: String? = null,
    
    // Ponto 1: Planeamento e Execução
    val startDate: Long? = null,
    val dueDate: Long? = null,
    val estimatedDurationMinutes: Int? = null, // Estimativa
    val timeReservedMinutes: Int? = null,      // Tempo reservado
    val actualDurationMinutes: Int = 0,        // Tempo real (Cronómetro)
    val timerStartedAt: Long? = null,          // Timestamp de quando o cronómetro começou
    val urgency: Int = 1,                      // 1=Baixa, 2=Média, 3=Alta
    val importance: Int = 1,                   // 1=Baixa, 2=Média, 3=Alta
    val energy: Int = 1,                       // 1=Baixa, 2=Média, 3=Alta
    val context: String? = null,               // Ex: "Em casa", "No escritório"
    val recurrenceRule: String? = null,        // DAILY, WEEKLY...
    
    // Ponto 2: Anexos e Contexto
    val instructions: String? = null,          // Instruções detalhadas
    val links: String? = null,                 // Links ou referências
    val completionCriteria: String? = null,    // Critério para dar como concluída
    val cancellationReason: String? = null,    // Motivo do cancelamento
    val attachmentUris: String? = null,        // URIs dos ficheiros (separados por vírgula)
    
    // Estado e Organização
    val status: Int = 0, // 0=Pendente, 1=Em Progresso, 2=Concluída, 3=Cancelada, 4=Arquivada
    val priority: Int = 1,
    val category: String? = null,
    val tags: String? = null,
    
    // Relações
    val projectId: Long? = null,
    val blockedByTaskId: Long? = null,
    val assignee: String? = null,
    
    // Sistema
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isCompleted: Boolean
        get() = status == 2
}
