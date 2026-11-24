package com.lumiere.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumiere.app.core.engine.IntelligenceEngine
import com.lumiere.app.core.engine.TemporalConflict
import com.lumiere.app.core.model.Recommendation
import com.lumiere.app.core.model.Suggestion
import com.lumiere.app.data.local.dao.TaskDao
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class LifeAreaProgress(
    val title: String,
    val subtitle: String,
    val progress: Float
)

class IntelligenceViewModel(
    private val engine: IntelligenceEngine,
    private val taskDao: TaskDao
) : ViewModel() {

    private val _recommendation = MutableStateFlow<Recommendation?>(null)
    val recommendation: StateFlow<Recommendation?> = _recommendation.asStateFlow()

    private val _conflicts = MutableStateFlow<List<TemporalConflict>>(emptyList())
    val conflicts: StateFlow<List<TemporalConflict>> = _conflicts.asStateFlow()

    private val _focusTrend = MutableStateFlow<List<Float>>(emptyList())
    val focusTrend: StateFlow<List<Float>> = _focusTrend.asStateFlow()

    private val _lifeAreas = MutableStateFlow<List<LifeAreaProgress>>(emptyList())
    val lifeAreas: StateFlow<List<LifeAreaProgress>> = _lifeAreas.asStateFlow()

    private val _history = MutableStateFlow<List<com.lumiere.app.core.engine.ApprovalDecision>>(emptyList())
    val history: StateFlow<List<com.lumiere.app.core.engine.ApprovalDecision>> = _history.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        refreshInsights()
        viewModelScope.launch {
            runCatching { engine.sweepExpired() }
        }
    }

    fun refreshInsights() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _recommendation.value = engine.analyzeWeek().getOrNull()
                _conflicts.value = engine.detectConflicts().getOrElse { emptyList() }
                _focusTrend.value = computeFocusTrend()
                _lifeAreas.value = computeLifeAreas()
                _history.value = engine.getAllDecisions().getOrElse { emptyList() }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _recommendation.value = null
                _conflicts.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun approveSuggestion(suggestion: Suggestion) {
        viewModelScope.launch {
            val result = engine.approveSuggestion(suggestion)
            if (result.isSuccess) {
                removeSuggestionFromUi(suggestion)
                refreshInsights()
            } else {
                // Se falhar, o ecrã NÃO é atualizado para esconder o problema
                android.util.Log.e("IntelligenceVM", "Erro ao aprovar sugestão: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun rejectSuggestion(suggestion: Suggestion) {
        viewModelScope.launch {
            val result = engine.rejectSuggestion(suggestion)
            if (result.isSuccess) {
                removeSuggestionFromUi(suggestion)
                refreshInsights()
            }
        }
    }

    private fun removeSuggestionFromUi(suggestion: Suggestion) {
        val current = _recommendation.value ?: return
        val updatedList = current.suggestions.filter { it.id != suggestion.id }
        _recommendation.value = current.copy(suggestions = updatedList)
    }

    private suspend fun computeFocusTrend(): List<Float> {
        val now = System.currentTimeMillis()
        val dayMs = 24L * 60 * 60 * 1000
        val timestamps = taskDao.completionTimestampsSince(now - 7 * dayMs)
        val byDay = timestamps.groupBy { it / dayMs }.mapValues { it.value.size }
        val today = now / dayMs
        val maxCount = (byDay.values.maxOrNull() ?: 0).coerceAtLeast(1)
        return (0 until 7).map { offset ->
            val day = today - (6 - offset)
            val count = byDay[day] ?: 0
            (count.toFloat() / maxCount) * 100f
        }
    }

    private suspend fun computeLifeAreas(): List<LifeAreaProgress> {
        val tasks = taskDao.getAllTasks().first()
        if (tasks.isEmpty()) {
            return DEFAULT_AREAS.map { LifeAreaProgress(it, "Sem dados", 0f) }
        }
        return tasks.groupBy { it.category?.takeIf { c -> c.isNotBlank() } ?: "Sem categoria" }
            .entries
            .take(5)
            .map { (cat, list) ->
                val done = list.count { it.isCompleted }
                val progress = if (list.isEmpty()) 0f else done.toFloat() / list.size
                LifeAreaProgress(cat, "$done/${list.size} tarefas", progress)
            }
    }

    companion object {
        private val DEFAULT_AREAS = listOf("Pessoal", "Trabalho", "Estudos", "Financas", "Saude")
    }
}
