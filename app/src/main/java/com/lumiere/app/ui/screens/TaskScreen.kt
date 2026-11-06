package com.lumiere.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var taskTitle by remember { mutableStateOf("") }
    var taskPriority by remember { mutableStateOf(1) }
    var taskDueDate by remember { mutableStateOf<Long?>(null) }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Cálculos temporais (Subfase 1)
    val todayEnd = getEndOfDay(System.currentTimeMillis())
    val tasksForToday = tasks.filter { !it.isCompleted && it.dueDate != null && it.dueDate in System.currentTimeMillis()..todayEnd }
    val tasksOverdue = tasks.filter { !it.isCompleted && it.dueDate != null && it.dueDate < System.currentTimeMillis() }
    val pendingTasks = tasks.count { !it.isCompleted }
    val completedTasks = tasks.count { it.isCompleted }

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
                    // Agora o cartão de hoje mostra apenas o que é para HOJE
                    SummaryCard(Modifier.weight(1f), "Tarefas de Hoje", tasksForToday.size.toString(), LumiereGold)
                    SummaryCard(Modifier.weight(1f), "Concluídas", completedTasks.toString(), Color.Green)
                }
            }

            // Alerta de Atrasadas (Agora usa o cálculo real)
            if (tasksOverdue.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF3E1A1A))) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color.Red)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Atenção: ${tasksOverdue.size} tarefas atrasadas", color = Color.White)
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
                        }
                    }
                }
            } else {
                items(tasks) { task ->
                    TaskItem(
                        task = task,
                        onTaskClick = { viewModel.toggleTaskCompletion(task) },
                        onTaskLongClick = { taskToDelete = task }
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
                            Text("Você tem $pendingTasks tarefas pendentes e ${tasksOverdue.size} atrasadas. Vamos organizar o seu dia?", color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
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
                    
                    // NOVO: Seletor de Data
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = LumiereGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = taskDueDate?.let { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it)) } ?: "Sem data",
                                color = LumiereGold
                            )
                        }
                        if (taskDueDate != null) {
                            IconButton(onClick = { taskDueDate = null }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpar data", tint = Color.Gray)
                            }
                        }
                    }

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
                        viewModel.addTask(taskTitle, priority = taskPriority, dueDate = taskDueDate)
                        taskTitle = ""; taskPriority = 1; taskDueDate = null; showDialog = false
                    }
                }) { Text("Adicionar", color = MaterialTheme.colorScheme.primary) }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant) } },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // DIALOG: DatePicker
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    taskDueDate = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK", color = LumiereGold) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = datePickerState)
        }
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

    val isOverdue = !task.isCompleted && task.dueDate != null && task.dueDate < System.currentTimeMillis()

    Card(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onTaskClick, onLongClick = onTaskLongClick),
        colors = CardDefaults.cardColors(containerColor = LumiereDarkGray),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.width(4.dp).height(24.dp).background(priorityColor, RoundedCornerShape(2.dp)))
            Spacer(modifier = Modifier.width(12.dp))
            
            Icon(
                imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.DateRange,
                contentDescription = null,
                tint = if (task.isCompleted) Color.Green else LumiereGold
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold, fontSize = 16.sp,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                // Mostrar data se existir
                if (task.dueDate != null) {
                    Text(
                        text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(task.dueDate)),
                        color = if (isOverdue) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                    )
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

// Função auxiliar para calcular o fim do dia
fun getEndOfDay(timestamp: Long): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    return calendar.timeInMillis
}
