package com.lumiere.app.core.model

interface Schedulable {
    val id: String
    val moduleId: String
    val title: String
    val startTimeMillis: Long?
    val deadlineMillis: Long? // <-- NOVO: A hora em que termina/o prazo
    val durationMinutes: Int?
    val energyCost: Int
    val priority: Int
}
