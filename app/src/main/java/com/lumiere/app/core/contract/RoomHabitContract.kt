package com.lumiere.app.core.contract

import com.lumiere.app.core.model.Sensitivity
import com.lumiere.app.data.local.dao.HabitDao
import kotlinx.coroutines.flow.first

class RoomHabitContract(private val habitDao: HabitDao) : HabitContract {
    override val contractVersion = "1.0.0"

    override suspend fun getActiveHabits(): Result<List<HabitSnapshot>> = runCatching {
        habitDao.getAllActiveHabits().first().map { it.toSnapshot() }
    }

    override suspend fun getHabit(id: String): Result<HabitSnapshot?> = runCatching {
        habitDao.getHabitById(id.toLongOrNull() ?: return@runCatching null)?.toSnapshot()
    }

    private fun com.lumiere.app.data.local.entity.Habit.toSnapshot() = HabitSnapshot(
        id = id.toString(),
        nameShort = name.take(60),
        frequency = HabitFrequency.DAILY,
        targetPerPeriod = targetValue ?: 1,
        checkInsLast7d = 0,
        streakCurrent = 0,
        successRate30d = 0f,
        lastUpdatedMillis = createdAt,
        sensitivity = Sensitivity.NORMAL
    )
}
