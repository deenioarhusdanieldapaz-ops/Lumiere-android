package com.lumiere.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String? = null,
    val notes: String? = null,
    val category: String? = null,
    // Frequência: DAILY, WEEKLY, CUSTOM
    val frequencyType: String = "DAILY",
    // Meta quantitativa: valor alvo (ex: 8), unidade (ex: copos)
    val targetValue: Int? = null,
    val unit: String? = null,
    // Dias da semana para frequência semanal (bitmask: 1=Seg, 2=Ter, 4=Qua, etc.)
    val weeklyDays: Int = 127, // Todos os dias por defeito
    val intervalDays: Int = 1, // Para frequência "a cada X dias"
    val preferredTime: String? = null, // Horário preferencial (ex: "08:00")
    val durationMinutes: Int? = null, // Duração estimada
    val completionCriteria: String? = null, // Critério de conclusão
    val isAdaptationPeriod: Boolean = false, // Período de adaptação
    val restDays: Int = 0, // Bitmask dos dias de descanso
    val status: Int = 0, // 0=Ativo, 1=Pausado, 2=Arquivado
    val startDate: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
