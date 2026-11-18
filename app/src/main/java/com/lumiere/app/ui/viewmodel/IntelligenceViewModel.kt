package com.lumiere.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumiere.app.data.repository.HabitRepository
import com.lumiere.app.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LifeAreaProgress(
    val title: String,
    val subtitle: String,
    val progress: Float
)

class IntelligenceViewModel(
    private val taskRepo: TaskRepository,
    private val habitRepo: HabitRepository
) : ViewModel() {

    // Dados mockados provisórios até a Fase 5 (Ligação ao Core)
    private val _focusTrend = MutableStateFlow(listOf(20f, 35f, 30f, 50f, 45f, 70f, 85f))
    val focusTrend: StateFlow<List<Float>> = _focusTrend.asStateFlow()

    private val _lifeAreas = MutableStateFlow(
        listOf(
            LifeAreaProgress("Vida Pessoal", "Hábitos M02, Rotinas M10", 0.65f),
            LifeAreaProgress("Desenvolvimento", "Metas M03, Estudos M04", 0.40f),
            LifeAreaProgress("Produtividade", "Tarefas M01, Calendário M05", 0.80f),
            LifeAreaProgress("Finanças", "Finanças M06, Negócio M09", 0.35f),
            LifeAreaProgress("Informação", "Notas M07, Docs M13", 0.55f)
        )
    )
    val lifeAreas: StateFlow<List<LifeAreaProgress>> = _lifeAreas.asStateFlow()

    fun refreshInsights() {
        viewModelScope.launch {
            // Aqui, na Fase 5, leremos os dados reais dos Repositórios.
            // Ex: val habits = habitRepo.getCheckIns(1).first()
        }
    }
}
