package com.lumiere.app.core

import com.lumiere.app.core.orchestrator.IntelligenceEngine
import com.lumiere.app.core.contracts.CoreContext
import com.lumiere.app.data.local.entity.Task
import com.lumiere.app.data.local.entity.Habit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreOrchestratorTest {

    private val engine = IntelligenceEngine()

    @Test
    fun `analise deteta tarefas de alta prioridade e exige aprovacao`() {
        val tasks = listOf(
            Task(id = 1, title = "Bug Urgente", isCompleted = false, priority = 1, updatedAt = 0L, status = 0, projectId = null, description = null, dueDate = null, blockedByTaskId = null, links = null, attachments = null, createdAt = 0L, isArchived = false),
            Task(id = 2, title = "Tarefa Normal", isCompleted = false, priority = 3, updatedAt = 0L, status = 0, projectId = null, description = null, dueDate = null, blockedByTaskId = null, links = null, attachments = null, createdAt = 0L, isArchived = false)
        )
        
        val result = engine.analyze(CoreContext(tasks, emptyList()))
        
        assertTrue(result.risks.any { it.contains("alta prioridade") })
        assertTrue(result.recommendations.any { it.id == "REC-TASK-01" && it.requiresApproval })
    }

    @Test
    fun `analise deteta sobrecarga de habitos`() {
        val habits = (1..6).map { 
            Habit(id = it, name = "Habito $it", status = 0, createdAt = 0L, timerAccumulatedMinutes = 0, timerAccumulatedSeconds = 0, frequency = 1, updatedAt = 0L, description = null, colorHex = null, iconName = null)
        }
        
        val result = engine.analyze(CoreContext(emptyList(), habits))
        
        assertTrue(result.risks.any { it.contains("Sobrecarga de hábitos") })
        assertTrue(result.recommendations.any { it.id == "REC-HAB-01" })
    }
}
