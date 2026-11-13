package com.lumiere.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumiere.app.ui.theme.LumiereSpacing
import com.lumiere.app.ui.viewmodel.HabitViewModel
import com.lumiere.app.ui.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyReviewScreen(
    taskViewModel: TaskViewModel,
    habitViewModel: HabitViewModel,
    isDark: Boolean
) {
    val tasks by taskViewModel.tasks.collectAsStateWithLifecycle()
    val habits by habitViewModel.habits.collectAsStateWithLifecycle()
    val allCheckIns by habitViewModel.allCheckIns.collectAsStateWithLifecycle()

    // 1. Cálculo das Datas da Semana Atual (Segunda a Domingo)
    val calendar = Calendar.getInstance()
    calendar.firstDayOfWeek = Calendar.MONDAY
    val currentDay = calendar.get(Calendar.DAY_OF_WEEK)
    val daysToSubtract = if (currentDay == Calendar.SUNDAY) 6 else currentDay - Calendar.MONDAY
    calendar.add(Calendar.DAY_OF_YEAR, -daysToSubtract)
    val startOfWeek = calendar.timeInMillis
    calendar.add(Calendar.DAY_OF_YEAR, 6)
    val endOfWeek = calendar.timeInMillis

    val dateFormat = SimpleDateFormat("d 'de' MMMM", Locale("pt", "PT"))
    val weekRangeText = "Semana de ${dateFormat.format(Date(startOfWeek))} a ${dateFormat.format(Date(endOfWeek))}"

    // 2. Cálculo Tarefas Reais
    val tasksThisWeek = tasks.filter { it.dueDate != null && it.dueDate in startOfWeek..endOfWeek }
    val completedTasks = tasksThisWeek.count { it.isCompleted }
    val totalTasks = tasksThisWeek.size
    val taskProgress = if (totalTasks > 0) completedTasks.toFloat() / totalTasks.toFloat() else 0f

    // 3. Cálculo Hábitos Reais
    val checkInsThisWeek = allCheckIns.filter { it.timestamp in startOfWeek..endOfWeek }
    val activeHabits = habits.size
    val expectedCheckIns = activeHabits * 7 // Assumindo hábitos diários
    val actualCheckIns = checkInsThisWeek.size
    val habitProgress = if (expectedCheckIns > 0) (actualCheckIns.toFloat() / expectedCheckIns.toFloat()).coerceIn(0f, 1f) else 0f

    // 4. Cálculo Tarefas Pendentes para Próxima Semana (ou atrasadas)
    val pendingTasks = tasks.filter { !it.isCompleted }.take(3)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (isDark) listOf(Color(0xFF1A150B), MaterialTheme.colorScheme.background) else listOf(Color(0xFFFFF8E1), MaterialTheme.colorScheme.background)
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = LumiereSpacing.M),
            verticalArrangement = Arrangement.spacedBy(LumiereSpacing.M)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // TopBar
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(8.dp))
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

            // Título e Data
            Column(modifier = Modifier.padding(top = LumiereSpacing.M)) {
                Text("Revisão Semanal", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.displayMedium, fontFamily = FontFamily.Serif)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(weekRangeText, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                    Surface(shape = RoundedCornerShape(50), color = Color.Transparent, border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.BarChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Em progresso", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // Secção de Resumo (3 Anéis)
            Row(horizontalArrangement = Arrangement.spacedBy(LumiereSpacing.S)) {
                ReviewRingCard(title = "Tarefas", progress = taskProgress, subtitle = "$completedTasks de $totalTasks", icon = Icons.Outlined.CheckCircle, modifier = Modifier.weight(1f))
                ReviewRingCard(title = "Hábitos", progress = habitProgress, subtitle = "$actualCheckIns de $expectedCheckIns", icon = Icons.Outlined.Spa, modifier = Modifier.weight(1f))
                // Metas (M03 não existe, fica 0%)
                ReviewRingCard(title = "Metas", progress = 0f, subtitle = "Sem dados", icon = Icons.Outlined.EmojiEvents, modifier = Modifier.weight(1f))
            }

            // Secção Reflexão
            Row(verticalAlignment = Alignment.CenterVertically) {
                Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), thickness = 0.5.dp)
                Text("R E F L E X Ã O", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium, letterSpacing = 2.sp, modifier = Modifier.padding(horizontal = LumiereSpacing.S))
                Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), thickness = 0.5.dp)
            }

            var text1 by remember { mutableStateOf("") }
            var text2 by remember { mutableStateOf("") }
            var text3 by remember { mutableStateOf("") }

            ReflectionCard(icon = Icons.Outlined.Star, title = "O que correu bem?", subtitle = "Partilhe os seus sucessos desta semana...", value = text1, onValueChange = { if (it.length <= 500) text1 = it })
            ReflectionCard(icon = Icons.Outlined.Warning, title = "O que foi difícil?", subtitle = "Partilhe os desafios que encontrou...", value = text2, onValueChange = { if (it.length <= 500) text2 = it })
            ReflectionCard(icon = Icons.Outlined.TrendingUp, title = "O que quero melhorar na próxima semana?", subtitle = "Defina uma área para evoluir...", value = text3, onValueChange = { if (it.length <= 500) text3 = it })

            // Secção Preparação
            Row(verticalAlignment = Alignment.CenterVertically) {
                Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), thickness = 0.5.dp)
                Text("P R E P A R A Ç Ã O", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium, letterSpacing = 2.sp, modifier = Modifier.padding(horizontal = LumiereSpacing.S))
                Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), thickness = 0.5.dp)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(LumiereSpacing.M)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.List, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Tarefas pendentes (${pendingTasks.size})", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall)
                                Text("Organize as suas próximas tarefas", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        OutlinedButton(onClick = { }, border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(50), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp), modifier = Modifier.height(28.dp)) {
                            Text("Ver todas", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    if (pendingTasks.isEmpty()) {
                        Text("Nenhuma tarefa pendente.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = LumiereSpacing.M))
                    } else {
                        pendingTasks.forEach { task ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = LumiereSpacing.M)) {
                                Icon(Icons.Outlined.DragIndicator, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(task.title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Botão Guardar Revisão
            var showSnackbar by remember { mutableStateOf(false) }
            Button(
                onClick = { showSnackbar = true },
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = LumiereSpacing.M),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar Revisão", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            if (showSnackbar) {
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(2000)
                    showSnackbar = false
                }
                Text("Revisão guardada com sucesso!", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun ReviewRingCard(title: String, progress: Float, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(LumiereSpacing.M), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(70.dp)) {
                val ringColor = MaterialTheme.colorScheme.primary
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(color = ringColor.copy(alpha = 0.2f), startAngle = 0f, sweepAngle = 360f, useCenter = false, style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round))
                    drawArc(color = ringColor, startAngle = -90f, sweepAngle = (progress * 360f), useCenter = false, style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round))
                }
                Text("${(progress * 100).toInt()}%", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(LumiereSpacing.S))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelSmall)
            }
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun ReflectionCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, value: String, onValueChange: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(LumiereSpacing.M)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall)
                    Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(modifier = Modifier.height(LumiereSpacing.S))
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth().height(80.dp),
                placeholder = { Text("Escreva aqui...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
            Text("${value.length}/500", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.End).padding(top = 4.dp))
        }
    }
}
