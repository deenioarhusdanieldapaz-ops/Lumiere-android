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

/**
 * Ações que o Lumière pode executar para resolver um conflito.
 */
sealed interface SuggestionAction {
    // Adiar qualquer item (Tarefa, Hábito ou futuro Módulo)
    data class DeferItem(val itemId: String, val moduleId: String, val days: Int) : SuggestionAction
    
    // Mudar a hora de um item (Ex: Tarefa para amanhã, Hábito para outra hora)
    data class RescheduleItem(val itemId: String, val moduleId: String, val newTimeMillis: Long) : SuggestionAction
    
    // Encurtar a duração (Ex: Reduzir hábito de 1h para 30min para libertar tempo)
    data class ShortenDuration(val itemId: String, val moduleId: String, val newDurationMinutes: Int) : SuggestionAction
    
    // Mudar a frequência (Exclusivo de hábitos por agora)
    data class ChangeFrequency(val itemId: String, val moduleId: String, val newFrequency: String) : SuggestionAction
}
