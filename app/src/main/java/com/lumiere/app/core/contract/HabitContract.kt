package com.lumiere.app.core.contract
import com.lumiere.app.core.model.Sensitivity

interface HabitContract {
    val contractVersion: String
    suspend fun getActiveHabits(): Result<List<HabitSnapshot>>
    suspend fun getHabit(id: String): Result<HabitSnapshot?>
}

data class HabitSnapshot(
    val id: String, val nameShort: String, val frequency: HabitFrequency,
    val targetPerPeriod: Int, val checkInsLast7d: Int, val streakCurrent: Int,
    val successRate30d: Float, val lastUpdatedMillis: Long,
    val sensitivity: Sensitivity = Sensitivity.NORMAL,
    val preferredTime: String? = null, val durationMinutes: Int? = null,
    val isAdaptationPeriod: Boolean = false, val unit: String? = null
)
enum class HabitFrequency { DAILY, WEEKLY, MONTHLY }
