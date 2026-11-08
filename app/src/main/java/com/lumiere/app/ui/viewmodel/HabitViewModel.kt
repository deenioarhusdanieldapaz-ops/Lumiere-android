package com.lumiere.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lumiere.app.data.local.entity.*
import com.lumiere.app.data.repository.HabitRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class HabitViewModel(private val repository: HabitRepository) : ViewModel() {
    val habits: StateFlow<List<Habit>> = repository.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allCheckIns: StateFlow<List<HabitCheckIn>> = repository.allCheckIns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addHabit(habit: Habit) {
        viewModelScope.launch { repository.insertHabit(habit) }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch { repository.deleteHabit(habit) }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch { repository.updateHabit(habit) }
    }

    fun addCheckIn(checkIn: HabitCheckIn) {
        viewModelScope.launch { repository.insertCheckIn(checkIn) }
    }


    fun addCheckInValue(habitId: Long, currentValue: Int, targetValue: Int?) {
        viewModelScope.launch {
            val newValue = currentValue + 1
            repository.insertCheckIn(
                HabitCheckIn(
                    habitId = habitId,
                    value = if (targetValue != null) newValue else 1
                )
            )
        }
    }

    fun getTodayValueForHabit(habitId: Long, allCheckIns: List<HabitCheckIn>): Int {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis
        val endOfDay = startOfDay + 86400000L - 1

        return allCheckIns
            .filter { it.habitId == habitId && it.timestamp in startOfDay..endOfDay }
            .sumOf { it.value ?: 1 }
    }

    fun getCheckInsForHabit(habitId: Long): StateFlow<List<HabitCheckIn>> = repository.getCheckIns(habitId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getTodayCheckIns(): Map<Long, HabitCheckIn> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        
        val endCal = Calendar.getInstance()
        endCal.set(Calendar.HOUR_OF_DAY, 23)
        endCal.set(Calendar.MINUTE, 59)
        endCal.set(Calendar.SECOND, 59)
        endCal.set(Calendar.MILLISECOND, 999)
        
        return emptyMap()
    }


    fun calculateStreak(habitId: Long): Int {
        val checkIns = allCheckIns.value.filter { it.habitId == habitId }.sortedByDescending { it.timestamp }
        if (checkIns.isEmpty()) return 0

        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val todayStart = cal.timeInMillis

        var foundToday = false
        var foundYesterday = false

        for (ci in checkIns) {
            val ciCal = Calendar.getInstance().apply { timeInMillis = ci.timestamp }
            ciCal.set(Calendar.HOUR_OF_DAY, 0); ciCal.set(Calendar.MINUTE, 0); ciCal.set(Calendar.SECOND, 0); ciCal.set(Calendar.MILLISECOND, 0)
            
            if (ciCal.timeInMillis == todayStart) foundToday = true
            if (ciCal.timeInMillis == todayStart - 86400000L) foundYesterday = true
            if (foundToday || foundYesterday) break
        }

        if (!foundToday && !foundYesterday) return 0

        var streak = 0
        var expectedTime = if (foundToday) todayStart else todayStart - 86400000L
        for (ci in checkIns) {
            val ciCal = Calendar.getInstance().apply { timeInMillis = ci.timestamp }
            ciCal.set(Calendar.HOUR_OF_DAY, 0); ciCal.set(Calendar.MINUTE, 0); ciCal.set(Calendar.SECOND, 0); ciCal.set(Calendar.MILLISECOND, 0)
            
            if (ciCal.timeInMillis == expectedTime) {
                streak++
                expectedTime -= 86400000L
            } else if (ciCal.timeInMillis < expectedTime) {
                break
            }
        }
        return streak
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
