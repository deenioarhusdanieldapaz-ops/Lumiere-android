package com.lumiere.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumiere.app.ui.components.LumiereEmptyState
import com.lumiere.app.ui.theme.LumiereSpacing
import com.lumiere.app.ui.viewmodel.HabitViewModel
import com.lumiere.app.ui.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    taskViewModel: TaskViewModel,
    habitViewModel: HabitViewModel,
    isDark: Boolean,
    onThemeToggle: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToHabits: () -> Unit
) {
    val tasks by taskViewModel.tasks.collectAsStateWithLifecycle()
    val habits by habitViewModel.habits.collectAsStateWithLifecycle()
    
    val recentTasks = tasks.filter { !it.isCompleted }.take(3)
    val recentHabits = habits.take(3)

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (isDark) {
            Box(
                modifier = Modifier.fillMaxWidth().height(300.dp).background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF3A2A10).copy(alpha = 0.6f), Color.Transparent),
                        center = Offset(1000f, 0f), radius = 800f
                    )
                )
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = LumiereSpacing.M),
            verticalArrangement = Arrangement.spacedBy(LumiereSpacing.M),
            contentPadding = PaddingValues(top = 32.dp, bottom = LumiereSpacing.M)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(LumiereSpacing.S))
                        Column {
                            Text("LUMIÈRE", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineMedium, fontFamily = FontFamily.Serif)
                            Text("Life Manager", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Icon(Icons.Outlined.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                            Box(modifier = Modifier.size(14.dp).background(Color.Red, CircleShape), contentAlignment = Alignment.Center) {
                                Text("3", color = Color.White, fontSize = 8.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(LumiereSpacing.M))
                        Box(modifier = Modifier.size(36.dp).border(1.dp, MaterialTheme.colorScheme.primary, CircleShape).padding(2.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant))
                    }
                }
            }

            item {
                Column {
                    Text("Bom dia, Proprietário!", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.displaySmall, fontFamily = FontFamily.Serif)
                    Text("Que hoje seja um dia produtivo e extraordinário.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ) {
                    Row(modifier = Modifier.padding(LumiereSpacing.M), verticalAlignment = Alignment.CenterVertically) {
                        val totalTasks = tasks.size
                        val completedTasks = tasks.count { it.isCompleted }
                        val progress = if (totalTasks > 0) completedTasks.toFloat() / totalTasks.toFloat() else 0f
                        val progressPercentage = (progress * 100).toInt()
                        val streak = habitViewModel.calculateGlobalStreak()

                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(70.dp)) {
                            val ringColor = MaterialTheme.colorScheme.primary
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawArc(color = ringColor.copy(alpha = 0.2f), startAngle = 0f, sweepAngle = 360f, useCenter = false, style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round))
                                drawArc(color = ringColor, startAngle = -90f, sweepAngle = (progress * 360f), useCenter = false, style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round))
                            }
                            Text("$progressPercentage%", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(LumiereSpacing.M))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Índice de Evolução", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall, fontFamily = FontFamily.Serif, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("Você está no caminho!", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            TextButton(onClick = { }, contentPadding = PaddingValues(0.dp), modifier = Modifier.height(20.dp)) {
                                Text("Ver detalhes →", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Box(modifier = Modifier.width(1.dp).height(50.dp).background(MaterialTheme.colorScheme.outline))
                        Spacer(modifier = Modifier.width(LumiereSpacing.S))
                        Column(modifier = Modifier.weight(0.6f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("$streak dias", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelMedium)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("$completedTasks/$totalTasks", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }

            // 4. Tarefas e Hábitos (com Modo Vazio)
            item {
                if (recentTasks.isEmpty() && recentHabits.isEmpty()) {
                    LumiereEmptyState(
                        icon = Icons.Outlined.WbSunny,
                        title = "Dia Limpo",
                        description = "Não tens tarefas nem hábitos pendentes. Aproveita para descansar ou planear a semana."
                    )
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(LumiereSpacing.M)) {
                        Card(
                            modifier = Modifier.weight(1f).clickable { onNavigateToTasks() },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(modifier = Modifier.padding(LumiereSpacing.S)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Tarefas", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                                }
                                Spacer(modifier = Modifier.height(LumiereSpacing.S))
                                if (recentTasks.isEmpty()) {
                                    Text("Nenhuma", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                } else {
                                    recentTasks.forEach { task ->
                                        Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 2.dp)) {
                                            Icon(Icons.Outlined.Circle, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp).padding(top = 2.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Column {
                                                Text(task.title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall)
                                                val dateStr = task.dueDate?.let { java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault()).format(java.util.Date(it)) } ?: "Sem data"
                                                Text(dateStr, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1f).clickable { onNavigateToHabits() },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(modifier = Modifier.padding(LumiereSpacing.S)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Spa, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Hábitos", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                                }
                                Spacer(modifier = Modifier.height(LumiereSpacing.S))
                                if (recentHabits.isEmpty()) {
                                    Text("Nenhum", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                } else {
                                    recentHabits.forEach { habit ->
                                        Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 2.dp)) {
                                            Icon(Icons.Outlined.Circle, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp).padding(top = 2.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Column {
                                                Text(habit.name, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall)
                                                val target = habit.targetValue ?: 0
                                                Text("Meta: $target", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Banner Inteligência
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Row(modifier = Modifier.padding(LumiereSpacing.S), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(LumiereSpacing.M))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Inteligência Lumière", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall, fontFamily = FontFamily.Serif)
                            val overdue = tasks.count { !it.isCompleted && it.dueDate != null && it.dueDate < System.currentTimeMillis() }
                            val pending = tasks.count { !it.isCompleted }
                            val bannerText = if (overdue > 0) {
                                "Tens $overdue tarefa(s) atrasada(s) e $pending por fazer. Sugiro focares-te primeiro nas atrasadas."
                            } else if (pending > 0) {
                                "Tens $pending tarefa(s) pendente(s). Vamos começar a riscá-las da lista?"
                            } else {
                                "Tudo em ordem! Todas as tarefas estão concluídas. Aproveita para relaxar."
                            }
                            Text(bannerText, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                        OutlinedButton(onClick = { }, border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(50), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp), modifier = Modifier.height(28.dp)) {
                            Text("Ver dica", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // 6. Grelha Áreas
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outline))
                    Text("As Tuas Áreas", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall, fontFamily = FontFamily.Serif, modifier = Modifier.padding(horizontal = LumiereSpacing.M))
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outline))
                }
            }
            item {
                val areas = listOf("Vida Pessoal" to "Equilíbrio", "Desenvolvimento" to "Evoluir", "Produtividade" to "Foco", "Gestão Financeira" to "Prosperar", "Saúde" to "Bem-estar", "Planeamento" to "Futuro")
                val icons = listOf(Icons.Outlined.Person, Icons.Outlined.School, Icons.Outlined.TrendingUp, Icons.Outlined.MonetizationOn, Icons.Outlined.FavoriteBorder, Icons.Outlined.CalendarToday)
                
                Column(verticalArrangement = Arrangement.spacedBy(LumiereSpacing.S)) {
                    areas.chunked(2).forEachIndexed { rowIndex, rowAreas ->
                        Row(horizontalArrangement = Arrangement.spacedBy(LumiereSpacing.S)) {
                            rowAreas.forEachIndexed { colIndex, area ->
                                val index = rowIndex * 2 + colIndex
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
                                ) {
                                    Row(modifier = Modifier.padding(LumiereSpacing.S), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(icons[index], contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(LumiereSpacing.S))
                                        Column {
                                            Text(area.first, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(area.second, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 7. Ações Rápidas (Única, no fim do scroll)
            item {
                Card(
                    shape = RoundedCornerShape(50),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().padding(top = LumiereSpacing.M)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { }, contentPadding = PaddingValues(0.dp), modifier = Modifier.height(24.dp)) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Nova Tarefa", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelSmall)
                        }
                        Box(modifier = Modifier.width(1.dp).height(16.dp).background(MaterialTheme.colorScheme.outline))
                        TextButton(onClick = { }, contentPadding = PaddingValues(0.dp), modifier = Modifier.height(24.dp)) {
                            Icon(Icons.Outlined.Spa, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Novo Hábito", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelSmall)
                        }
                        Box(modifier = Modifier.width(1.dp).height(16.dp).background(MaterialTheme.colorScheme.outline))
                        TextButton(onClick = { }, contentPadding = PaddingValues(0.dp), modifier = Modifier.height(24.dp)) {
                            Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Novo Evento", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
