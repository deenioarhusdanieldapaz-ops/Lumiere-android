package com.lumiere.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
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
fun TaskScreen(viewModel: TaskViewModel, onOpenDrawer: () -> Unit = {}) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showQuickAdd by remember { mutableStateOf(false) }

    val tasks = uiState.tasks // Usar a lista base e agrupar localmente para as secções
    val now = System.currentTimeMillis()
    val endOfDay = getEndOfDay(now)

    val overdueTasks = tasks.filter { !it.isCompleted && it.dueDate != null && it.dueDate < now }
    val todayTasks = tasks.filter { !it.isCompleted && it.dueDate != null && it.dueDate >= now && it.dueDate <= endOfDay }
    val upcomingTasks = tasks.filter { !it.isCompleted && (it.dueDate == null || it.dueDate > endOfDay) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showQuickAdd = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 60.dp) // Acima da bottom bar
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Tarefa", modifier = Modifier.size(32.dp))
            }
        },
        bottomBar = {
            // Barra de navegação visual baseada na imagem
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
                NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Início") })
                NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Default.CheckCircle, null) }, label = { Text("Tarefas") }, colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, selectedTextColor = MaterialTheme.colorScheme.primary))
                NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.Favorite, null) }, label = { Text("Hábitos") })
                NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.MoreHoriz, null) }, label = { Text("Mais") })
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = LumiereSpacing.M),
            verticalArrangement = Arrangement.spacedBy(LumiereSpacing.M)
        ) {
            // Header (Título e Subtítulo)
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(top = LumiereSpacing.M), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onOpenDrawer) { Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.primary) }
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text("Hoje", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.primary)
                            Text("Vamos fazer hoje ainda melhor.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Row {
                        Icon(Icons.Default.Search, contentDescription = "Pesquisar", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Box {
                            Icon(Icons.Default.Notifications, contentDescription = "Notificações", tint = MaterialTheme.colorScheme.primary)
                            Box(modifier = Modifier.align(Alignment.TopEnd).background(Color.Red, CircleShape).size(8.dp))
                        }
                    }
                }
            }

            // Summary Cards
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryBox(Modifier.weight(1f), "3", "Hoje", Icons.Default.CalendarToday, MaterialTheme.colorScheme.primary)
                    SummaryBox(Modifier.weight(1f), overdueTasks.size.toString(), "Atrasada", Icons.Default.Warning, MaterialTheme.colorScheme.error)
                    SummaryBox(Modifier.weight(1f), "68%", "Foco", Icons.Default.TrackChanges, MaterialTheme.colorScheme.primary)
                }
            }

            // Secção Atrasadas
            if (overdueTasks.isNotEmpty()) {
                item { SectionHeader("Atrasadas", overdueTasks.size, Icons.Default.Schedule, MaterialTheme.colorScheme.error) }
                items(overdueTasks) { task ->
                    TaskCardItem(task, MaterialTheme.colorScheme.error, "há 2h") // Simulação de tempo
                }
            }

            // Secção Hoje
            if (todayTasks.isNotEmpty()) {
                item { SectionHeader("Hoje", todayTasks.size, Icons.Default.CalendarMonth, MaterialTheme.colorScheme.primary) }
                items(todayTasks) { task ->
                    TaskCardItem(task, MaterialTheme.colorScheme.primary)
                }
            }

            // Secção Próximas
            if (upcomingTasks.isNotEmpty()) {
                item { SectionHeader("Próximas", upcomingTasks.size, Icons.Default.Schedule, Color(0xFF3B82F6)) }
                items(upcomingTasks) { task ->
                    TaskCardItem(task, Color(0xFF3B82F6))
                }
            }

            // Cartão da Inteligência Lumière
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(LumiereRadius.M))
                        .background(Color(0xFF1A1500))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(LumiereRadius.M))
                        .padding(LumiereSpacing.M)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Inteligência Lumière", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                            Text("Tens 2 tarefas de 30m. Queres agrupá-las esta tarde?", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                        }
                        OutlinedButton(onClick = {}, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)) {
                            Text("Ver", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
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

@Composable
private fun SectionHeader(title: String, count: Int, icon: ImageVector, color: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(top = LumiereSpacing.S), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.width(8.dp))
        Box(modifier = Modifier.background(color, CircleShape).padding(horizontal = 8.dp, vertical = 2.dp)) {
            Text(count.toString(), color = Color.White, style = MaterialTheme.typography.labelSmall)
        }
        Spacer(modifier = Modifier.weight(1f))
        Text("Ver todas >", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun TaskCardItem(task: Task, borderColor: Color, timeLabel: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(LumiereRadius.M))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(LumiereRadius.M))
    ) {
        Box(modifier = Modifier.width(4.dp).height(if (task.actualDurationMinutes > 0) 72.dp else 60.dp).background(borderColor))
        Row(modifier = Modifier.padding(LumiereSpacing.M).weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(24.dp).border(2.dp, MaterialTheme.colorScheme.outline, CircleShape))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(task.category ?: "Pessoal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (timeLabel != null) {
                        Text(" • $timeLabel", style = MaterialTheme.typography.bodySmall, color = borderColor)
                    }
                }
                if (task.actualDurationMinutes > 0 && task.estimatedDurationMinutes != null) {
                    val p = (task.actualDurationMinutes.toFloat() / task.estimatedDurationMinutes).coerceIn(0f, 1f)
                    LinearProgressIndicator(progress = p, modifier = Modifier.fillMaxWidth().padding(top = 4.dp).height(4.dp).clip(RoundedCornerShape(2.dp)), color = borderColor, trackColor = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
            if (task.actualDurationMinutes == 0 && task.dueDate == null) {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            } else if (task.actualDurationMinutes == 0 && task.dueDate != null) {
                // Simulação de botão Iniciar Timer
                OutlinedButton(onClick = {}, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)) {
                    Text("Iniciar timer", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
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
