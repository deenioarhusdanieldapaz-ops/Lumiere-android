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


    fun updateHabit(habit: Habit) {
        viewModelScope.launch { repository.updateHabit(habit) }
    }

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
        viewModelScope.launch {
            repository.updateHabit(habit.copy(status = 0))
        }
    }

    fun addFailureCheckIn(habitId: Long) {
        viewModelScope.launch {
            repository.insertCheckIn(
                HabitCheckIn(habitId = habitId, value = 0, type = "FAIL")
            )
        }
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
        val habit = habits.value.find { it.id == habitId } ?: return 0
        val checkIns = allCheckIns.value.filter { it.habitId == habitId }.sortedByDescending { it.timestamp }
        if (checkIns.isEmpty()) return 0

        var streak = 0
        val cal = Calendar.getInstance()
        
        // Começar no dia de hoje e voltar para trás
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        
        var currentDay = cal.timeInMillis
        var foundAny = false

        // Loop pelos últimos 365 dias (limite de segurança)
        for (i in 0..365) {
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1=Domingo, 2=Segunda...
            // Converter para bitmask (Segunda=1, Domingo=64)
            val dayBit = when (dayOfWeek) {
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 4
                Calendar.THURSDAY -> 8
                Calendar.FRIDAY -> 16
                Calendar.SATURDAY -> 32
                Calendar.SUNDAY -> 64
                else -> 0
            }
            
            val isRestDay = (habit.restDays and dayBit) != 0
            val isExpectedDay = (habit.weeklyDays and dayBit) != 0
            
            val hasCheckInOnThisDay = checkIns.any { 
                val ciCal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
                ciCal.set(Calendar.HOUR_OF_DAY, 0); ciCal.set(Calendar.MINUTE, 0); ciCal.set(Calendar.SECOND, 0); ciCal.set(Calendar.MILLISECOND, 0)
                ciCal.timeInMillis == currentDay 
            }

            if (isExpectedDay && !isRestDay) {
                if (hasCheckInOnThisDay) {
                    streak++
                    foundAny = true
                } else if (i == 0) {
                    // Se é hoje e ainda não há check-in, não quebra (o dia ainda não acabou)
                    // Não faz nada, deixa o streak continuar para ontem
                } else {
                    // Falhou um dia esperado, quebra a sequência
                    break
                }
            }
            
            // Avançar para o dia anterior
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
