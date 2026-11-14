package com.lumiere.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumiere.app.data.repository.NotificationsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val repository: NotificationsRepository
) : ViewModel() {

    val enabled: StateFlow<Boolean> = repository.enabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), repository.enabled.value)

    fun setEnabled(value: Boolean) {
        viewModelScope.launch { repository.setEnabled(value) }
    }
}
