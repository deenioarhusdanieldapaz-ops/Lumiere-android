package com.lumiere.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AppNotification(
    val title: String,
    val subtitle: String,
    val time: String,
    val type: String, // "TASK", "HABIT", "SUMMARY"
    val isRead: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    taskViewModel: TaskViewModel,
    habitViewModel: HabitViewModel,
    onBack: () -> Unit
) {
    val tasks by taskViewModel.tasks.collectAsStateWithLifecycle()
    val habits by habitViewModel.habits.collectAsStateWithLifecycle()
    val allCheckIns by habitViewModel.allCheckIns.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(1) } // 0=Tudo, 1=Não lidas, 2=Resumos
    val tabs = listOf("Tudo", "Não lidas", "Resumos")

    // Lógica de Dados Reais (Sem Mocks)
    val todayStart = java.util.Calendar.getInstance().apply { set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0); set(java.util.Calendar.SECOND, 0) }.timeInMillis
    val completedTasksToday = tasks.filter { it.isCompleted && it.completedAt != null && it.completedAt!! >= todayStart }
    val pendingHabitsToday = habits.filter { habit -> 
        !allCheckIns.any { it.habitId == habit.id && it.timestamp >= todayStart && it.type == "SUCCESS" } 
    }

    val notifications = mutableListOf<AppNotification>()
    val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())

    // Gerar notificações de Tarefas
    completedTasksToday.forEach { task ->
        notifications.add(AppNotification("Tarefa Concluída", task.title, timeFmt.format(Date(task.completedAt!!)), "TASK", false))
    }
    // Gerar notificações de Hábitos
    pendingHabitsToday.take(3).forEach { habit ->
        notifications.add(AppNotification("Lembrete de Hábito", habit.name, "08:00", "HABIT", false))
    }

    // Cálculos para o Resumo
    val pendingTasksCount = tasks.count { !it.isCompleted }
    val habitProgress = if (habits.isNotEmpty()) {
        val checkInsToday = allCheckIns.count { it.timestamp >= todayStart }
        (checkInsToday.toFloat() / habits.size.toFloat() * 100).toInt().coerceIn(0, 100)
    } else 0

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = LumiereSpacing.M, vertical = LumiereSpacing.S), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Notificações", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.displaySmall, fontFamily = FontFamily.Serif)
                    }
                    TextButton(onClick = { /* TODO: Limpar tudo */ }) {
                        Text("Limpar tudo", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]), color = MaterialTheme.colorScheme.primary, height = 2.dp)
                    },
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title, color = if (selectedTab == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) })
                    }
                }
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = LumiereSpacing.M),
            verticalArrangement = Arrangement.spacedBy(LumiereSpacing.M),
            contentPadding = PaddingValues(top = LumiereSpacing.M, bottom = LumiereSpacing.L)
        ) {
            // 1. Cartão de Resumo (Regra das 4h)
            if (selectedTab == 0 || selectedTab == 2) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Column(modifier = Modifier.padding(LumiereSpacing.M)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Resumo Lumière (4h)", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium, fontFamily = FontFamily.Serif)
                            }
                            Spacer(modifier = Modifier.height(LumiereSpacing.S))
                            Text("• $pendingTasksCount tarefas pendentes", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                            Text("• Progresso de hábitos a $habitProgress%", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                            Text("• Reunião às 15h (placeholder)", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // 2. Lista de Notificações
            item {
                Text("HOJE", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.sp, modifier = Modifier.padding(top = LumiereSpacing.S))
            }

            if (notifications.isEmpty()) {
                item {
                    LumiereEmptyState(
                        icon = Icons.Outlined.Notifications,
                        title = "Sem notificações",
                        description = "Silêncio total. Não tens alertas ou tarefas concluídas hoje."
                    )
                }
            } else {
                val filteredNotifs = when (selectedTab) {
                    1 -> notifications.filter { !it.isRead }
                    else -> notifications
                }
                items(filteredNotifs.size) { index ->
                    val notif = filteredNotifs[index]
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { /* TODO: Abrir detalhe */ },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(modifier = Modifier.padding(LumiereSpacing.M), verticalAlignment = Alignment.CenterVertically) {
                            // Ponto dourado de não lida
                            if (!notif.isRead) {
                                Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            // Ícone
                            Icon(
                                imageVector = when(notif.type) { "TASK" -> Icons.Outlined.CheckCircle; "HABIT" -> Icons.Outlined.Notifications; else -> Icons.Outlined.Info },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(LumiereSpacing.M))
                            // Textos
                            Column(modifier = Modifier.weight(1f)) {
                                Text(notif.title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge)
                                Text(notif.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            // Hora
                            Text(notif.time, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
