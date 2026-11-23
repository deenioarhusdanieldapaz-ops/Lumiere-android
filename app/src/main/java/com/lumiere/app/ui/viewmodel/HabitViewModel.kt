package com.lumiere.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lumiere.app.data.local.entity.*
import com.lumiere.app.data.repository.HabitRepository
import com.lumiere.app.domain.usecase.*
import com.lumiere.app.core.time.TimeProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HabitViewModel(private val repository: HabitRepository, private val timeProvider: TimeProvider) : ViewModel() {

    val habits: StateFlow<List<Habit>> = repository.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allCheckIns: StateFlow<List<HabitCheckIn>> = repository.allCheckIns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val addCheckInValueUC = AddCheckInValueUseCase(repository, timeProvider)
    private val addFailureCheckInUC = AddFailureCheckInUseCase(repository, timeProvider)
    private val getTodayValueUC = GetTodayValueForHabitUseCase(timeProvider)
    private val archiveHabitUC = ArchiveHabitUseCase(repository)
    private val startTimerUC = StartHabitTimerUseCase(repository, timeProvider)
    private val pauseTimerUC = PauseHabitTimerUseCase(repository, timeProvider)
    private val completeTimerUC = CompleteHabitTimerUseCase(repository, timeProvider)
    private val getWeeklyProgressUC = GetWeeklyProgressUseCase(timeProvider)
    private val getIntervalStatusUC = GetIntervalStatusUseCase(timeProvider)
    private val pauseHabitUC = PauseHabitUseCase(repository, timeProvider)
    private val resumeHabitUC = ResumeHabitUseCase(repository, timeProvider)
    private val calculateStreakUC = CalculateHabitStreakUseCase(timeProvider)

    fun addHabit(habit: Habit) { viewModelScope.launch { repository.insertHabit(habit) } }
    fun archiveHabit(habit: Habit) { viewModelScope.launch { archiveHabitUC(habit) } }
    fun updateHabit(habit: Habit) { viewModelScope.launch { repository.updateHabit(habit) } }
    fun addCheckIn(checkIn: HabitCheckIn) { viewModelScope.launch { repository.insertCheckIn(checkIn) } }

    fun addCheckInValue(habitId: Long, currentValue: Int, targetValue: Int?) {
        viewModelScope.launch { addCheckInValueUC(habitId, currentValue, targetValue) }
    }
    fun addFailureCheckIn(habitId: Long) { viewModelScope.launch { addFailureCheckInUC(habitId) } }
    fun getTodayValueForHabit(habitId: Long, allCheckIns: List<HabitCheckIn>): Int = getTodayValueUC(habitId, allCheckIns)
    
    fun startTimer(habit: Habit) { viewModelScope.launch { startTimerUC(habit) } }
    fun pauseTimer(habit: Habit) { viewModelScope.launch { pauseTimerUC(habit) } }
    fun completeTimer(habit: Habit) { viewModelScope.launch { completeTimerUC(habit) } }

    fun getWeeklyProgress(habit: Habit, allCheckIns: List<HabitCheckIn>): Pair<Int, Int> = getWeeklyProgressUC(habit, allCheckIns)
    fun getIntervalStatus(habit: Habit, allCheckIns: List<HabitCheckIn>): Pair<Int, Int> = getIntervalStatusUC(habit, allCheckIns)

    fun pauseHabit(habit: Habit) { viewModelScope.launch { pauseHabitUC(habit) } }
    fun resumeHabit(habit: Habit) { viewModelScope.launch { resumeHabitUC(habit) } }
    fun deleteCheckIn(checkIn: HabitCheckIn) { viewModelScope.launch { repository.deleteCheckIn(checkIn) } }

    fun getCheckInsForHabit(habitId: Long): StateFlow<List<HabitCheckIn>> = repository.getCheckIns(habitId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getTodayCheckIns(): Map<Long, HabitCheckIn> = emptyMap()

    fun calculateStreak(habitId: Long): Int {
        val habit = habits.value.find { it.id == habitId } ?: return 0
        return calculateStreakUC(habit, allCheckIns.value)
    }

    fun calculateGlobalStreak(): Int = habits.value.maxOfOrNull { calculateStreak(it.id) } ?: 0

    fun calculateConsistency(): Int {
        if (habits.value.isEmpty()) return 0
        var totalProgress = 0.0
        val todayCheckIns = allCheckIns.value
        val todayKey = com.lumiere.app.core.time.TimeUtils.dayKey(timeProvider.currentTimeMillis(), timeProvider.timeZone())

        habits.value.forEach { habit ->
            val kind = habit.kind()
            val habitCheckInsToday = todayCheckIns.filter {
                it.habitId == habit.id && com.lumiere.app.core.time.TimeUtils.dayKey(it.timestamp, timeProvider.timeZone()) == todayKey
            }

            val progress = when (kind) {
                HabitKind.BOOLEAN -> if (habitCheckInsToday.any { it.type == "SUCCESS" }) 1.0 else 0.0
                HabitKind.QUANTITATIVE -> {
                    val current = habitCheckInsToday.maxByOrNull { it.timestamp }?.value ?: 0
                    (current.toDouble() / (habit.targetValue ?: 1)).coerceAtMost(1.0)
                }
                HabitKind.DURATION -> {
                    val currentMin = habitCheckInsToday.maxByOrNull { it.timestamp }?.durationMinutes ?: 0
                    (currentMin.toDouble() / (habit.durationMinutes ?: 1)).coerceAtMost(1.0)
                }
                else -> if (habitCheckInsToday.any { it.type == "SUCCESS" }) 1.0 else 0.0
            }
            totalProgress += progress
        }
        return ((totalProgress / habits.value.size) * 100).toInt()
    }
}

class HabitViewModelFactory(private val repository: HabitRepository, private val timeProvider: TimeProvider) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HabitViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HabitViewModel(repository, timeProvider) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
