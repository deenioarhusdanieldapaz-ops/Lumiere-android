package com.lumiere.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import com.lumiere.app.ui.theme.LumiereSpacing
import com.lumiere.app.ui.viewmodel.HabitViewModel
import com.lumiere.app.ui.viewmodel.TaskViewModel

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
    
    val mockTasks = listOf("Estudar Engenharia", "Rever o orçamento", "Planear a semana")
    val mockHabits = listOf("Beber água", "Exercício físico", "Ler 15 min")

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (isDark) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF3A2A10).copy(alpha = 0.6f), Color.Transparent),
                            center = Offset(1000f, 0f),
                            radius = 800f
                        )
                    )
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = LumiereSpacing.M),
            verticalArrangement = Arrangement.spacedBy(LumiereSpacing.L),
            contentPadding = PaddingValues(top = 40.dp, bottom = 200.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(LumiereSpacing.S))
                        Column {
                            Text("LUMIÈRE", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineMedium, fontFamily = FontFamily.Serif)
                            Text("Life Manager", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(start = 2.dp))
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Icon(Icons.Outlined.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                            Box(modifier = Modifier.size(16.dp).background(Color.Red, CircleShape), contentAlignment = Alignment.Center) {
                                Text("3", color = Color.White, fontSize = 10.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(LumiereSpacing.M))
                        Box(modifier = Modifier.size(40.dp).border(1.dp, MaterialTheme.colorScheme.primary, CircleShape).padding(2.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant))
                    }
                }
            }

            item {
                Column {
                    Text("Bom dia, Daniel!", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.displayMedium, fontFamily = FontFamily.Serif)
                    Spacer(modifier = Modifier.height(LumiereSpacing.S))
                    Text("Que hoje seja um dia produtivo e extraordinário.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(modifier = Modifier.padding(LumiereSpacing.M), verticalAlignment = Alignment.CenterVertically) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(90.dp)) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawArc(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), startAngle = 0f, sweepAngle = 360f, useCenter = false, style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round))
                                drawArc(color = MaterialTheme.colorScheme.primary, startAngle = -90f, sweepAngle = 72f, useCenter = false, style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("20%", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text("do mês", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Spacer(modifier = Modifier.width(LumiereSpacing.M))

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Índice de Evolução", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall, fontFamily = FontFamily.Serif, maxLines = 1)
                            Text("Você está no caminho!", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                            Spacer(modifier = Modifier.height(LumiereSpacing.S))
                            TextButton(onClick = { }, contentPadding = PaddingValues(0.dp), modifier = Modifier.height(24.dp)) {
                                Text("Ver detalhes →", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Box(modifier = Modifier.width(1.dp).height(70.dp).background(MaterialTheme.colorScheme.outline))
                        Spacer(modifier = Modifier.width(LumiereSpacing.S))

                        Column(verticalArrangement = Arrangement.spacedBy(LumiereSpacing.S)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Column {
                                    Text("Sequência", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                                    Text("7 dias", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Column {
                                    Text("Meta Mês", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                                    Text("25 dias", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(LumiereSpacing.M)) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(LumiereSpacing.M)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Tarefas de Hoje", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                            }
                            Spacer(modifier = Modifier.height(LumiereSpacing.S))
                            mockTasks.forEachIndexed { index, title ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                                    Icon(if (index == 0) Icons.Filled.CheckCircle else Icons.Outlined.Circle, contentDescription = null, tint = if (index == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(title, color = if (index == 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, textDecoration = if (index == 0) androidx.compose.ui.text.style.TextDecoration.LineThrough else null)
                                }
                            }
                            Spacer(modifier = Modifier.height(LumiereSpacing.S))
                            Text("Ver todas →", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(LumiereSpacing.M)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Spa, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Hábitos de Hoje", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                            }
                            Spacer(modifier = Modifier.height(LumiereSpacing.S))
                            mockHabits.forEachIndexed { index, title ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = if (index < 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                            Spacer(modifier = Modifier.height(LumiereSpacing.S))
                            Text("Ver todos →", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Row(modifier = Modifier.padding(LumiereSpacing.M), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(LumiereSpacing.M))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Inteligência Lumière", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall, fontFamily = FontFamily.Serif)
                            Text("Excelente progresso! Mantém o foco.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                        }
                        OutlinedButton(onClick = { }, border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(50), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                            Text("Ver dica", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outline))
                    Text("As Tuas Áreas", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontFamily = FontFamily.Serif, modifier = Modifier.padding(horizontal = LumiereSpacing.M))
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
                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(icons[index], contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(area.first, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(area.second, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
