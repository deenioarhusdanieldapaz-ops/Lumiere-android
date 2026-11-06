package com.lumiere.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumiere.app.ui.theme.LumiereDarkGray
import com.lumiere.app.ui.theme.LumiereGold
import com.lumiere.app.ui.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var taskTitle by remember { mutableStateOf("") }

    val completedTasks = tasks.count { it.isCompleted }
    val pendingTasks = tasks.count { !it.isCompleted }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("M01 — Tarefas", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nova Tarefa")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text(
                        text = "Olá!",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Vamos fazer hoje ainda melhor.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        title = "Tarefas de Hoje",
                        count = pendingTasks.toString(),
                        icon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = LumiereGold) }
                    )
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        title = "Concluídas",
                        count = completedTasks.toString(),
                        icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green) }
                    )
                }
            }

            if (pendingTasks > 0) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF3E1A1A))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Atenção: $pendingTasks tarefas pendentes", color = Color.White)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Minhas Tarefas",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (tasks.isEmpty()) {
                item {
                    Text(
                        text = "Ainda não tem tarefas. Clica no + para começar.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            } else {
                items(tasks) { task ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = LumiereDarkGray),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.DateRange,
                                contentDescription = null,
                                tint = if (task.isCompleted) Color.Green else LumiereGold
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = task.title,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                if (task.description != null) {
                                    Text(
                                        text = task.description,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Nova Tarefa", color = MaterialTheme.colorScheme.primary) },
            text = {
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    label = { Text("Título da Tarefa") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (taskTitle.isNotBlank()) {
                        viewModel.addTask(taskTitle)
                        taskTitle = ""
                        showDialog = false
                    }
                }) {
                    Text("Adicionar", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun SummaryCard(modifier: Modifier = Modifier, title: String, count: String, icon: @Composable () -> Unit) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = LumiereDarkGray),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                icon()
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = count, color = MaterialTheme.colorScheme.onBackground, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
    }
}
