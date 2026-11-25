package com.lumiere.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumiere.app.data.local.entity.Task
import com.lumiere.app.ui.components.*
import com.lumiere.app.ui.theme.LumiereIcons
import com.lumiere.app.ui.theme.LumiereRadius
import com.lumiere.app.ui.theme.LumiereSpacing
import com.lumiere.app.ui.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    viewModel: TaskViewModel,
    onOpenDrawer: () -> Unit = {},
    onTaskClick: (Task) -> Unit = {},
    onOpenProjects: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val now = System.currentTimeMillis()
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

    // ESTADO PARA O QUICK ADD
    var showQuickAdd by remember { mutableStateOf(false) }
    
    // ESTADO PARA CONFIRMAÇÃO DE ELIMINAÇÃO (Próximo passo)
    var taskToDelete by remember { mutableStateOf<Task?>(null) }
    var confirmText by remember { mutableStateOf("") }

    val overdueCount = uiState.tasks.count { !it.isCompleted && it.dueDate != null && it.dueDate < now }
    val todayCount = uiState.tasks.count { !it.isCompleted && it.dueDate != null && it.dueDate >= now }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Cabeçalho
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(LumiereSpacing.M),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onOpenDrawer) { Icon(LumiereIcons.Notes, contentDescription = "Menu", tint = MaterialTheme.colorScheme.primary) }
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text("Hoje", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text("Vamos fazer hoje ainda melhor.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    IconButton(onClick = onOpenProjects) { Icon(LumiereIcons.Folder, contentDescription = "Projetos", tint = MaterialTheme.colorScheme.primary) }
                }
            }

            // Resumo
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = LumiereSpacing.M),
                    horizontalArrangement = Arrangement.spacedBy(LumiereSpacing.S)
                ) {
                    SummaryCard(Modifier.weight(1f), todayCount.toString(), "Hoje", MaterialTheme.colorScheme.primary)
                    SummaryCard(Modifier.weight(1f), overdueCount.toString(), "Atrasadas", MaterialTheme.colorScheme.error)
                    SummaryCard(Modifier.weight(1f), "68%", "Foco", MaterialTheme.colorScheme.primary)
                }
            }

            // Abas
            item {
                ScrollableTabRow(
                    selectedTabIndex = 0,
                    edgePadding = LumiereSpacing.M,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {}
                ) {
                    Tab(selected = true, onClick = { }, text = { Text("Todas") })
                    Tab(selected = false, onClick = { }, text = { Text("Hoje") })
                    Tab(selected = false, onClick = { }, text = { Text("Atrasadas") })
                    Tab(selected = false, onClick = { }, text = { Text("Concluídas") })
                }
            }

            // Lista de Tarefas
            val displayTasks = uiState.filteredTasks
            if (displayTasks.isEmpty()) {
                item { Text("Nenhuma tarefa encontrada.", modifier = Modifier.padding(LumiereSpacing.M), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                items(displayTasks) { task ->
                    val cardState = when {
                        task.isCompleted -> LumiereCardState.DONE
                        task.dueDate != null && task.dueDate < now -> LumiereCardState.OVERDUE
                        else -> LumiereCardState.PENDING
                    }
                    val metadataText = task.dueDate?.let { dateFormat.format(Date(it)) }

                    LumiereUniversalCard(
                        variant = CardVariant.ENTITY,
                        density = CardDensity.NORMAL,
                        data = CardData(
                            title = task.title,
                            description = task.description,
                            subtitle = task.category ?: "Pessoal",
                            state = cardState,
                            metadata = metadataText,
                            attachmentCount = task.attachmentUris?.split(",")?.size,
                            isPriority = task.priority > 1,
                            actions = listOf(
                                CardAction(
                                    label = "Concluir",
                                    icon = LumiereIcons.CheckCircle,
                                    isPrimary = true,
                                    onClick = { viewModel.toggleTaskCompletion(task) }
                                ),
                                CardAction(
                                    label = "Editar",
                                    icon = LumiereIcons.Edit,
                                    onClick = { onTaskClick(task) }
                                )
                            ),
                            menuActions = listOf(
                                CardAction(label = "Duplicar", icon = LumiereIcons.Copy, onClick = { viewModel.duplicateTask(task) }),
                                CardAction(label = "Arquivar", icon = LumiereIcons.Archive, onClick = { viewModel.archiveTask(task) }),
                                CardAction(label = "Eliminar", icon = LumiereIcons.Delete, onClick = { taskToDelete = task })
                            )
                        ),
                        onClick = { onTaskClick(task) },
                        modifier = Modifier.padding(horizontal = LumiereSpacing.M, vertical = 6.dp)
                    )
                }
            }
        }

        // FAB LIGADO AO QUICK ADD
        FloatingActionButton(
            onClick = { showQuickAdd = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape,
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 24.dp, end = LumiereSpacing.M)
        ) {
            Icon(LumiereIcons.Add, contentDescription = "Adicionar Tarefa", modifier = Modifier.size(32.dp))
        }
    }

    // FORMULÁRIO RÁPIDO (Bottom Sheet)
    if (showQuickAdd) {
        val quickFormData = UniversalFormData(
            title = "Nova Tarefa Rápida",
            fields = listOf(
                FormField(id = "title", label = "Título da Tarefa", placeholder = "Ex: Comprar leite", isRequired = true, step = 1)
            )
        )
        LumiereUniversalForm(
            mode = FormMode.QUICK,
            data = quickFormData,
            onDismiss = { showQuickAdd = false },
            onSubmit = { values ->
                val title = values["title"] ?: ""
                if (title.isNotBlank()) {
                    viewModel.addTask(Task(title = title))
                    showQuickAdd = false
                }
            }
        )
    }

    // DIÁLOGO DE ELIMINAÇÃO (Implementado a seguir)
    if (taskToDelete != null) {
        AlertDialog(
            onDismissRequest = { taskToDelete = null; confirmText = "" },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Eliminar tarefa?", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column {
                    Text("Esta ação não pode ser desfeita. O histórico será preservado.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Escreve ELIMINAR para confirmar", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = confirmText,
                        onValueChange = { confirmText = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.error, unfocusedBorderColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(LumiereRadius.S)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { taskToDelete?.let { viewModel.deleteTask(it) }; taskToDelete = null; confirmText = "" },
                    enabled = confirmText == "ELIMINAR",
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { taskToDelete = null; confirmText = "" }) { Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        )
    }
}

@Composable
private fun SummaryCard(modifier: Modifier, value: String, label: String, color: androidx.compose.ui.graphics.Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(LumiereRadius.M)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
