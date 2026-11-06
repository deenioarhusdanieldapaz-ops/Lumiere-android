package com.lumiere.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumiere.app.data.local.entity.Task
import com.lumiere.app.ui.theme.LumiereDarkGray
import com.lumiere.app.ui.theme.LumiereGold
import com.lumiere.app.ui.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var taskTitle by remember { mutableStateOf("") }
    var taskPriority by remember { mutableStateOf(1) } // 1=Baixa, 2=Média, 3=Alta

    var taskToDelete by remember { mutableStateOf<Task?>(null) }

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
            ) { Icon(Icons.Default.Add, contentDescription = "Nova Tarefa") }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text("Olá!", color = MaterialTheme.colorScheme.onBackground, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text("Vamos fazer hoje ainda melhor.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryCard(Modifier.weight(1f), "Tarefas de Hoje", pendingTasks.toString(), LumiereGold)
                    SummaryCard(Modifier.weight(1f), "Concluídas", completedTasks.toString(), Color.Green)
                }
            }

            if (pendingTasks > 0) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF3E1A1A))) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color.Red)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Atenção: $pendingTasks tarefas pendentes", color = Color.White)
                        }
                    }
                }
            }

            item { Text("Minhas Tarefas", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(top = 8.dp)) }

            if (tasks.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), colors = CardDefaults.cardColors(containerColor = LumiereDarkGray), shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(32.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = LumiereGold, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Ainda não tem tarefas", color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Comece por criar a sua primeira tarefa ou explore as recomendações.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }
            } else {
                items(tasks) { task ->
                    TaskItem(
                        task = task,
                        onTaskClick = { viewModel.toggleTaskCompletion(task) },
                        onTaskLongClick = { taskToDelete = task } // Abre o aviso de apagar
                    )
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
                    Box(modifier = Modifier.background(brush = Brush.horizontalGradient(colors = listOf(Color(0xFF2A200F), LumiereDarkGray))).padding(20.dp)) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = LumiereGold, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Inteligência Lumière", color = LumiereGold, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Você tem $pendingTasks tarefas em risco de atraso. Vamos organizar o seu dia?", color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            TextButton(onClick = { /* Ação futura */ }) { Text("Ver Recomendações →", color = LumiereGold) }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // DIALOG: Criar Tarefa
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Nova Tarefa", color = MaterialTheme.colorScheme.primary) },
            text = {
                Column {
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Título") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onBackground, unfocusedTextColor = MaterialTheme.colorScheme.onBackground, focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Prioridade:", color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        FilterChip(selected = taskPriority == 1, onClick = { taskPriority = 1 }, label = { Text("Baixa") })
                        FilterChip(selected = taskPriority == 2, onClick = { taskPriority = 2 }, label = { Text("Média") })
                        FilterChip(selected = taskPriority == 3, onClick = { taskPriority = 3 }, label = { Text("Alta") })
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (taskTitle.isNotBlank()) {
                        viewModel.addTask(taskTitle, priority = taskPriority)
                        taskTitle = ""; taskPriority = 1; showDialog = false
                    }
                }) { Text("Adicionar", color = MaterialTheme.colorScheme.primary) }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant) } },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // DIALOG: Apagar Tarefa
    if (taskToDelete != null) {
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            title = { Text("Apagar Tarefa", color = Color.Red) },
            text = { Text("Tens a certeza que queres apagar '${taskToDelete?.title}'?", color = MaterialTheme.colorScheme.onBackground) },
            confirmButton = {
                TextButton(onClick = {
                    taskToDelete?.let { viewModel.deleteTask(it) }
                    taskToDelete = null
                }) { Text("Apagar", color = Color.Red) }
            },
            dismissButton = { TextButton(onClick = { taskToDelete = null }) { Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant) } },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskItem(task: Task, onTaskClick: () -> Unit, onTaskLongClick: () -> Unit) {
    val priorityColor = when(task.priority) {
        3 -> Color.Red
        2 -> LumiereGold
        else -> Color.Green
    }

    Card(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onTaskClick, onLongClick = onTaskLongClick),
        colors = CardDefaults.cardColors(containerColor = LumiereDarkGray),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Indicador de Prioridade (uma barrinha vertical colorida)
            Box(modifier = Modifier.width(4.dp).height(24.dp).background(priorityColor, RoundedCornerShape(2.dp)))
            Spacer(modifier = Modifier.width(12.dp))
            
            Icon(
                imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.DateRange,
                contentDescription = null,
                tint = if (task.isCompleted) Color.Green else LumiereGold
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = task.title,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold, fontSize = 16.sp,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                if (task.description != null) {
                    Text(task.description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun SummaryCard(modifier: Modifier = Modifier, title: String, count: String, color: Color) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = LumiereDarkGray), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = color)
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(count, color = MaterialTheme.colorScheme.onBackground, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
    }
}
