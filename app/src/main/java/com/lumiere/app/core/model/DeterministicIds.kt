package com.lumiere.app.core.model

import java.security.MessageDigest

object DeterministicIds {
    fun forFact(statement: String, moduleId: String, recordId: String): String =
        hash("fact|$statement|$moduleId|$recordId")

    fun forInference(statement: String, method: String): String =
        hash("inference|$statement|$method")

    fun forSuggestion(statement: String, targetModules: Set<String>, actionKey: String): String =
        hash("suggestion|$statement|${targetModules.sorted().joinToString(",")}|$actionKey")

    fun actionKey(action: SuggestionAction?): String = when (action) {
        null -> "none"
        is SuggestionAction.DeferItem -> "defer:${action.itemId}:${action.days}"
        is SuggestionAction.BatchDeferItems -> "batch:${action.itemIds.sorted().joinToString(",")}:${action.days}"
        is SuggestionAction.RescheduleItem -> "resched:${action.itemId}:${action.newTimeMillis}"
        is SuggestionAction.ShortenDuration -> "shorten:${action.itemId}:${action.newDurationMinutes}"
        is SuggestionAction.ChangeFrequency -> "freq:${action.itemId}:${action.newFrequency}"
    }

    private fun hash(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }.take(16)
    }
}
