package com.lumiere.app.core.contract

import com.lumiere.app.core.model.Schedulable
import com.lumiere.app.core.model.Sensitivity
import java.util.Calendar

interface HabitContract {
    val contractVersion: String
    suspend fun getActiveHabits(): Result<List<HabitSnapshot>>
    suspend fun getHabit(id: String): Result<HabitSnapshot?>
}

data class HabitSnapshot(
    override val id: String, 
    val nameShort: String, 
    val frequency: HabitFrequency,
    val targetPerPeriod: Int, 
    val checkInsLast7d: Int, 
    val streakCurrent: Int,
    val successRate30d: Float, 
    val lastUpdatedMillis: Long,
    val sensitivity: Sensitivity = Sensitivity.NORMAL,
    val preferredTime: String? = null, 
    override val durationMinutes: Int? = null,
    val isAdaptationPeriod: Boolean = false, 
    val unit: String? = null
) : Schedulable {
    override val moduleId: String get() = "M02_HABITS"
    override val title: String get() = nameShort
    override val startTimeMillis: Long? get() {
        if (preferredTime == null) return null
        val parts = preferredTime.split(":")
        if (parts.size != 2) return null
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, parts[0].toIntOrNull() ?: return null)
        cal.set(Calendar.MINUTE, parts[1].toIntOrNull() ?: return null)
        return cal.timeInMillis
    }
    override val deadlineMillis: Long? get() = startTimeMillis?.let { it + ((durationMinutes ?: 30) * 60_000L) }
    override val energyCost: Int get() = 2
    override val priority: Int get() = if (isAdaptationPeriod) 3 else 2
}
enum class HabitFrequency { DAILY, WEEKLY, MONTHLY }
