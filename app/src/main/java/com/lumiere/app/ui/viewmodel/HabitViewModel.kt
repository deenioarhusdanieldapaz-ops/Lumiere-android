package com.lumiere.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lumiere.app.data.local.entity.*
import com.lumiere.app.data.repository.HabitRepository
import com.lumiere.app.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class HabitViewModel(private val repository: HabitRepository) : ViewModel() {

    val habits: StateFlow<List<Habit>> = repository.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allCheckIns: StateFlow<List<HabitCheckIn>> = repository.allCheckIns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val addCheckInValueUC = AddCheckInValueUseCase(repository)
    private val addFailureCheckInUC = AddFailureCheckInUseCase(repository)
    private val getTodayValueUC = GetTodayValueForHabitUseCase()
    private val deleteHabitUC = DeleteHabitUseCase(repository)
    private val startTimerUC = StartHabitTimerUseCase(repository)
    private val pauseTimerUC = PauseHabitTimerUseCase(repository)
    private val completeTimerUC = CompleteHabitTimerUseCase(repository)
    private val getWeeklyProgressUC = GetWeeklyProgressUseCase()
    private val getIntervalStatusUC = GetIntervalStatusUseCase()

    fun addHabit(habit: Habit) { viewModelScope.launch { repository.insertHabit(habit) } }
    fun deleteHabit(habit: Habit) { viewModelScope.launch { deleteHabitUC(habit) } }
    fun updateHabit(habit: Habit) { viewModelScope.launch { repository.updateHabit(habit) } }
    fun addCheckIn(checkIn: HabitCheckIn) { viewModelScope.launch { repository.insertCheckIn(checkIn) } }

    fun addCheckInValue(habitId: Long, currentValue: Int, targetValue: Int?) {
        viewModelScope.launch { addCheckInValueUC(habitId, currentValue, targetValue) }
    }

    fun addFailureCheckIn(habitId: Long) {
        viewModelScope.launch { addFailureCheckInUC(habitId) }
    }

    fun getTodayValueForHabit(habitId: Long, allCheckIns: List<HabitCheckIn>): Int {
        return getTodayValueUC(habitId, allCheckIns)
    }

    fun startTimer(habit: Habit) { viewModelScope.launch { startTimerUC(habit) } }
    fun pauseTimer(habit: Habit) { viewModelScope.launch { pauseTimerUC(habit) } }
    fun completeTimer(habit: Habit) { viewModelScope.launch { completeTimerUC(habit) } }

    fun getWeeklyProgress(habit: Habit, allCheckIns: List<HabitCheckIn>): Pair<Int, Int> =
        getWeeklyProgressUC(habit, allCheckIns)

    fun getIntervalStatus(habit: Habit, allCheckIns: List<HabitCheckIn>): Pair<Int, Int> =
        getIntervalStatusUC(habit, allCheckIns)

    fun pauseHabit(habit: Habit) {
        viewModelScope.launch {
            repository.updateHabit(habit.copy(status = 1))
            repository.insertException(
                HabitException(
                    habitId = habit.id,
                    startDate = System.currentTimeMillis(),
                    endDate = Long.MAX_VALUE,
                    type = "PAUSE",
                    reason = "Pausa manual"
                )
            )
        }
    }

    fun resumeHabit(habit: Habit) {
        viewModelScope.launch { repository.updateHabit(habit.copy(status = 0)) }
    }

    fun deleteCheckIn(checkIn: HabitCheckIn) {
        viewModelScope.launch { repository.deleteCheckIn(checkIn) }
    }

    fun getCheckInsForHabit(habitId: Long): StateFlow<List<HabitCheckIn>> = repository.getCheckIns(habitId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getTodayCheckIns(): Map<Long, HabitCheckIn> = emptyMap()

    fun calculateStreak(habitId: Long): Int {
        val habit = habits.value.find { it.id == habitId } ?: return 0
        val checkIns = allCheckIns.value.filter { it.habitId == habitId && it.type == "SUCCESS" }.sortedByDescending { it.timestamp }
        if (checkIns.isEmpty()) return 0

        var streak = 0
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        var currentDay = cal.timeInMillis
        var foundAny = false

        for (i in 0..365) {
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val dayBit = when (dayOfWeek) {
                Calendar.MONDAY -> 1; Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 4; Calendar.THURSDAY -> 8
                Calendar.FRIDAY -> 16; Calendar.SATURDAY -> 32
                Calendar.SUNDAY -> 64; else -> 0
            }
            val isRestDay = (habit.restDays and dayBit) != 0
            val isExpectedDay = (habit.weeklyDays and dayBit) != 0

            val hasCheckInOnThisDay = checkIns.any {
                val ciCal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
                ciCal.set(Calendar.HOUR_OF_DAY, 0); ciCal.set(Calendar.MINUTE, 0)
                ciCal.set(Calendar.SECOND, 0); ciCal.set(Calendar.MILLISECOND, 0)
                ciCal.timeInMillis == currentDay
            }

            if (isExpectedDay && !isRestDay) {
                if (hasCheckInOnThisDay) { streak++; foundAny = true }
                else if (i == 0) { /* hoje ainda pode ser feito */ }
                else break
            }
            cal.add(Calendar.DAY_OF_YEAR, -1)
            currentDay = cal.timeInMillis
        }
        return if (foundAny) streak else 0
    }
}

class HabitViewModelFactory(private val repository: HabitRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HabitViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HabitViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
