package com.lumiere.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumiere.app.data.local.entity.Task
import com.lumiere.app.ui.theme.LumiereDarkGray
import com.lumiere.app.ui.theme.LumiereGold
import com.lumiere.app.ui.theme.LumiereIcons
import com.lumiere.app.ui.viewmodel.TaskViewModel
import java.util.Calendar

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TaskScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    
    // ESTADOS ELEVADOS (Para não perder dados ao fechar o painel)
    var showSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf("ALL") } // ALL, TODAY, OVERDUE, COMPLETED

    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var taskMenu by remember { mutableStateOf<Task?>(null) }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }
    var taskDetails by remember { mutableStateOf<Task?>(null) }

    // Dados do formulário
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var urgency by remember { mutableIntStateOf(1) }
    var importance by remember { mutableIntStateOf(1) }
    var energy by remember { mutableIntStateOf(1) }
    var context by remember { mutableStateOf("") }
    var estimatedDuration by remember { mutableStateOf("") }
    var timeReserved by remember { mutableStateOf("") }
    var taskDueDate by remember { mutableStateOf<Long?>(null) }
    var taskPriority by remember { mutableIntStateOf(1) }
    var taskRecurrence by remember { mutableStateOf("NONE") }
    var expandedRecurrence by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var instructions by remember { mutableStateOf("") }
    var links by remember { mutableStateOf("") }
    var completionCriteria by remember { mutableStateOf("") }

    
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
                onClick = { 
                    taskToEdit = null
                    title = ""; category = ""; context = ""; taskDueDate = null; taskPriority = 1; taskRecurrence = "NONE"
                    urgency = 1; importance = 1; energy = 1; estimatedDuration = ""; timeReserved = ""; instructions = ""; links = ""; completionCriteria = ""
                    showSheet = true 
                },
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
                    SummaryCard(Modifier.weight(1f), "Pendentes", pendingTasks.toString(), LumiereGold)
                    SummaryCard(Modifier.weight(1f), "Concluídas", completedTasks.toString(), Color.Green)
                }
            }

            val tasksOverdue = tasks.filter { !it.isCompleted && it.dueDate != null && it.dueDate < System.currentTimeMillis() }
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

            
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Pesquisar tarefas...") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = LumiereGold) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LumiereGold)
                )
            }
            
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    FilterChip(selected = activeFilter == "ALL", onClick = { activeFilter = "ALL" }, label = { Text("Todas") })
                    FilterChip(selected = activeFilter == "TODAY", onClick = { activeFilter = "TODAY" }, label = { Text("Hoje") })
                    FilterChip(selected = activeFilter == "OVERDUE", onClick = { activeFilter = "OVERDUE" }, label = { Text("Atrasadas") })
                    FilterChip(selected = activeFilter == "COMPLETED", onClick = { activeFilter = "COMPLETED" }, label = { Text("Concluídas") })
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
                
                val filteredTasks = tasks.filter { task ->
                    val matchesSearch = task.title.contains(searchQuery, ignoreCase = true)
                    val matchesFilter = when(activeFilter) {
                        "TODAY" -> !task.isCompleted && task.dueDate != null && task.dueDate >= System.currentTimeMillis() && task.dueDate <= getEndOfDay(System.currentTimeMillis())
                        "OVERDUE" -> !task.isCompleted && task.dueDate != null && task.dueDate < System.currentTimeMillis()
                        "COMPLETED" -> task.isCompleted
                        else -> true
                    }
                    matchesSearch && matchesFilter
                }
                items(filteredTasks) { task ->
                    TaskItem(
                        task = task,
                        onTaskClick = { viewModel.toggleTaskCompletion(task) },
                        onTaskLongClick = { taskMenu = task },
                        onInfoClick = { taskDetails = task },
                        onTimerClick = { viewModel.toggleTimer(task) }
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

    // MODAL: Menu de Contexto (Long Press)
    if (taskMenu != null) {
        ModalBottomSheet(onDismissRequest = { taskMenu = null }, containerColor = LumiereDarkGray) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Text(taskMenu!!.title, color = LumiereGold, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 16.dp))
                ListItem(
                    headlineContent = { Text("Editar Tarefa", color = MaterialTheme.colorScheme.onSurface) },
                    leadingContent = { Icon(LumiereIcons.Edit, contentDescription = null, tint = LumiereGold) },
                    modifier = Modifier.clickable { 
                    taskToEdit = taskMenu
                    title = taskMenu!!.title
                    category = taskMenu!!.category ?: ""
                    urgency = taskMenu!!.urgency
                    importance = taskMenu!!.importance
                    energy = taskMenu!!.energy
                    context = taskMenu!!.context ?: ""
                    estimatedDuration = taskMenu!!.estimatedDurationMinutes?.toString() ?: ""
                    timeReserved = taskMenu!!.timeReservedMinutes?.toString() ?: ""
                    instructions = taskMenu!!.instructions ?: ""
                    links = taskMenu!!.links ?: ""
                    completionCriteria = taskMenu!!.completionCriteria ?: ""
                    showSheet = true; taskMenu = null 
                },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
                ListItem(
                    headlineContent = { Text("Duplicar", color = MaterialTheme.colorScheme.onSurface) },
                    leadingContent = { Icon(LumiereIcons.Copy, contentDescription = null, tint = LumiereGold) },
                    modifier = Modifier.clickable { viewModel.duplicateTask(taskMenu!!); taskMenu = null },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
                ListItem(
                    headlineContent = { Text("Arquivar", color = MaterialTheme.colorScheme.onSurface) },
                    leadingContent = { Icon(LumiereIcons.Archive, contentDescription = null, tint = LumiereGold) },
                    modifier = Modifier.clickable { viewModel.archiveTask(taskMenu!!); taskMenu = null },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
                ListItem(
                    headlineContent = { Text("Eliminar", color = Color.Red) },
                    leadingContent = { Icon(LumiereIcons.Delete, contentDescription = null, tint = Color.Red) },
                    modifier = Modifier.clickable { taskToDelete = taskMenu; taskMenu = null },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
        }
    }

    // MODAL: Detalhes da Tarefa (Para o Ícone ⓘ)
    if (taskDetails != null) {
        AlertDialog(
            onDismissRequest = { taskDetails = null },
            title = { Text("Detalhes da Tarefa", color = LumiereGold) },
            text = {
                Column {
                    Text(taskDetails!!.title, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    if (!taskDetails!!.instructions.isNullOrBlank()) {
                        Text("Instruções:", color = LumiereGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(taskDetails!!.instructions!!, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (!taskDetails!!.links.isNullOrBlank()) {
                        Text("Links:", color = LumiereGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        val context = LocalContext.current
                        Text(
                            text = taskDetails!!.links!!,
                            color = Color.Cyan,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(taskDetails!!.links))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Ignorar se o link for inválido
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (!taskDetails!!.completionCriteria.isNullOrBlank()) {
                        Text("Critério de Conclusão:", color = LumiereGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(taskDetails!!.completionCriteria!!, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (taskDetails!!.instructions.isNullOrBlank() && taskDetails!!.links.isNullOrBlank() && taskDetails!!.completionCriteria.isNullOrBlank()) {
                        Text("Nenhum detalhe extra guardado.", color = Color.Gray)
                    }
                }
            },
            confirmButton = { TextButton(onClick = { taskDetails = null }) { Text("Fechar", color = LumiereGold) } },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // MODAL: Criar / Editar Tarefa (Elevado)
    if (showSheet) {
        ModalBottomSheet(onDismissRequest = { showSheet = false }, containerColor = LumiereDarkGray) {
            Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()).fillMaxWidth()) {
                Text(if (taskToEdit == null) "Nova Tarefa" else "Editar Tarefa", color = LumiereGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Categoria") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = context, onValueChange = { context = it }, label = { Text("Contexto (ex: Casa)") }, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = estimatedDuration, onValueChange = { estimatedDuration = it }, label = { Text("Estimativa (min)") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = timeReserved, onValueChange = { timeReserved = it }, label = { Text("Reservado (min)") }, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))

                                // DATA E HORA
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = LumiereGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(taskDueDate?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it)) } ?: "Sem Data", color = LumiereGold, fontSize = 12.sp)
                    }
                    OutlinedButton(onClick = { showTimePicker = true }, modifier = Modifier.weight(1f), enabled = taskDueDate != null) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = LumiereGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(taskDueDate?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it)) } ?: "Hora", color = if (taskDueDate != null) LumiereGold else Color.Gray, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // RECORRÊNCIA
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { expandedRecurrence = true }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = LumiereGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when(taskRecurrence) {
                                "DAILY" -> "Repetir: Diariamente"
                                "WEEKLY" -> "Repetir: Semanalmente"
                                "MONTHLY" -> "Repetir: Mensalmente"
                                "YEARLY" -> "Repetir: Anualmente"
                                else -> "Repetir: Nunca"
                            },
                            color = LumiereGold
                        )
                    }
                    DropdownMenu(expanded = expandedRecurrence, onDismissRequest = { expandedRecurrence = false }, modifier = Modifier.background(LumiereDarkGray)) {
                        listOf("NONE" to "Nunca", "DAILY" to "Diariamente", "WEEKLY" to "Semanalmente", "MONTHLY" to "Mensalmente", "YEARLY" to "Anualmente").forEach { (rule, label) ->
                            DropdownMenuItem(text = { Text(label, color = MaterialTheme.colorScheme.onBackground) }, onClick = { taskRecurrence = rule; expandedRecurrence = false })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // PRIORIDADE
                Text("Prioridade:", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    FilterChip(selected = taskPriority == 1, onClick = { taskPriority = 1 }, label = { Text("Baixa") })
                    FilterChip(selected = taskPriority == 2, onClick = { taskPriority = 2 }, label = { Text("Média") })
                    FilterChip(selected = taskPriority == 3, onClick = { taskPriority = 3 }, label = { Text("Alta") })
                }
                Spacer(modifier = Modifier.height(8.dp))

Text("Urgência: ${if(urgency==1) "Baixa" else if(urgency==2) "Média" else "Alta"}", color = MaterialTheme.colorScheme.onSurface)
                Slider(value = urgency.toFloat(), onValueChange = { urgency = it.toInt() }, valueRange = 1f..3f, steps = 1)
                
                Text("Importância: ${if(importance==1) "Baixa" else if(importance==2) "Média" else "Alta"}", color = MaterialTheme.colorScheme.onSurface)
                Slider(value = importance.toFloat(), onValueChange = { importance = it.toInt() }, valueRange = 1f..3f, steps = 1)

                Text("Energia Necessária: ${if(energy==1) "Baixa" else if(energy==2) "Média" else "Alta"}", color = MaterialTheme.colorScheme.onSurface)
                Slider(value = energy.toFloat(), onValueChange = { energy = it.toInt() }, valueRange = 1f..3f, steps = 1)
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Anexos e Contexto", color = LumiereGold, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = instructions, onValueChange = { instructions = it }, label = { Text("Instruções / Notas") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = links, onValueChange = { links = it }, label = { Text("Links (ex: https://...)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = completionCriteria, onValueChange = { completionCriteria = it }, label = { Text("Critério de Conclusão") }, modifier = Modifier.fillMaxWidth())
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        val task = (taskToEdit ?: Task(title = "")).copy(
                            title = title,
                            category = category.ifBlank { null },
                            urgency = urgency,
                            importance = importance,
                            energy = energy,
                            context = context.ifBlank { null },
                            dueDate = taskDueDate,
                            priority = taskPriority,
                            recurrenceRule = taskRecurrence,
                            estimatedDurationMinutes = estimatedDuration.toIntOrNull(),
                            timeReservedMinutes = timeReserved.toIntOrNull(),
                            instructions = instructions.ifBlank { null },
                            links = links.ifBlank { null },
                            completionCriteria = completionCriteria.ifBlank { null }
                        )
                        if (taskToEdit == null) viewModel.addTask(task) else viewModel.updateTask(task)
                        showSheet = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = LumiereGold)
                ) { Text("Guardar", color = Color.Black) }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // DIALOG: DatePicker
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = taskDueDate ?: System.currentTimeMillis())
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val currentCalendar = Calendar.getInstance()
                        if (taskDueDate != null) currentCalendar.timeInMillis = taskDueDate!!
                        val newCalendar = Calendar.getInstance()
                        newCalendar.timeInMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                        newCalendar.set(Calendar.HOUR_OF_DAY, currentCalendar.get(Calendar.HOUR_OF_DAY))
                        newCalendar.set(Calendar.MINUTE, currentCalendar.get(Calendar.MINUTE))
                        taskDueDate = newCalendar.timeInMillis
                        showDatePicker = false
                    }) { Text("OK", color = LumiereGold) }
                },
                dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } }
            ) { DatePicker(state = datePickerState) }
        }

        // DIALOG: TimePicker
        if (showTimePicker) {
            val timePickerState = rememberTimePickerState()
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                title = { Text("Escolher Hora", color = LumiereGold) },
                text = { TimePicker(state = timePickerState) },
                confirmButton = {
                    TextButton(onClick = {
                        val cal = Calendar.getInstance()
                        if (taskDueDate != null) cal.timeInMillis = taskDueDate!!
                        cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        cal.set(Calendar.MINUTE, timePickerState.minute)
                        taskDueDate = cal.timeInMillis
                        showTimePicker = false
                    }) { Text("OK", color = LumiereGold) }
                },
                dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") } },
                containerColor = LumiereDarkGray
            )
        }
    }

    // DIALOG: Confirmar Eliminação
    if (taskToDelete != null) {
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            title = { Text("Apagar Tarefa", color = Color.Red) },
            text = { Text("Tens a certeza que queres apagar '${taskToDelete?.title}'?", color = MaterialTheme.colorScheme.onBackground) },
            confirmButton = { TextButton(onClick = { viewModel.deleteTask(taskToDelete!!); taskToDelete = null }) { Text("Apagar", color = Color.Red) } },
            dismissButton = { TextButton(onClick = { taskToDelete = null }) { Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant) } },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskItem(task: Task, onTaskClick: () -> Unit, onTaskLongClick: () -> Unit, onInfoClick: () -> Unit, onTimerClick: () -> Unit) {
    val isOverdue = !task.isCompleted && task.dueDate != null && task.dueDate < System.currentTimeMillis()
    
    val priorityColor = when(task.priority) {
        3 -> Color.Red
        2 -> LumiereGold
        else -> Color.Green
    }
    val priorityText = when(task.priority) {
        3 -> "Alta"
        2 -> "Média"
        else -> "Baixa"
    }

    val cardColor = when {
        isOverdue -> Color(0xFF3E1A1A)
        task.isCompleted -> LumiereDarkGray.copy(alpha = 0.5f)
        else -> LumiereDarkGray
    }
    
    val borderStroke = if (isOverdue) androidx.compose.foundation.BorderStroke(1.dp, Color.Red) else null

    // Lógica do Cronómetro em Tempo Real
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(task.timerStartedAt) {
        if (task.timerStartedAt != null) {
            while (true) {
                currentTime = System.currentTimeMillis()
                kotlinx.coroutines.delay(1000L)
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onTaskClick, onLongClick = onTaskLongClick),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp),
        border = borderStroke
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Barrinha lateral
            Box(modifier = Modifier.width(4.dp).height(56.dp).background(priorityColor, RoundedCornerShape(2.dp)))
            Spacer(modifier = Modifier.width(12.dp))
            
            // Ícone de Estado
            Icon(
                imageVector = if (task.isCompleted) Icons.Default.CheckCircle else if (isOverdue) Icons.Default.Warning else Icons.Default.DateRange,
                contentDescription = null, 
                tint = if (task.isCompleted) Color.Green else if (isOverdue) Color.Red else LumiereGold
            )
            Spacer(modifier = Modifier.width(12.dp))
            
            // Conteúdo Principal
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold, fontSize = 16.sp,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Badges (Categoria, Contexto, Prioridade)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (!task.category.isNullOrBlank()) {
                        Text(task.category!!, color = LumiereGold, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    if (!task.context.isNullOrBlank()) {
                        Text("@${task.context}", color = Color.Gray, fontSize = 11.sp, maxLines = 1)
                    }
                    Surface(color = priorityColor.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                        Text(priorityText, color = priorityColor, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), maxLines = 1)
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))

                // Data e Hora / Cronómetro
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (task.dueDate != null) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = if (isOverdue) Color.Red else Color.Gray, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(task.dueDate)),
                            color = if (isOverdue) Color.Red else Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                    
                    if (task.timerStartedAt != null) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Green, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        // Cálculo do tempo real em segundos
                        val elapsedSeconds = ((currentTime - task.timerStartedAt) / 1000).coerceAtLeast(0)
                        val hours = elapsedSeconds / 3600
                        val minutes = (elapsedSeconds % 3600) / 60
                        val seconds = elapsedSeconds % 60
                        val timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                        
                        Text(timeString, color = Color.Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    } else if (task.actualDurationMinutes > 0) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("${task.actualDurationMinutes} min", color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }

            // Botões à direita (Cronómetro e Info)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onTimerClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (task.timerStartedAt != null) Icons.Default.Close else Icons.Default.PlayArrow,
                        contentDescription = "Cronómetro",
                        tint = if (task.timerStartedAt != null) Color.Red else LumiereGold
                    )
                }
                if (!task.instructions.isNullOrBlank() || !task.links.isNullOrBlank() || !task.completionCriteria.isNullOrBlank()) {
                    IconButton(onClick = onInfoClick, modifier = Modifier.size(32.dp)) { 
                        Icon(Icons.Default.Info, contentDescription = "Ver Detalhes", tint = LumiereGold) 
                    }
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

fun getEndOfDay(timestamp: Long): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    return calendar.timeInMillis
}
