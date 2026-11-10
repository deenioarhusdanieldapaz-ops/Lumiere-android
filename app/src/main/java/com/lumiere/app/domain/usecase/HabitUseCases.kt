package com.lumiere.app.domain.usecase

import com.lumiere.app.data.local.entity.*
import com.lumiere.app.data.repository.HabitRepository
import java.util.Calendar

class AddCheckInValueUseCase(private val repository: HabitRepository) {
    suspend operator fun invoke(habitId: Long, currentValue: Int, targetValue: Int?) {
        val (startOfDay, endOfDay) = todayBounds()
        val existing = repository.getCheckInForDay(habitId, startOfDay, endOfDay)

        val newValue = if (targetValue != null) currentValue + 1 else 1
        val newType = when {
            targetValue == null -> "SUCCESS"
            newValue >= targetValue -> "SUCCESS"
            newValue > 0 -> "PARTIAL"
            else -> "FAIL"
        }

        if (existing != null) {
            repository.updateCheckIn(
                existing.copy(
                    value = newValue,
                    type = newType,
                    timestamp = System.currentTimeMillis()
                )
            )
        } else {
            repository.insertCheckIn(
                HabitCheckIn(habitId = habitId, value = newValue, type = newType)
            )
        }
    }

    private fun todayBounds(): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        return start to (start + 86_400_000L - 1)
    }
}

class AddFailureCheckInUseCase(private val repository: HabitRepository) {
    suspend operator fun invoke(habitId: Long) {
        val (startOfDay, endOfDay) = todayBounds()
        val existing = repository.getCheckInForDay(habitId, startOfDay, endOfDay)
        if (existing != null) {
            repository.updateCheckIn(existing.copy(value = 0, type = "FAIL", timestamp = System.currentTimeMillis()))
        } else {
            repository.insertCheckIn(HabitCheckIn(habitId = habitId, value = 0, type = "FAIL"))
        }
    }

    private fun todayBounds(): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        return start to (start + 86_400_000L - 1)
    }
}

class GetTodayValueForHabitUseCase {
    operator fun invoke(habitId: Long, allCheckIns: List<HabitCheckIn>): Int {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        val end = start + 86_400_000L - 1
        // Pega o ÚLTIMO check-in do dia (não soma)
        return allCheckIns
            .filter { it.habitId == habitId && it.timestamp in start..end }
            .maxByOrNull { it.timestamp }?.value ?: 0
    }
}

class DeleteHabitUseCase(private val repository: HabitRepository) {
    suspend operator fun invoke(habit: Habit) {
        // CASCADE já apaga check-ins e exceções, mas garantimos
        repository.deleteCheckInsForHabit(habit.id)
        repository.deleteHabit(habit)
    }
}
