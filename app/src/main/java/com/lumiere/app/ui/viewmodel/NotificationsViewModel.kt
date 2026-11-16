package com.lumiere.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumiere.app.data.repository.NotificationsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationsViewModel(private val repository: NotificationsRepository) : ViewModel() {
    private fun <T> NotificationsRepository.toState(flow: kotlinx.coroutines.flow.StateFlow<T>) = 
        flow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), flow.value)

    val enabled = repository.toState(repository.enabled)
    val soundEnabled = repository.toState(repository.soundEnabled)
    val vibrationEnabled = repository.toState(repository.vibrationEnabled)
    val volume = repository.toState(repository.volume)
    val tone = repository.toState(repository.tone)
    val silenceEnabled = repository.toState(repository.silenceEnabled)
    val criticalAlerts = repository.toState(repository.criticalAlerts)
    val m01 = repository.toState(repository.m01); val m02 = repository.toState(repository.m02)
    val m03 = repository.toState(repository.m03); val m04 = repository.toState(repository.m04)
    val m05 = repository.toState(repository.m05)

    fun setEnabled(v: Boolean) = viewModelScope.launch { repository.setEnabled(v) }
    fun setSoundEnabled(v: Boolean) = viewModelScope.launch { repository.setSoundEnabled(v) }
    fun setVibrationEnabled(v: Boolean) = viewModelScope.launch { repository.setVibrationEnabled(v) }
    fun setVolume(v: Int) = viewModelScope.launch { repository.setVolume(v) }
    fun setTone(v: String) = viewModelScope.launch { repository.setTone(v) }
    fun setSilenceEnabled(v: Boolean) = viewModelScope.launch { repository.setSilenceEnabled(v) }
    fun setCriticalAlerts(v: Boolean) = viewModelScope.launch { repository.setCriticalAlerts(v) }
    fun setM01(v: Boolean) = viewModelScope.launch { repository.setM01(v) }
    fun setM02(v: Boolean) = viewModelScope.launch { repository.setM02(v) }
    fun setM03(v: Boolean) = viewModelScope.launch { repository.setM03(v) }
    fun setM04(v: Boolean) = viewModelScope.launch { repository.setM04(v) }
    fun setM05(v: Boolean) = viewModelScope.launch { repository.setM05(v) }
}
