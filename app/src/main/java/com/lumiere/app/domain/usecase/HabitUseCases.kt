package com.lumiere.app.domain.usecase

import com.lumiere.app.data.local.entity.*
import com.lumiere.app.data.repository.HabitRepository
import com.lumiere.app.core.time.TimeProvider
import com.lumiere.app.core.time.TimeUtils
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

private fun todayBounds(timeProvider: TimeProvider): Pair<Long, Long> {
    val now = timeProvider.currentTimeMillis()
    val start = TimeUtils.startOfDay(now, timeProvider.timeZone())
    return start to (start + 86_400_000L - 1)
}

class AddCheckInValueUseCase(private val repository: HabitRepository, private val timeProvider: TimeProvider) {
    suspend operator fun invoke(habitId: Long, currentValue: Int, targetValue: Int?) {
        val (startOfDay, endOfDay) = todayBounds(timeProvider)
        val existing = repository.getCheckInForDay(habitId, startOfDay, endOfDay)

        val newValue = if (targetValue != null) currentValue + 1 else 1
        val newType = when {
            targetValue == null -> "SUCCESS"
            newValue >= targetValue -> "SUCCESS"
            newValue > 0 -> "PARTIAL"
            else -> "FAIL"
        }
        val now = timeProvider.currentTimeMillis()

        if (existing != null) {
            repository.updateCheckIn(existing.copy(value = newValue, type = newType, timestamp = now))
        } else {
            repository.insertCheckIn(HabitCheckIn(habitId = habitId, value = newValue, type = newType, timestamp = now))
        }
    }
}

class AddFailureCheckInUseCase(private val repository: HabitRepository, private val timeProvider: TimeProvider) {
    suspend operator fun invoke(habitId: Long) {
        val (startOfDay, endOfDay) = todayBounds(timeProvider)
        val existing = repository.getCheckInForDay(habitId, startOfDay, endOfDay)
        val now = timeProvider.currentTimeMillis()
        if (existing != null) {
            repository.updateCheckIn(existing.copy(value = 0, type = "FAIL", timestamp = now))
        } else {
            repository.insertCheckIn(HabitCheckIn(habitId = habitId, value = 0, type = "FAIL", timestamp = now))
        }
    }
}

class GetTodayValueForHabitUseCase(private val timeProvider: TimeProvider) {
    operator fun invoke(habitId: Long, allCheckIns: List<HabitCheckIn>): Int {
        val (start, end) = todayBounds(timeProvider)
        return allCheckIns
            .filter { it.habitId == habitId && it.timestamp in start..end }
            .maxByOrNull { it.timestamp }?.value ?: 0
    }
}

// P0: Em vez de apagar, apenas arquiva (status = 2)
class ArchiveHabitUseCase(private val repository: HabitRepository) {
    suspend operator fun invoke(habit: Habit) {
        repository.updateHabit(habit.copy(status = 2))
    }
}

class StartHabitTimerUseCase(private val repository: HabitRepository, private val timeProvider: TimeProvider) {
    suspend operator fun invoke(habit: Habit) {
        if (habit.timerStartedAt != null) return
        repository.updateHabit(habit.copy(timerStartedAt = timeProvider.currentTimeMillis()))
    }
}

class PauseHabitTimerUseCase(private val repository: HabitRepository, private val timeProvider: TimeProvider) {
    suspend operator fun invoke(habit: Habit) {
        val started = habit.timerStartedAt ?: return
        val elapsedSec = ((timeProvider.currentTimeMillis() - started) / 1000L).toInt().coerceAtLeast(0)
        repository.updateHabit(habit.copy(
            timerStartedAt = null,
            timerAccumulatedSeconds = habit.timerAccumulatedSeconds + elapsedSec
        ))
    }
}

class CompleteHabitTimerUseCase(private val repository: HabitRepository, private val timeProvider: TimeProvider) {
    suspend operator fun invoke(habit: Habit) {
        val started = habit.timerStartedAt
        val now = timeProvider.currentTimeMillis()
        val elapsedSec = if (started != null) ((now - started) / 1000L).toInt().coerceAtLeast(0) else 0
        val totalSec = habit.timerAccumulatedSeconds + elapsedSec
        val totalMin = totalSec / 60

        if (totalSec == 0) {
            repository.updateHabit(habit.copy(timerStartedAt = null, timerAccumulatedSeconds = 0))
            return
        }

        val (startOfDay, endOfDay) = todayBounds(timeProvider)
        val existing = repository.getCheckInForDay(habit.id, startOfDay, endOfDay)

        val target = habit.durationMinutes ?: 0
        val type = if (target == 0 || totalSec >= target * 60) "SUCCESS" else "PARTIAL"

        if (existing != null) {
            repository.updateCheckIn(existing.copy(durationMinutes = totalMin, type = type, timestamp = now))
        } else {
            repository.insertCheckIn(HabitCheckIn(habitId = habit.id, durationMinutes = totalMin, type = type, timestamp = now))
        }
        repository.updateHabit(habit.copy(timerStartedAt = null, timerAccumulatedSeconds = 0))
    }
}

