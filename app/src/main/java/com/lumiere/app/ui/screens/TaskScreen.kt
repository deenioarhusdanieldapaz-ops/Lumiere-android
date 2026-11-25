package com.lumiere.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumiere.app.data.local.entity.Task
import com.lumiere.app.ui.components.CardData
import com.lumiere.app.ui.components.LumiereCardState
import com.lumiere.app.ui.components.LumiereUniversalCard
import com.lumiere.app.ui.theme.LumiereRadius
import com.lumiere.app.ui.theme.LumiereSpacing
import com.lumiere.app.ui.viewmodel.TaskViewModel

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

    // Contadores
    val overdueCount = uiState.tasks.count { !it.isCompleted && it.dueDate != null && it.dueDate < now }
    val todayCount = uiState.tasks.count { !it.isCompleted && it.dueDate != null && it.dueDate >= now }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // 1. Cabeçalho
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(LumiereSpacing.M),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onOpenDrawer) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.primary)
                        }
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text("Hoje", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text("Vamos fazer hoje ainda melhor.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Row {
                        IconButton(onClick = onOpenProjects) {
                            Icon(Icons.Default.Folder, contentDescription = "Projetos", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // 2. Resumo
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

            // 3. Abas
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

            // 4. Lista de Tarefas
            val displayTasks = uiState.filteredTasks
            if (displayTasks.isEmpty()) {
                item {
                    Text("Nenhuma tarefa encontrada.", modifier = Modifier.padding(LumiereSpacing.M), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(displayTasks) { task ->
                    val cardState = when {
                        task.isCompleted -> LumiereCardState.DONE
                        task.dueDate != null && task.dueDate < now -> LumiereCardState.OVERDUE
                        else -> LumiereCardState.PENDING
                    }
                    
                    LumiereUniversalCard(
                        data = CardData(
                            title = task.title,
                            subtitle = task.category ?: "Sem categoria",
                            state = cardState
                        ),
                        onClick = { onTaskClick(task) },
                        modifier = Modifier.padding(horizontal = LumiereSpacing.M, vertical = 4.dp)
                    )
                }
            }
        }

        // 5. FAB
        FloatingActionButton(
            onClick = { /* Quick Add */ },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape,
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 24.dp, end = LumiereSpacing.M)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Adicionar Tarefa", modifier = Modifier.size(32.dp))
        }
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
