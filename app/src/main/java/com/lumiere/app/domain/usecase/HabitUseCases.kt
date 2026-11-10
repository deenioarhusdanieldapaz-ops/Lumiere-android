package com.lumiere.app.domain.usecase

import com.lumiere.app.data.local.entity.*
import com.lumiere.app.data.repository.HabitRepository
import java.util.Calendar

enum class HabitKind {
    BOOLEAN, QUANTITATIVE, DURATION, WEEKLY_FREQUENCY, INTERVAL
}

fun Habit.kind(): HabitKind = when {
    durationMinutes != null && durationMinutes > 0 -> HabitKind.DURATION
    targetValue != null && targetValue > 0 -> HabitKind.QUANTITATIVE
    frequencyType == "CUSTOM" && intervalDays > 1 -> HabitKind.INTERVAL
    frequencyType == "WEEKLY" && weeklyDays.countOneBits() > 1 -> HabitKind.WEEKLY_FREQUENCY
    else -> HabitKind.BOOLEAN
}

private fun todayBounds(): Pair<Long, Long> {
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    val start = cal.timeInMillis
    return start to (start + 86_400_000L - 1)
}

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
            repository.updateCheckIn(existing.copy(
                value = newValue,
                type = newType,
                timestamp = System.currentTimeMillis()
            ))
        } else {
            repository.insertCheckIn(
                HabitCheckIn(habitId = habitId, value = newValue, type = newType)
            )
        }
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
}

class GetTodayValueForHabitUseCase {
    operator fun invoke(habitId: Long, allCheckIns: List<HabitCheckIn>): Int {
        val (start, end) = todayBounds()
        return allCheckIns
            .filter { it.habitId == habitId && it.timestamp in start..end }
            .maxByOrNull { it.timestamp }?.value ?: 0
    }
}

class DeleteHabitUseCase(private val repository: HabitRepository) {
    suspend operator fun invoke(habit: Habit) {
        repository.deleteCheckInsForHabit(habit.id)
        repository.deleteHabit(habit)
    }
}

class StartHabitTimerUseCase(private val repository: HabitRepository) {
    suspend operator fun invoke(habit: Habit) {
        if (habit.timerStartedAt != null) return
        repository.updateHabit(habit.copy(timerStartedAt = System.currentTimeMillis()))
    }
}

class PauseHabitTimerUseCase(private val repository: HabitRepository) {
    suspend operator fun invoke(habit: Habit) {
        val started = habit.timerStartedAt ?: return
        val elapsedSec = ((System.currentTimeMillis() - started) / 1000L).toInt().coerceAtLeast(0)
        repository.updateHabit(habit.copy(
            timerStartedAt = null,
            timerAccumulatedSeconds = habit.timerAccumulatedSeconds + elapsedSec
        ))
    }
}

class CompleteHabitTimerUseCase(private val repository: HabitRepository) {
    suspend operator fun invoke(habit: Habit) {
        val started = habit.timerStartedAt
        val elapsedSec = if (started != null) {
            ((System.currentTimeMillis() - started) / 1000L).toInt().coerceAtLeast(0)
        } else 0
        val totalSec = habit.timerAccumulatedSeconds + elapsedSec
        val totalMin = totalSec / 60

        // Se não correu tempo suficiente, só reseta (não cria FAIL)
        if (totalSec == 0) {
            repository.updateHabit(habit.copy(timerStartedAt = null, timerAccumulatedSeconds = 0))
            return
        }

        val (startOfDay, endOfDay) = todayBounds()
        val existing = repository.getCheckInForDay(habit.id, startOfDay, endOfDay)

        val target = habit.durationMinutes ?: 0
        val type = when {
            target == 0 -> "SUCCESS"
            totalSec >= target * 60 -> "SUCCESS"
            else -> "PARTIAL"
        }

        if (existing != null) {
            repository.updateCheckIn(existing.copy(
                durationMinutes = totalMin,
                type = type,
                timestamp = System.currentTimeMillis()
            ))
        } else {
            repository.insertCheckIn(HabitCheckIn(
                habitId = habit.id,
                durationMinutes = totalMin,
                type = type
            ))
        }

        repository.updateHabit(habit.copy(timerStartedAt = null, timerAccumulatedSeconds = 0))
    }
}

class GetWeeklyProgressUseCase {
    operator fun invoke(habit: Habit, allCheckIns: List<HabitCheckIn>): Pair<Int, Int> {
        val target = habit.weeklyDays.countOneBits()
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val weekStart = cal.timeInMillis
        val weekEnd = weekStart + 7 * 86_400_000L - 1
        val done = allCheckIns
            .filter { it.habitId == habit.id && it.timestamp in weekStart..weekEnd && it.type == "SUCCESS" }
            .size
        return done to target
    }
}

class GetIntervalStatusUseCase {
    operator fun invoke(habit: Habit, allCheckIns: List<HabitCheckIn>): Pair<Int, Int> {
        val last = allCheckIns
            .filter { it.habitId == habit.id && it.type == "SUCCESS" }
            .maxByOrNull { it.timestamp } ?: return 0 to habit.intervalDays
        val daysSince = ((System.currentTimeMillis() - last.timestamp) / 86_400_000L).toInt()
        return daysSince to habit.intervalDays
    }
}
