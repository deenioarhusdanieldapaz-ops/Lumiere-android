package com.lumiere.app.core.model

/**
 * Contrato Universal para o motor de Inteligência.
 * Qualquer módulo (M01, M02, M03... M13) que queira que o cérebro do Lumière 
 * resolva conflitos TEM de implementar esta interface.
 */
interface Schedulable {
    val id: String
    val moduleId: String // Ex: "M01_TASKS", "M02_HABITS"
    val title: String
    val startTimeMillis: Long?
    val durationMinutes: Int?
    val energyCost: Int // 1 = Baixa, 2 = Média, 3 = Alta
    val priority: Int   // 1 = Baixa, 2 = Média, 3 = Alta
}

