package com.lumiere.app.core.model

sealed interface SuggestionAction {
    data class DeferItem(val itemId: String, val moduleId: String, val days: Int) : SuggestionAction
    data class RescheduleItem(val itemId: String, val moduleId: String, val newTimeMillis: Long) : SuggestionAction
    data class ShortenDuration(val itemId: String, val moduleId: String, val newDurationMinutes: Int) : SuggestionAction
    data class ChangeFrequency(val itemId: String, val moduleId: String, val newFrequency: String) : SuggestionAction
}
