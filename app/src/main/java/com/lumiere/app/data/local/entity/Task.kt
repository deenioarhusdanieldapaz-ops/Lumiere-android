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
    
    // Planeamento Temporal
    val startDate: Long? = null,
    val dueDate: Long? = null,
    val estimatedDurationMinutes: Int? = null, // Duração estimada
    val actualDurationMinutes: Int = 0,        // Cronómetro (tempo real)
    val recurrenceRule: String? = null,        // Ex: "DAILY", "WEEKLY", "MONTHLY"
    
    // Estado e Organização
    val status: Int = 0, // 0=Pendente, 1=Em Progresso, 2=Concluída, 3=Cancelada, 4=Arquivada
    val priority: Int = 1, // 1=Baixa, 2=Média, 3=Alta, 4=Urgente
    val category: String? = null, // Ex: "Trabalho", "Pessoal"
    val tags: String? = null,     // Etiquetas separadas por vírgula
    
    // Relações e Dependências
    val projectId: Long? = null,      // Liga ao Projeto
    val blockedByTaskId: Long? = null, // Se estiver bloqueada por outra tarefa
    val assignee: String? = null,     // Responsável
    
    // Sistema
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    // Propriedade auxiliar para manter compatibilidade com o UI atual
    val isCompleted: Boolean
        get() = status == 2
}
