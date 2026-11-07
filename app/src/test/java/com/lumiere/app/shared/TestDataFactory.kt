package com.lumiere.app.shared

import com.lumiere.app.data.local.entity.Task

object TestDataFactory {

    fun task(
        id: Long = 0L,
        title: String = "Tarefa QA",
        status: Int = 0,
        recurrenceRule: String? = null,
        dueDate: Long? = null,
        timerStartedAt: Long? = null,
        actualDurationMinutes: Int = 0,
        cancellationReason: String? = null
    ) = Task(
        id = id,
        title = title,
        status = status,
        recurrenceRule = recurrenceRule,
        dueDate = dueDate,
        timerStartedAt = timerStartedAt,
        actualDurationMinutes = actualDurationMinutes,
        cancellationReason = cancellationReason
    )

    fun completedTask(
        id: Long = 0L,
        title: String = "Tarefa QA concluída",
        recurrenceRule: String? = null,
        dueDate: Long? = null
    ) = task(id = id, title = title, status = 2, recurrenceRule = recurrenceRule, dueDate = dueDate)
}
