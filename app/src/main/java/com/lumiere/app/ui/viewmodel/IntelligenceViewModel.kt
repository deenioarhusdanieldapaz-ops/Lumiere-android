package com.lumiere.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumiere.app.core.engine.IntelligenceEngine
import com.lumiere.app.core.model.Recommendation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IntelligenceViewModel(
    private val engine: IntelligenceEngine
) : ViewModel() {

    private val _recommendations = MutableStateFlow<List<Recommendation>>(emptyList())
    val recommendations: StateFlow<List<Recommendation>> = _recommendations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun refreshInsights() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = engine.analyzeWeek()
            if (result.isSuccess) {
                _recommendations.value = result.getOrDefault(emptyList())
            }
            _isLoading.value = false
        }
    }
}
