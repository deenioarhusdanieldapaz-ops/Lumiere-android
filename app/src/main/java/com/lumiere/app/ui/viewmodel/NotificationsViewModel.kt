package com.lumiere.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumiere.app.data.repository.NotificationsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationsViewModel(private val repository: NotificationsRepository) : ViewModel() {
    val enabled = repository.enabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.enabled.value)
    val soundEnabled = repository.soundEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.soundEnabled.value)
    val vibrationEnabled = repository.vibrationEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.vibrationEnabled.value)

    fun setEnabled(value: Boolean) = viewModelScope.launch { repository.setEnabled(value) }
    fun setSoundEnabled(value: Boolean) = viewModelScope.launch { repository.setSoundEnabled(value) }
    fun setVibrationEnabled(value: Boolean) = viewModelScope.launch { repository.setVibrationEnabled(value) }
}
