package com.lumiere.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.lumiere.app.data.local.entity.Project
import com.lumiere.app.data.local.entity.Task
import com.lumiere.app.ui.components.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val startDateStr = task.startDate?.let { dateFormat.format(Date(it)) } ?: ""
    val dueDateStr = task.dueDate?.let { dateFormat.format(Date(it)) } ?: ""

    // Mapeamento dos campos distribuídos pelos 3 passos
    val formData = UniversalFormData(
        title = "Editar Tarefa",
        fields = listOf(
            // Passo 1: Informação Principal
            FormField(id = "title", label = "Título da Tarefa", placeholder = "Ex: Preparar apresentação", isRequired = true, hint = "Mantém curto e claro", step = 1),
            FormField(id = "description", label = "Descrição", placeholder = "Adicione mais detalhes...", hint = "Opcional", step = 1, maxLength = 500),
            FormField(id = "priority", label = "Prioridade", type = FormFieldType.PRIORITY, options = listOf("Baixa", "Média", "Alta"), step = 1),

            // Passo 2: Planeamento e Tempo
            FormField(id = "startDate", label = "Data e Hora de Início", type = FormFieldType.DATETIME, placeholder = "DD/MM/AAAA HH:MM", step = 2),
            FormField(id = "dueDate", label = "Data e Hora de Prazo", type = FormFieldType.DATETIME, placeholder = "DD/MM/AAAA HH:MM", step = 2),
            FormField(id = "duration", label = "Duração Estimada (min)", placeholder = "Ex: 45", step = 2),
            FormField(id = "recurrence", label = "Recorrência", type = FormFieldType.DROPDOWN, options = listOf("Nenhuma", "Diária", "Semanal", "Mensal", "Anual"), step = 2),

            // Passo 3: Organização
            FormField(id = "project", label = "Projeto", type = FormFieldType.DROPDOWN, options = projectOptions, step = 3),
            FormField(id = "category", label = "Categoria", placeholder = "Ex: Trabalho, Pessoal", step = 3),
            FormField(id = "blockedBy", label = "Bloqueado por (ID)", placeholder = "Nenhuma dependência", hint = "ID da tarefa bloqueadora", step = 3),
            FormField(id = "attachments", label = "Anexos", type = FormFieldType.ATTACHMENT, step = 3)
        )
    )

    val initialValues = mapOf(
        "title" to task.title,
        "description" to (task.description ?: ""),
        "project" to currentProjectName,
        "category" to (task.category ?: ""),
        "priority" to when(task.priority) { 1 -> "Baixa"; 2 -> "Média"; 3 -> "Alta"; else -> "Média" },
        "startDate" to startDateStr,
        "dueDate" to dueDateStr,
        "duration" to (task.estimatedDurationMinutes?.toString() ?: ""),
        "recurrence" to (task.recurrenceRule ?: "Nenhuma"),
        "blockedBy" to (task.blockedByTaskId?.toString() ?: "")
    )

    LumiereUniversalForm(
        mode = FormMode.FULL,
        data = formData,
        initialValues = initialValues,
        onDismiss = onBack,
        onSubmit = { values ->
            val selectedProject = projects.find { it.name == values["project"] }
            val parsedStartDate = try { dateFormat.parse(values["startDate"] ?: "")?.time } catch (e: Exception) { task.startDate }
            val parsedDueDate = try { dateFormat.parse(values["dueDate"] ?: "")?.time } catch (e: Exception) { task.dueDate }

            val updatedTask = task.copy(
                title = values["title"] ?: task.title,
                description = values["description"],
                projectId = selectedProject?.id,
                category = values["category"],
                priority = when(values["priority"]) { "Baixa" -> 1; "Alta" -> 3; else -> 2 },
                startDate = parsedStartDate,
                dueDate = parsedDueDate,
                estimatedDurationMinutes = values["duration"]?.toIntOrNull(),
                recurrenceRule = if (values["recurrence"] == "Nenhuma") null else values["recurrence"]?.uppercase(),
                blockedByTaskId = values["blockedBy"]?.toLongOrNull()
            )
            onSave(updatedTask)
        }
    )
}
