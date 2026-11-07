package com.lumiere.app.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.draw.clip

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
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
    val appContext = LocalContext.current
    var taskAttachmentUris by remember { mutableStateOf<List<String>>(emptyList()) }
    
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri>? ->
        if (uris != null) {
            taskAttachmentUris = uris.map { it.toString() }
        }
    }
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    
    // ESTADOS ELEVADOS (Para não perder dados ao fechar o painel)
    var showSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf("ALL") }
    var selectedProjectFilter by remember { mutableStateOf<Long?>(null) } // ALL, TODAY, OVERDUE, COMPLETED

    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var taskMenu by remember { mutableStateOf<Task?>(null) }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }
    var taskToCancel by remember { mutableStateOf<Task?>(null) }
    var cancelReason by remember { mutableStateOf("") }
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
    var selectedProjectId by remember { mutableStateOf<Long?>(null) }
    var blockedByTaskId by remember { mutableStateOf<Long?>(null) }
    var taskRecurrence by remember { mutableStateOf("NONE") }
    var expandedRecurrence by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showProjectsSheet by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }
    var showStatsSheet by remember { mutableStateOf(false) }
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
                actions = {
                    IconButton(onClick = { showSearchBar = !showSearchBar }) {
                        Icon(LumiereIcons.Search, contentDescription = "Pesquisar", tint = LumiereGold)
                    }
                    Box {
                        IconButton(onClick = { showFilterMenu = true }) {
                            Icon(LumiereIcons.Filter, contentDescription = "Filtros", tint = LumiereGold)
                        }
                        DropdownMenu(expanded = showFilterMenu, onDismissRequest = { showFilterMenu = false }, modifier = Modifier.background(LumiereDarkGray)) {
                            listOf("ALL" to "Todas", "TODAY" to "Hoje", "OVERDUE" to "Atrasadas", "COMPLETED" to "Concluídas").forEach { (filter, label) ->
                                DropdownMenuItem(
                                    text = { Text(label, color = if (activeFilter == filter) LumiereGold else MaterialTheme.colorScheme.onBackground) },
                                    onClick = { activeFilter = filter; showFilterMenu = false }
                                )
                            }
                        }
                    }
                    IconButton(onClick = { showProjectsSheet = true }) {
                        Icon(LumiereIcons.Folder, contentDescription = "Projetos", tint = LumiereGold)
                    }
                    IconButton(onClick = { showStatsSheet = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Estatísticas", tint = LumiereGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    taskToEdit = null
                    title = ""; category = ""; context = ""; taskDueDate = null; taskPriority = 1; taskRecurrence = "NONE"
                    urgency = 1; importance = 1; energy = 1; estimatedDuration = ""; timeReserved = ""; instructions = ""; links = ""; completionCriteria = ""; taskAttachmentUris = emptyList()
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
                val todayEnd = getEndOfDay(System.currentTimeMillis())
                val tasksForToday = tasks.filter { !it.isCompleted && it.dueDate != null && it.dueDate in System.currentTimeMillis()..todayEnd }
                val tasksOverdue = tasks.filter { !it.isCompleted && it.dueDate != null && it.dueDate < System.currentTimeMillis() }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryCard(Modifier.weight(1f), "Hoje", tasksForToday.size.toString(), LumiereGold)
                    SummaryCard(Modifier.weight(1f), "Concluídas", completedTasks.toString(), Color.Green)
                    SummaryCard(Modifier.weight(1f), "Atrasadas", tasksOverdue.size.toString(), Color.Red)
                }
            }

            val tasksOverdue = tasks.filter { !it.isCompleted && it.dueDate != null && it.dueDate < System.currentTimeMillis() }
            if (tasksOverdue.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF3E1A1A))) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color.Red)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Atenção: ${tasksOverdue.size} tarefas atrasadas", color = Color.White, modifier = Modifier.weight(1f))
                            TextButton(onClick = { activeFilter = "OVERDUE" }) {
                                Text("Ver detalhes >", color = LumiereGold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            
            if (showSearchBar) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Pesquisar tarefas...") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(LumiereIcons.Search, contentDescription = null, tint = LumiereGold) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LumiereGold)
                    )
                }
            }

            
            if (selectedProjectFilter != null) {
                item {
                    val projName = projects.find { it.id == selectedProjectFilter }?.name ?: "Projeto"
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                        InputChip(
                            selected = true,
                            onClick = { selectedProjectFilter = null },
                            label = { Text(projName, color = LumiereGold) },
                            trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Limpar filtro", tint = LumiereGold, modifier = Modifier.size(16.dp)) },
                            colors = InputChipDefaults.inputChipColors(selectedContainerColor = LumiereGold.copy(alpha = 0.2f))
                        )
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
                
                val filteredTasks = tasks.filter { task ->
                    val matchesSearch = task.title.contains(searchQuery, ignoreCase = true)
                    val matchesFilter = when(activeFilter) {
                        "TODAY" -> !task.isCompleted && task.dueDate != null && task.dueDate >= System.currentTimeMillis() && task.dueDate <= getEndOfDay(System.currentTimeMillis())
                        "OVERDUE" -> !task.isCompleted && task.dueDate != null && task.dueDate < System.currentTimeMillis()
                        "COMPLETED" -> task.isCompleted
                        else -> true
                    }
                    val matchesProject = selectedProjectFilter == null || task.projectId == selectedProjectFilter
                    matchesSearch && matchesFilter && matchesProject
                }
                items(filteredTasks) { task ->
                    TaskItem(
                        task = task,
                        onToggleComplete = { viewModel.toggleTaskCompletion(task) },
                        onOpenDetails = { taskDetails = task },
                        onMenuClick = { taskMenu = task },
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
                        taskAttachmentUris = taskMenu!!.attachmentUris?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
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

                if (taskMenu!!.status == 3) {
                    ListItem(
                        headlineContent = { Text("Retomar Tarefa", color = MaterialTheme.colorScheme.onSurface) },
                        leadingContent = { Icon(Icons.Default.Refresh, contentDescription = null, tint = LumiereGold) },
                        modifier = Modifier.clickable { viewModel.resumeTask(taskMenu!!); taskMenu = null },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                } else if (taskMenu!!.status != 2) {
                    ListItem(
                        headlineContent = { Text("Cancelar Tarefa", color = Color.Red) },
                        leadingContent = { Icon(Icons.Default.Close, contentDescription = null, tint = Color.Red) },
                        modifier = Modifier.clickable { taskToCancel = taskMenu; taskMenu = null },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }

                ListItem(
                    headlineContent = { Text("Eliminar", color = Color.Red) },
                    leadingContent = { Icon(LumiereIcons.Delete, contentDescription = null, tint = Color.Red) },
                    modifier = Modifier.clickable { taskToDelete = taskMenu; taskMenu = null },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
        }
    }

    // MODAL: Detalhes da Tarefa (Subtarefas e Histórico)
    if (taskDetails != null) {
        val subtasks by viewModel.getSubtasks(taskDetails!!.id).collectAsStateWithLifecycle(initialValue = emptyList())
        val history by viewModel.getHistory(taskDetails!!.id).collectAsStateWithLifecycle(initialValue = emptyList())
        var newSubtaskTitle by remember { mutableStateOf("") }

        ModalBottomSheet(onDismissRequest = { taskDetails = null }, containerColor = LumiereDarkGray) {
            Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()).fillMaxWidth()) {
                Text(taskDetails!!.title, color = LumiereGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                // Instruções, Links e Critérios
                if (!taskDetails!!.instructions.isNullOrBlank()) {
                    Text("Instruções:", color = LumiereGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(taskDetails!!.instructions!!, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (!taskDetails!!.links.isNullOrBlank()) {
                    Text("Links:", color = LumiereGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    val appContext = LocalContext.current
    var taskAttachmentUris by remember { mutableStateOf<List<String>>(emptyList()) }
                    Text(
                        text = taskDetails!!.links!!,
                        color = Color.Cyan,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(taskDetails!!.links))
                                appContext.startActivity(intent)
                            } catch (e: Exception) { }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (!taskDetails!!.attachmentUris.isNullOrBlank()) {
                    Text("Anexos:", color = LumiereGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    val uriList = taskDetails!!.attachmentUris!!.split(",")
                    uriList.forEachIndexed { index, uriString ->
                        Text(
                            text = "📎 Abrir Ficheiro ${index + 1}",
                            color = Color.Cyan,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(Uri.parse(uriString), "*/*")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    appContext.startActivity(intent)
                                } catch (e: Exception) { }
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (!taskDetails!!.completionCriteria.isNullOrBlank()) {
                    Text("Critério de Conclusão:", color = LumiereGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(taskDetails!!.completionCriteria!!, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color.Gray)

                // Secção de Subtarefas
                Text("Subtarefas", color = LumiereGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newSubtaskTitle,
                        onValueChange = { newSubtaskTitle = it },
                        label = { Text("Nova subtarefa...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { 
                        if (newSubtaskTitle.isNotBlank()) {
                            viewModel.addSubtask(taskDetails!!.id, newSubtaskTitle)
                            newSubtaskTitle = ""
                        }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar", tint = LumiereGold)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                if (subtasks.isEmpty()) {
                    Text("Sem subtarefas.", color = Color.Gray, fontSize = 12.sp)
                } else {
                    subtasks.forEach { subtask ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { viewModel.toggleSubtask(subtask) }) {
                            Checkbox(
                                checked = subtask.isCompleted,
                                onCheckedChange = { viewModel.toggleSubtask(subtask) },
                                colors = CheckboxDefaults.colors(checkedColor = LumiereGold)
                            )
                            Text(
                                text = subtask.title,
                                color = if (subtask.isCompleted) Color.Gray else MaterialTheme.colorScheme.onBackground,
                                textDecoration = if (subtask.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.deleteSubtask(subtask) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Apagar", tint = Color.Red, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color.Gray)

                // Secção de Histórico
                Text("Histórico", color = LumiereGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                if (history.isEmpty()) {
                    Text("Sem histórico.", color = Color.Gray, fontSize = 12.sp)
                } else {
                    history.forEach { h ->
                        Text(
                            text = "${SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(h.timestamp))} - ${h.action}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }


    // MODAL: Gestão de Projetos
    if (showProjectsSheet) {
        var newProjectName by remember { mutableStateOf("") }
        ModalBottomSheet(onDismissRequest = { showProjectsSheet = false }, containerColor = LumiereDarkGray) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Text("Gestão de Projetos", color = LumiereGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newProjectName,
                        onValueChange = { newProjectName = it },
                        label = { Text("Nome do novo projeto") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LumiereGold)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = {
                        if (newProjectName.isNotBlank()) {
                            viewModel.addProject(newProjectName)
                            newProjectName = ""
                        }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Criar Projeto", tint = LumiereGold)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Os teus projetos:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                
                if (projects.isEmpty()) {
                    Text("Ainda não tens projetos criados.", color = Color.Gray, fontSize = 12.sp)
                } else {
                    projects.forEach { proj ->
                        val projTasks = tasks.filter { it.projectId == proj.id }
                        val total = projTasks.size
                        val done = projTasks.count { it.isCompleted }
                        
                        ListItem(
                            headlineContent = { Text(proj.name, color = MaterialTheme.colorScheme.onSurface) },
                            supportingContent = { 
                                Text(
                                    text = if(total == 0) "Sem tarefas" else "$done de $total tarefas concluídas",
                                    color = if(done == total && total > 0) Color.Green else Color.Gray,
                                    fontSize = 12.sp
                                )
                            },
                            leadingContent = { Icon(LumiereIcons.Folder, contentDescription = null, tint = LumiereGold) },
                            modifier = Modifier.clickable {
                                selectedProjectFilter = proj.id
                                showProjectsSheet = false
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }


    // MODAL: Estatísticas
    if (showStatsSheet) {
        val totalTasks = tasks.size
        val completed = tasks.count { it.isCompleted }
        val overdue = tasks.count { !it.isCompleted && it.dueDate != null && it.dueDate < System.currentTimeMillis() }
        val totalMinutes = tasks.sumOf { it.actualDurationMinutes }
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        ModalBottomSheet(onDismissRequest = { showStatsSheet = false }, containerColor = LumiereDarkGray) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Text("Estatísticas do M01", color = LumiereGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total", color = Color.Gray, fontSize = 12.sp)
                        Text("$totalTasks", color = MaterialTheme.colorScheme.onBackground, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Concluídas", color = Color.Green, fontSize = 12.sp)
                        Text("$completed", color = Color.Green, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Atrasadas", color = Color.Red, fontSize = 12.sp)
                        Text("$overdue", color = Color.Red, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Divider(color = Color.Gray)
                Spacer(modifier = Modifier.height(24.dp))
                
                Text("Tempo Total Investido", color = LumiereGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("${hours}h ${minutes}m", color = MaterialTheme.colorScheme.onBackground, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // MODAL: Criar / Editar Tarefa (Elevado)
    if (showSheet) {
        ModalBottomSheet(onDismissRequest = { showSheet = false }, containerColor = LumiereDarkGray) {
            Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()).fillMaxWidth()) {
                Text(if (taskToEdit == null) "Nova Tarefa" else "Editar Tarefa", color = LumiereGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                var expandedBlocker by remember { mutableStateOf(false) }
                val availableBlockers = tasks.filter { it.id != taskToEdit?.id && !it.isCompleted }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { expandedBlocker = true }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = if(blockedByTaskId != null) Color.Red else LumiereGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tasks.find { it.id == blockedByTaskId }?.title ?: "Sem bloqueio",
                            color = if(blockedByTaskId != null) Color.Red else LumiereGold
                        )
                    }
                    DropdownMenu(expanded = expandedBlocker, onDismissRequest = { expandedBlocker = false }, modifier = Modifier.background(LumiereDarkGray)) {
                        DropdownMenuItem(text = { Text("Sem bloqueio", color = MaterialTheme.colorScheme.onBackground) }, onClick = { blockedByTaskId = null; expandedBlocker = false })
                        availableBlockers.forEach { t ->
                            DropdownMenuItem(text = { Text(t.title, color = MaterialTheme.colorScheme.onBackground) }, onClick = { blockedByTaskId = t.id; expandedBlocker = false })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Categoria") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = context, onValueChange = { context = it }, label = { Text("Contexto (ex: Casa)") }, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Seletor de Projetos
                var expandedProject by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { expandedProject = true }, modifier = Modifier.fillMaxWidth()) {
                        Icon(LumiereIcons.Folder, contentDescription = null, tint = LumiereGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = projects.find { it.id == selectedProjectId }?.name ?: "Sem Projeto",
                            color = LumiereGold
                        )
                    }
                    DropdownMenu(expanded = expandedProject, onDismissRequest = { expandedProject = false }, modifier = Modifier.background(LumiereDarkGray)) {
                        DropdownMenuItem(text = { Text("Sem Projeto", color = MaterialTheme.colorScheme.onBackground) }, onClick = { selectedProjectId = null; expandedProject = false })
                        projects.forEach { proj ->
                            DropdownMenuItem(text = { Text(proj.name, color = MaterialTheme.colorScheme.onBackground) }, onClick = { selectedProjectId = proj.id; expandedProject = false })
                        }
                    }
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
                
                OutlinedButton(
                    onClick = { 
                        if (taskAttachmentUris.size < 10) {
                            filePickerLauncher.launch("*/*") 
                        }
                    }, 
                    modifier = Modifier.fillMaxWidth(),
                    enabled = taskAttachmentUris.size < 10
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = if (taskAttachmentUris.size < 10) LumiereGold else Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (taskAttachmentUris.isEmpty()) "Anexar Ficheiros (Máx 10)" else "${taskAttachmentUris.size} Ficheiros Anexados",
                        color = if (taskAttachmentUris.size < 10) LumiereGold else Color.Gray
                    )
                }
                if (taskAttachmentUris.isNotEmpty()) {
                    Text("Ficheiros selecionados:", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                    taskAttachmentUris.forEachIndexed { index, uri ->
                        Text("${index + 1}. $uri", color = Color.Gray, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
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
                            projectId = selectedProjectId,
                            blockedByTaskId = blockedByTaskId,
                            priority = taskPriority,
                            recurrenceRule = taskRecurrence,
                            estimatedDurationMinutes = estimatedDuration.toIntOrNull(),
                            timeReservedMinutes = timeReserved.toIntOrNull(),
                            instructions = instructions.ifBlank { null },
                            links = links.ifBlank { null },
                            completionCriteria = completionCriteria.ifBlank { null },
                            attachmentUris = if (taskAttachmentUris.isEmpty()) null else taskAttachmentUris.joinToString(","),
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


    // DIALOG: Motivo de Cancelamento
    if (taskToCancel != null) {
        AlertDialog(
            onDismissRequest = { taskToCancel = null; cancelReason = "" },
            title = { Text("Cancelar Tarefa", color = Color.Red) },
            text = {
                Column {
                    Text("Porque estás a cancelar '${taskToCancel?.title}'?", color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        label = { Text("Motivo (ex: Falta de tempo)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (cancelReason.isNotBlank()) {
                        viewModel.cancelTask(taskToCancel!!, cancelReason)
                        taskToCancel = null
                        cancelReason = ""
                    }
                }) { Text("Confirmar", color = Color.Red) }
            },
            dismissButton = { TextButton(onClick = { taskToCancel = null; cancelReason = "" }) { Text("Voltar", color = LumiereGold) } },
            containerColor = MaterialTheme.colorScheme.surface
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskItem(task: Task, onToggleComplete: () -> Unit, onOpenDetails: () -> Unit, onMenuClick: () -> Unit, onTimerClick: () -> Unit) {
    val isOverdue = !task.isCompleted && task.dueDate != null && task.dueDate < System.currentTimeMillis()
    val isCompleted = task.isCompleted
    
    val priorityColor = when(task.priority) {
        3 -> Color(0xFFE53935) // Vermelho
        2 -> LumiereGold        // Dourado
        else -> Color(0xFF43A047) // Verde
    }
    val priorityText = when(task.priority) {
        3 -> "Alta"
        2 -> "Média"
        else -> "Baixa"
    }
    val priorityIcon = if (task.priority == 3) Icons.Default.Warning else Icons.Default.Info

    // Lógica do Cronómetro
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
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onOpenDetails, onLongClick = onMenuClick),
        colors = CardDefaults.cardColors(containerColor = if (isCompleted) LumiereDarkGray.copy(alpha = 0.5f) else LumiereDarkGray),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Ícone Principal
            Box(
                modifier = Modifier.size(48.dp).background(priorityColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.DateRange,
                    contentDescription = null,
                    tint = priorityColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))

            // Conteúdo
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontWeight = FontWeight.Bold, fontSize = 16.sp,
                    color = if (isCompleted) Color.Gray else Color.White,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                
                // Badges (Categoria e Prioridade)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!task.category.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(Color.Black.copy(alpha=0.3f), RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                             Icon(LumiereIcons.Folder, contentDescription = null, tint = LumiereGold, modifier = Modifier.size(12.dp))
                             Spacer(modifier = Modifier.width(4.dp))
                             Text(task.category!!, color = LumiereGold, fontSize = 11.sp)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(priorityColor.copy(alpha=0.1f), RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Icon(priorityIcon, contentDescription = null, tint = priorityColor, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(priorityText, color = priorityColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Data/Hora
                if (task.dueDate != null) {
                     val cal = java.util.Calendar.getInstance().apply { timeInMillis = task.dueDate }
                     val todayCal = java.util.Calendar.getInstance()
                     val isToday = cal.get(java.util.Calendar.YEAR) == todayCal.get(java.util.Calendar.YEAR) && cal.get(java.util.Calendar.DAY_OF_YEAR) == todayCal.get(java.util.Calendar.DAY_OF_YEAR)
                     val dateStr = if (isToday) "Hoje • ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(task.dueDate))}" else SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault()).format(Date(task.dueDate))
                     
                     Row(verticalAlignment = Alignment.CenterVertically) {
                          Icon(Icons.Default.DateRange, contentDescription = null, tint = if(isOverdue) Color.Red else Color.Gray, modifier = Modifier.size(12.dp))
                          Spacer(modifier = Modifier.width(4.dp))
                          Text(dateStr, color = if(isOverdue) Color.Red else Color.Gray, fontSize = 11.sp)
                     }
                }
                
                // Progresso (se houver estimativa)
                if (task.estimatedDurationMinutes != null && task.estimatedDurationMinutes > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val progress = (task.actualDurationMinutes.toFloat() / task.estimatedDurationMinutes.toFloat()).coerceIn(0f, 1f)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                         LinearProgressIndicator(
                             progress = progress, 
                             modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)), 
                             color = priorityColor, 
                             trackColor = Color.DarkGray
                         )
                         Spacer(modifier = Modifier.width(8.dp))
                         Text("${(progress * 100).toInt()}%", color = Color.White, fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))
            
            // Ações
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onTimerClick,
                    modifier = Modifier.size(36.dp).border(1.dp, LumiereGold.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (task.timerStartedAt != null) Icons.Default.Close else Icons.Default.PlayArrow,
                        contentDescription = "Timer",
                        tint = if (task.timerStartedAt != null) Color.Red else LumiereGold
                    )
                }
                IconButton(onClick = onMenuClick, modifier = Modifier.size(24.dp)) {
                     Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.Gray)
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
