package com.lumiere.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumiere.app.data.local.entity.Task
import com.lumiere.app.ui.components.*
import com.lumiere.app.ui.theme.Lumiere
import com.lumiere.app.ui.theme.LumiereRadius
import com.lumiere.app.ui.theme.LumiereSpacing
import com.lumiere.app.ui.viewmodel.TaskViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    viewModel: TaskViewModel,
    onOpenDrawer: () -> Unit = {},
    onTaskClick: (Task) -> Unit = {},
    onOpenProjects: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showQuickAdd by remember { mutableStateOf(false) }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }
    var confirmText by remember { mutableStateOf("") }

    val tasks = uiState.tasks
    val now = System.currentTimeMillis()
    val endOfDay = getEndOfDay(now)

    val overdueTasks = tasks.filter { !it.isCompleted && it.dueDate != null && it.dueDate < now }
    val todayTasks = tasks.filter { !it.isCompleted && it.dueDate != null && it.dueDate >= now && it.dueDate <= endOfDay }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = LumiereSpacing.M, vertical = LumiereSpacing.M), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onOpenDrawer) { Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.primary) }
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text("Hoje", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.primary)
                            Text("Vamos fazer hoje ainda melhor.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Row {
                        IconButton(onClick = onOpenProjects) { Icon(Icons.Default.Folder, contentDescription = "Projetos", tint = MaterialTheme.colorScheme.primary) }
                        IconButton(onClick = { }) { Icon(Icons.Default.Search, contentDescription = "Pesquisar", tint = MaterialTheme.colorScheme.primary) }
                    }
                }
            }

            item {
                ScrollableTabRow(
                    selectedTabIndex = when(uiState.activeFilter) {
                        "TODAY" -> 1
                        "COMPLETED" -> 3
                        "OVERDUE" -> 2
                        else -> 0
                    },
                    edgePadding = LumiereSpacing.M,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {}
                ) {
                    Tab(selected = uiState.activeFilter == "ALL", onClick = { viewModel.setFilter("ALL") }, text = { Text("Todas") })
                    Tab(selected = uiState.activeFilter == "TODAY", onClick = { viewModel.setFilter("TODAY") }, text = { Text("Hoje") })
                    Tab(selected = uiState.activeFilter == "OVERDUE", onClick = { viewModel.setFilter("OVERDUE") }, text = { Text("Atrasadas") })
                    Tab(selected = uiState.activeFilter == "COMPLETED", onClick = { viewModel.setFilter("COMPLETED") }, text = { Text("Concluídas") })
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth().padding(LumiereSpacing.M), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryBox(Modifier.weight(1f), todayTasks.size.toString(), "Hoje", Icons.Default.CalendarToday, MaterialTheme.colorScheme.primary)
                    SummaryBox(Modifier.weight(1f), overdueTasks.size.toString(), "Atrasada", Icons.Default.Warning, MaterialTheme.colorScheme.error)
                    SummaryBox(Modifier.weight(1f), "68%", "Foco", Icons.Default.TrackChanges, MaterialTheme.colorScheme.primary)
                }
            }

            val displayTasks = uiState.filteredTasks
            if (displayTasks.isEmpty()) {
                item { Text("Nenhuma tarefa encontrada.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(LumiereSpacing.M)) }
            } else {
                items(displayTasks) { task ->
                    LumiereUniversalCard(
                        variant = CardVariant.ENTITY,
                        density = CardDensity.COMPACT,
                        data = CardData(
                            title = task.title,
                            subtitle = task.category ?: "Pessoal",
                            state = when {
                                task.isCompleted -> LumiereCardState.DONE
                                task.dueDate != null && task.dueDate < now -> LumiereCardState.OVERDUE
                                else -> LumiereCardState.PENDING
                            },
                            actions = listOf(
                                CardAction(
                                    label = "Eliminar",
                                    isPrimary = false,
                                    onClick = { taskToDelete = task }
                                )
                            )
                        ),
                        onClick = { onTaskClick(task) },
                        modifier = Modifier.padding(horizontal = LumiereSpacing.M, vertical = 4.dp)
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showQuickAdd = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.Black,
            shape = CircleShape,
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 24.dp, end = LumiereSpacing.M)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Adicionar Tarefa", modifier = Modifier.size(32.dp))
        }
    }

    if (showQuickAdd) {
        LumiereUniversalForm(
            mode = FormMode.QUICK,
            data = UniversalFormData(
                title = "Nova Tarefa",
                fields = listOf(FormField(id = "title", label = "Título", placeholder = "Ex: Comprar leite", isRequired = true))
            ),
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
private fun SummaryBox(modifier: Modifier, value: String, label: String, icon: ImageVector, color: Color) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(LumiereRadius.M))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(LumiereRadius.M))
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(0.5f).height(2.dp).background(color))
    }
}

private fun getEndOfDay(timestamp: Long): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = timestamp
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    cal.set(Calendar.MILLISECOND, 999)
    return cal.timeInMillis
}
