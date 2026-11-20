package com.lumiere.app.core.contracts

import com.lumiere.app.data.local.entity.Task
import com.lumiere.app.data.local.entity.Habit

data class CoreContext(
    val tasks: List<Task>,
    val habits: List<Habit>
)

data class Recommendation(
    val id: String,
    val title: String,
    val reasoning: String,
    val requiresApproval: Boolean
)

data class AnalysisResult(
    val risks: List<String>,
    val recommendations: List<Recommendation>
)