class GetWeeklyProgressUseCase(private val timeProvider: TimeProvider) {
    operator fun invoke(habit: Habit, allCheckIns: List<HabitCheckIn>): Pair<Int, Int> {
        val target = habit.weeklyDays.countOneBits()
        val now = timeProvider.currentTimeMillis()
        val cal = Calendar.getInstance(timeProvider.timeZone()).apply { timeInMillis = now }
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        val weekStart = TimeUtils.startOfDay(cal.timeInMillis, timeProvider.timeZone())
        val weekEnd = weekStart + 7 * 86_400_000L - 1
        
        val done = allCheckIns.count { it.habitId == habit.id && it.timestamp in weekStart..weekEnd && it.type == "SUCCESS" }
        return done to target
    }
}

class GetIntervalStatusUseCase(private val timeProvider: TimeProvider) {
    operator fun invoke(habit: Habit, allCheckIns: List<HabitCheckIn>): Pair<Int, Int> {
        val last = allCheckIns.filter { it.habitId == habit.id && it.type == "SUCCESS" }.maxByOrNull { it.timestamp } ?: return 0 to habit.intervalDays
        val daysSince = ((timeProvider.currentTimeMillis() - last.timestamp) / 86_400_000L).toInt()
        return daysSince to habit.intervalDays
    }
}

class PauseHabitUseCase(private val repository: HabitRepository, private val timeProvider: TimeProvider) {
    suspend operator fun invoke(habit: Habit) {
        val now = timeProvider.currentTimeMillis()
        repository.updateHabit(habit.copy(status = 1))
        repository.insertException(HabitException(habitId = habit.id, startDate = now, endDate = Long.MAX_VALUE, type = "PAUSE", reason = "Pausa manual"))
    }
}

class ResumeHabitUseCase(private val repository: HabitRepository, private val timeProvider: TimeProvider) {
    suspend operator fun invoke(habit: Habit) {
        val now = timeProvider.currentTimeMillis()
        repository.updateHabit(habit.copy(status = 0))
        // P2: Fechar a exceção ativa
        val exceptions = repository.getExceptions(habit.id).firstOrNull() ?: emptyList()
        val activeException = exceptions.find { it.type == "PAUSE" && it.endDate == Long.MAX_VALUE }
        if (activeException != null) {
            repository.insertException(activeException.copy(endDate = now))
        }
    }
}

class CalculateHabitStreakUseCase(private val timeProvider: TimeProvider) {
    operator fun invoke(habit: Habit, allCheckIns: List<HabitCheckIn>): Int {
        val checkIns = allCheckIns.filter { it.habitId == habit.id && it.type == "SUCCESS" }.sortedByDescending { it.timestamp }
        if (checkIns.isEmpty()) return 0

        var streak = 0
        val tz = timeProvider.timeZone()
        var currentDay = TimeUtils.dayKey(timeProvider.currentTimeMillis(), tz)
        var foundAny = false

        for (i in 0..365) {
            val dayOfWeek = Calendar.getInstance(tz).apply { timeInMillis = TimeUtils.startOfDay(timeProvider.currentTimeMillis(), tz) + i * 86_400_000L }.get(Calendar.DAY_OF_WEEK)
            val dayBit = when (dayOfWeek) {
                Calendar.MONDAY -> 1; Calendar.TUESDAY -> 2; Calendar.WEDNESDAY -> 4; Calendar.THURSDAY -> 8
                Calendar.FRIDAY -> 16; Calendar.SATURDAY -> 32; Calendar.SUNDAY -> 64; else -> 0
            }
            val isRestDay = (habit.restDays and dayBit) != 0
            val isExpectedDay = (habit.weeklyDays and dayBit) != 0

            val hasCheckIn = checkIns.any { TimeUtils.dayKey(it.timestamp, tz) == currentDay }

            if (isExpectedDay && !isRestDay) {
                if (hasCheckIn) { streak++; foundAny = true }
                else if (i == 0) { /* hoje pode ser feito */ }
                else break
            }
            currentDay--
        }
        return if (foundAny) streak else 0
    }
}
