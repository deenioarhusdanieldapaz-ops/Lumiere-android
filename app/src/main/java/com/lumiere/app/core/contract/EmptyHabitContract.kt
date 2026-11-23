package com.lumiere.app.core.contract

class EmptyHabitContract : HabitContract {
    override val contractVersion: String = "0.0.0-stub"
    override suspend fun getActiveHabits(): Result<List<HabitSnapshot>> = Result.success(emptyList())
    override suspend fun getHabit(id: String): Result<HabitSnapshot?> = Result.success(null)
}
