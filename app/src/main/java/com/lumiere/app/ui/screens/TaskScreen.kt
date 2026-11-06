package com.lumiere.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumiere.app.data.local.entity.Task
import com.lumiere.app.ui.theme.LumiereDarkGray
import com.lumiere.app.ui.theme.LumiereGold
import com.lumiere.app.ui.theme.LumiereIcons
import com.lumiere.app.ui.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TaskScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    
    var showSheet by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var taskMenu by remember { mutableStateOf<Task?>(null) }
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
                onClick = { taskToEdit = null; showSheet = true },
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
                        onTaskLongClick = { taskMenu = task }
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // MODAL: Menu de Contexto
    if (taskMenu != null) {
        ModalBottomSheet(onDismissRequest = { taskMenu = null }, containerColor = LumiereDarkGray) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Text(taskMenu!!.title, color = LumiereGold, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 16.dp))
                ListItem(
                    headlineContent = { Text("Editar Tarefa", color = MaterialTheme.colorScheme.onSurface) },
                    leadingContent = { Icon(LumiereIcons.Edit, contentDescription = null, tint = LumiereGold) },
                    modifier = Modifier.clickable { taskToEdit = taskMenu; showSheet = true; taskMenu = null },
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

    // MODAL: Criar / Editar Tarefa (Com Ponto 1 e 2)
    if (showSheet) {
        TaskEditSheet(
            taskToEdit = taskToEdit,
            onDismiss = { showSheet = false },
            onSave = { task ->
                if (taskToEdit == null) viewModel.addTask(task) else viewModel.updateTask(task)
                showSheet = false
            }
        )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditSheet(taskToEdit: Task?, onDismiss: () -> Unit, onSave: (Task) -> Unit) {
    var title by remember { mutableStateOf(taskToEdit?.title ?: "") }
    var category by remember { mutableStateOf(taskToEdit?.category ?: "") }
    var urgency by remember { mutableIntStateOf(taskToEdit?.urgency ?: 1) }
    var importance by remember { mutableIntStateOf(taskToEdit?.importance ?: 1) }
    var energy by remember { mutableIntStateOf(taskToEdit?.energy ?: 1) }
    var context by remember { mutableStateOf(taskToEdit?.context ?: "") }
    var estimatedDuration by remember { mutableStateOf(taskToEdit?.estimatedDurationMinutes?.toString() ?: "") }
    var timeReserved by remember { mutableStateOf(taskToEdit?.timeReservedMinutes?.toString() ?: "") }
    var instructions by remember { mutableStateOf(taskToEdit?.instructions ?: "") }
    var links by remember { mutableStateOf(taskToEdit?.links ?: "") }
    var completionCriteria by remember { mutableStateOf(taskToEdit?.completionCriteria ?: "") }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = LumiereDarkGray) {
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
                        estimatedDurationMinutes = estimatedDuration.toIntOrNull(),
                        timeReservedMinutes = timeReserved.toIntOrNull(),
                        instructions = instructions.ifBlank { null },
                        links = links.ifBlank { null },
                        completionCriteria = completionCriteria.ifBlank { null }
                    )
                    onSave(task)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = LumiereGold)
            ) { Text("Guardar", color = Color.Black) }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskItem(task: Task, onTaskClick: () -> Unit, onTaskLongClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onTaskClick, onLongClick = onTaskLongClick),
        colors = CardDefaults.cardColors(containerColor = LumiereDarkGray),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.DateRange,
                contentDescription = null, tint = if (task.isCompleted) Color.Green else LumiereGold
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold, fontSize = 16.sp,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                if (!task.context.isNullOrBlank()) {
                    Text("Contexto: ${task.context}", color = Color.Gray, fontSize = 12.sp)
                }
                if (!task.instructions.isNullOrBlank()) {
                    Icon(Icons.Default.Info, contentDescription = "Tem instruções", tint = LumiereGold, modifier = Modifier.size(14.dp))
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
