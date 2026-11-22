package com.lumiere.app.core.contract

import com.lumiere.app.core.model.Sensitivity
import com.lumiere.app.core.time.TimeProvider
import com.lumiere.app.data.local.dao.HabitCheckInDao
import com.lumiere.app.data.local.dao.HabitDao
import kotlinx.coroutines.flow.first

class RoomHabitContract(
    private val habitDao: HabitDao,
    private val checkInDao: HabitCheckInDao,
    private val timeProvider: TimeProvider
) : HabitContract {
    override val contractVersion = "1.0.0"

    override suspend fun getActiveHabits(): Result<List<HabitSnapshot>> = runCatching {
        val now = timeProvider.currentTimeMillis()
        habitDao.getAllActiveHabits().first().map { it.toSnapshot(now) }
    }

    override suspend fun getHabit(id: String): Result<HabitSnapshot?> = runCatching {
        val habitId = id.toLongOrNull() ?: return@runCatching null
        val habit = habitDao.getHabitById(habitId) ?: return@runCatching null
        habit.toSnapshot(timeProvider.currentTimeMillis())
    }

    private suspend fun com.lumiere.app.data.local.entity.Habit.toSnapshot(now: Long): HabitSnapshot {
        val dayMs = 24L * 60 * 60 * 1000
        val checkIns7d = checkInDao.countSuccessSince(id, now - 7 * dayMs)
        val checkIns30d = checkInDao.countSuccessSince(id, now - 30 * dayMs)
        val freq = when (frequencyType.uppercase()) {
            "WEEKLY" -> HabitFrequency.WEEKLY
            "MONTHLY" -> HabitFrequency.MONTHLY
            else -> HabitFrequency.DAILY
        }
        val expected30d = when (freq) {
            HabitFrequency.DAILY -> 30
            HabitFrequency.WEEKLY -> 4
            HabitFrequency.MONTHLY -> 1
        }
        val rate = (checkIns30d.toFloat() / expected30d).coerceIn(0f, 1f)
        val timestamps = checkInDao.successTimestampsForHabit(id)
        val lastActivity = timestamps.firstOrNull() ?: createdAt
        return HabitSnapshot(
            id = id.toString(),
            nameShort = name.take(60),
            frequency = freq,
            targetPerPeriod = targetValue ?: 1,
            checkInsLast7d = checkIns7d,
            streakCurrent = computeStreak(timestamps, now),
            successRate30d = rate,
            lastUpdatedMillis = lastActivity,
            sensitivity = Sensitivity.NORMAL,
            preferredTime = preferredTime, durationMinutes = durationMinutes,
            isAdaptationPeriod = isAdaptationPeriod, unit = unit
        )
    }

    private fun computeStreak(timestampsDesc: List<Long>, now: Long): Int {
        if (timestampsDesc.isEmpty()) return 0
        val dayMs = 24L * 60 * 60 * 1000
        val today = now / dayMs
        val days = timestampsDesc.map { it / dayMs }.distinct().sortedDescending()
        if (days.isEmpty() || days.first() < today - 1) return 0
        var streak = 1
        var expected = days.first() - 1
        for (i in 1 until days.size) {
            if (days[i] == expected) { streak++; expected-- } else break
        }
        return streak
    }
}
