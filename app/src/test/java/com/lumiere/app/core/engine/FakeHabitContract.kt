package com.lumiere.app.core.engine

import com.lumiere.app.core.contract.HabitContract
import com.lumiere.app.core.contract.HabitSnapshot

class FakeHabitContract(private val habits: List<HabitSnapshot>) : HabitContract {
    override val contractVersion: String = "fake-1.0.0"
    override suspend fun getActiveHabits(): Result<List<HabitSnapshot>> =
        Result.success(habits)
    override suspend fun getHabit(id: String): Result<HabitSnapshot?> =
        Result.success(habits.find { it.id == id })
}
