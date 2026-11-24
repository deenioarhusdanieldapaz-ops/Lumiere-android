package com.lumiere.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lumiere.app.data.local.entity.Project
import com.lumiere.app.data.local.entity.Task
import com.lumiere.app.ui.components.*
import com.lumiere.app.ui.theme.LumiereRadius
import com.lumiere.app.ui.theme.LumiereSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    task: Task,
    projects: List<Project>,
    onBack: () -> Unit,
    onSave: (Task) -> Unit
) {
    val projectOptions = listOf("Sem projeto") + projects.map { it.name }
    val currentProjectName = projects.find { it.id == task.projectId }?.name ?: "Sem projeto"

    // Mapear os 12 campos para o formulário
    val formData = UniversalFormData(
        title = "Editar tarefa",
        fields = listOf(
            FormField(id = "title", label = "Título", placeholder = "Ex: Preparar apresentação", isRequired = true, hint = "Mantém curto e claro"),
            FormField(id = "description", label = "Descrição", placeholder = "Adiciona mais detalhes..."),
            FormField(id = "project", label = "Projeto", type = FormFieldType.DROPDOWN, options = projectOptions, placeholder = currentProjectName),
            FormField(id = "category", label = "Categoria", placeholder = task.category ?: "Ex: Trabalho"),
            FormField(id = "startDate", label = "Data de início", type = FormFieldType.DATE, placeholder = "Hoje, 09:00"),
            FormField(id = "dueDate", label = "Prazo", type = FormFieldType.DATE, placeholder = "Hoje, 18:00"),
            FormField(id = "duration", label = "Duração estimada (min)", placeholder = task.estimatedDurationMinutes?.toString() ?: "45"),
            FormField(id = "recurrence", label = "Recorrência", type = FormFieldType.DROPDOWN, options = listOf("Nenhuma", "Diária", "Semanal", "Mensal", "Anual"), placeholder = "Nenhuma"),
            FormField(id = "priority", label = "Prioridade", type = FormFieldType.PRIORITY, options = listOf("Baixa", "Média", "Alta")),
            FormField(id = "tags", label = "Tags", placeholder = "urgente, equipa"),
            FormField(id = "blockedBy", label = "Bloqueado por (ID)", placeholder = task.blockedByTaskId?.toString() ?: "Nenhum")
        )
    )

    // Estado para guardar os valores temporários
    val initialValues = mapOf(
        "title" to task.title,
        "description" to (task.description ?: ""),
        "project" to currentProjectName,
        "category" to (task.category ?: ""),
        "priority" to when(task.priority) { 1 -> "Baixa"; 2 -> "Média"; 3 -> "Alta"; else -> "Média" },
        "duration" to (task.estimatedDurationMinutes?.toString() ?: ""),
        "recurrence" to (task.recurrenceRule ?: "Nenhuma"),
        "tags" to (task.tags ?: "")
    )

    LumiereUniversalForm(
        mode = FormMode.FULL,
        data = formData,
        onDismiss = onBack,
        onSubmit = { values ->
            val selectedProject = projects.find { it.name == values["project"] }
            val updatedTask = task.copy(
                title = values["title"] ?: task.title,
                description = values["description"],
                projectId = selectedProject?.id,
                category = values["category"],
                priority = when(values["priority"]) { "Baixa" -> 1; "Alta" -> 3; else -> 2 },
                estimatedDurationMinutes = values["duration"]?.toIntOrNull(),
                recurrenceRule = if (values["recurrence"] == "Nenhuma") null else values["recurrence"]?.uppercase(),
                tags = values["tags"]
            )
            onSave(updatedTask)
        }
    )
}
