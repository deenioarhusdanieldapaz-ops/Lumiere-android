package com.lumiere.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumiere.app.ui.components.LumiereEmptyState
import com.lumiere.app.ui.theme.LumiereSpacing
import com.lumiere.app.ui.viewmodel.HabitViewModel
import com.lumiere.app.ui.viewmodel.TaskViewModel

data class AreaItem(val icon: ImageVector, val title: String, val subtitle: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    taskViewModel: TaskViewModel,
    habitViewModel: HabitViewModel,
    isDark: Boolean,
    onThemeToggle: () -> Unit
) {
    val tasks by taskViewModel.tasks.collectAsStateWithLifecycle()
    val habits by habitViewModel.habits.collectAsStateWithLifecycle()

    val todayTasks = tasks.filter { !it.isCompleted && it.dueDate != null && it.dueDate >= System.currentTimeMillis() }
    
    val areas = listOf(
        AreaItem(Icons.Outlined.CheckCircle, "Tarefas", "${todayTasks.size} ativos"),
        AreaItem(Icons.Outlined.Star, "Hábitos", "${habits.size} em progresso"),
        AreaItem(Icons.Outlined.DateRange, "Calendário", "Sem eventos"),
        AreaItem(Icons.Outlined.Favorite, "Saúde", "Registo diário"),
        AreaItem(Icons.Outlined.PlayArrow, "Finanças", "Consultar"),
        AreaItem(Icons.Outlined.Info, "Ver Tudo", "Explorar módulos")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(LumiereSpacing.S))
                        Text("LUMIÈRE", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineMedium)
                    }
                },
                actions = {
                    IconButton(onClick = onThemeToggle) {
                        Icon(if (isDark) Icons.Filled.DarkMode else Icons.Filled.LightMode, contentDescription = "Tema", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Outlined.Notifications, contentDescription = "Notificações", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = LumiereSpacing.M),
            verticalArrangement = Arrangement.spacedBy(LumiereSpacing.L)
        ) {
            // 1. HERO CARD (Índice de Evolução)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(modifier = Modifier.padding(LumiereSpacing.L), verticalAlignment = Alignment.CenterVertically) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                            CircularProgressIndicator(
                                progress = 0.2f,
                                modifier = Modifier.size(80.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                strokeWidth = 4.dp,
                                strokeCap = StrokeCap.Round
                            )
                            Text("20%", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge)
                        }
                        Spacer(modifier = Modifier.width(LumiereSpacing.L))
                        Column {
                            Text("ÍNDICE DE EVOLUÇÃO", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(LumiereSpacing.S))
                            Text("Continue a construir o seu império.", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }

            // 2. INSIGHT LUMIÈRE (O painel de inteligência de volta)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(modifier = Modifier.padding(LumiereSpacing.M), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Outlined.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(LumiereSpacing.M))
                        Column {
                            Text("Insight Lumière", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                            Spacer(modifier = Modifier.height(LumiereSpacing.S))
                            Text(
                                "Tudo em ordem! Aproveita para adiantar algum hábito ou planear a semana.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // 3. FOCO DE HOJE (Tarefas e Hábitos)
            item {
                Text("Foco de Hoje", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(LumiereSpacing.M))
                
                if (todayTasks.isEmpty() && habits.isEmpty()) {
                    LumiereEmptyState(
                        icon = Icons.Outlined.CheckCircle,
                        title = "Tudo Limpo",
                        description = "Não tens tarefas nem hábitos pendentes para hoje."
                    )
                } else {
                    // Tarefas (Máx 3)
                    if (todayTasks.isNotEmpty()) {
                        Text("Tarefas", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                        todayTasks.take(3).forEach { task ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = LumiereSpacing.S),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(modifier = Modifier.padding(LumiereSpacing.M), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(LumiereSpacing.S))
                                    Text(task.title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                    
                    // Hábitos (Máx 3)
                    if (habits.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(LumiereSpacing.M))
                        Text("Hábitos", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                        habits.take(3).forEach { habit ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = LumiereSpacing.S),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(modifier = Modifier.padding(LumiereSpacing.M), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(LumiereSpacing.S))
                                    Text(habit.name, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }

            // 4. GRELHA DE ÁREAS (Agora em baixo)
            item {
                Text("Áreas", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(LumiereSpacing.M))
            }
            item {
                // Substituindo o LazyVerticalGrid por Rows para evitar bugs de scroll no Compose
                Column(verticalArrangement = Arrangement.spacedBy(LumiereSpacing.M)) {
                    areas.chunked(2).forEach { rowItems ->
                        Row(horizontalArrangement = Arrangement.spacedBy(LumiereSpacing.M)) {
                            rowItems.forEach { area ->
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Column(modifier = Modifier.padding(LumiereSpacing.M)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Icon(area.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Icon(Icons.Outlined.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Spacer(modifier = Modifier.height(LumiereSpacing.M))
                                        Text(area.title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
                                        Text(area.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(LumiereSpacing.L))
            }
        }
    }
}
