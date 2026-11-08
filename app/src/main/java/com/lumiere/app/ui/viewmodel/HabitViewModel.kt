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

    fun updateHabit(habit: Habit) {
        viewModelScope.launch { repository.updateHabit(habit) }
    }

    fun addCheckIn(checkIn: HabitCheckIn) {
        viewModelScope.launch { repository.insertCheckIn(checkIn) }
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
