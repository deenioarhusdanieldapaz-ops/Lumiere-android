package com.lumiere.app.core.engine

import com.lumiere.app.data.local.dao.HabitDao
import com.lumiere.app.data.local.entity.Habit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeHabitDao : HabitDao {
    override fun getAllActiveHabits(): Flow<List<Habit>> = flowOf(emptyList())
    override suspend fun getHabitById(id: Long): Habit? = null
    override suspend fun insertHabit(habit: Habit): Long = 0L
    override suspend fun updateHabit(habit: Habit) {}
    override suspend fun deleteHabit(habit: Habit) {}
}
