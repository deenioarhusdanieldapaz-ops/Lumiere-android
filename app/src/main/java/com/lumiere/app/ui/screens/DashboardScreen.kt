package com.lumiere.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    
    // Dados mock para replicar a imagem
    val mockTasks = listOf("Estudar Engenharia", "Rever o orçamento", "Planear a semana")
    val mockHabits = listOf("Beber água", "Exercício físico", "Ler 15 min")

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Efeito de Gradiente Radial (Brilho no topo direito)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF3A2A10).copy(alpha = 0.8f), Color.Transparent),
                        center = Offset(1000f, 0f),
                        radius = 800f
                    )
                )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = LumiereSpacing.M),
            verticalArrangement = Arrangement.spacedBy(LumiereSpacing.L),
            contentPadding = PaddingValues(top = 40.dp, bottom = 120.dp)
        ) {
            // 1. Header (Logo + Notificações + Avatar)
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
                            Text("LUMIÈRE", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineMedium, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif)
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
                        Box(
                            modifier = Modifier.size(40.dp).border(1.dp, MaterialTheme.colorScheme.primary, CircleShape).padding(2.dp).clip(CircleShape).background(Color.Gray)
                        )
                    }
                }
            }

            // 2. Saudação
            item {
                Column {
                    Text(
                        "Bom dia, Daniel!",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.displayMedium,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
                    )
                    Spacer(modifier = Modifier.height(LumiereSpacing.S))
                    Text(
                        "Que hoje seja um dia produtivo e extraordinário.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // 3. Hero Card (Índice de Evolução)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(LumiereSpacing.L),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Anel de Progresso
                        Box(contentAlignment = Alignment.Center) {
                            Canvas(modifier = Modifier.size(100.dp)) {
                                drawArc(
                                    color = Color(0xFFD4AF37).copy(alpha = 0.2f),
                                    startAngle = 0f, sweepAngle = 360f, useCenter = false,
                                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                                )
                                drawArc(
                                    color = Color(0xFFD4AF37),
                                    startAngle = -90f, sweepAngle = 72f, useCenter = false,
                                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("20%", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                Text("do mês", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Spacer(modifier = Modifier.width(LumiereSpacing.L))

                        // Textos e Botão
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Índice de Evolução", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Você está no caminho certo!", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(LumiereSpacing.M))
                            OutlinedButton(
                                onClick = { },
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(50),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("Ver detalhes →", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        // Divisória e Stats
                        Box(modifier = Modifier.width(1.dp).height(80.dp).background(Color.DarkGray))
                        Spacer(modifier = Modifier.width(LumiereSpacing.M))
                        Column(verticalArrangement = Arrangement.spacedBy(LumiereSpacing.M)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Sequência", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                                    Text("7 dias", color = Color.White, style = MaterialTheme.typography.titleSmall)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Meta do Mês", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                                    Text("25 dias", color = Color.White, style = MaterialTheme.typography.titleSmall)
                                }
                            }
                        }
                    }
                }
            }

            // 4. Cartões Tarefas e Hábitos (Lado a Lado)
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(LumiereSpacing.M)) {
                    // Cartão Tarefas
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(0.5.dp, Color(0xFF333333))
                    ) {
                        Column(modifier = Modifier.padding(LumiereSpacing.M)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Tarefas de Hoje", color = Color.White, style = MaterialTheme.typography.titleSmall)
                            }
                            Spacer(modifier = Modifier.height(LumiereSpacing.M))
                            mockTasks.forEachIndexed { index, title ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                                    Icon(if (index == 0) Icons.Filled.CheckCircle else Icons.Outlined.Circle, contentDescription = null, tint = if (index == 0) MaterialTheme.colorScheme.primary else Color.Gray, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(title, color = if (index == 0) Color.Gray else Color.White, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, textDecoration = if (index == 0) androidx.compose.ui.text.style.TextDecoration.LineThrough else null)
                                }
                            }
                            Spacer(modifier = Modifier.height(LumiereSpacing.M))
                            Text("Ver todas →", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    // Cartão Hábitos
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(0.5.dp, Color(0xFF333333))
                    ) {
                        Column(modifier = Modifier.padding(LumiereSpacing.M)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Spa, contentDescription = null, tint = MaterialTheme.colorScheme.primary) // Lotus icon approx
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Hábitos de Hoje", color = Color.White, style = MaterialTheme.typography.titleSmall)
                            }
                            Spacer(modifier = Modifier.height(LumiereSpacing.M))
                            mockHabits.forEachIndexed { index, title ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = if (index < 2) MaterialTheme.colorScheme.primary else Color.Gray, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(title, color = Color.White, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                            Spacer(modifier = Modifier.height(LumiereSpacing.M))
                            Text("Ver todos →", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // 5. Banner Inteligência Lumière
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Row(modifier = Modifier.padding(LumiereSpacing.M), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(LumiereSpacing.M))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Inteligência Lumière", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif)
                            Text("Excelente progresso! Mantém o foco nas tuas prioridades.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                        }
                        OutlinedButton(onClick = { }, border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(50)) {
                            Text("Ver dica", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // 6. Grelha As Tuas Áreas
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.DarkGray))
                    Text("As Tuas Áreas", color = Color.White, style = MaterialTheme.typography.titleMedium, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, modifier = Modifier.padding(horizontal = LumiereSpacing.M))
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.DarkGray))
                }
            }
            item {
                val areas = listOf(
                    "Vida Pessoal" to "Equilíbrio e bem-estar",
                    "Desenvolvimento" to "Aprender e evoluir",
                    "Produtividade" to "Foco e resultados",
                    "Gestão Financeira" to "Planear e prosperar",
                    "Saúde" to "Corpo e mente",
                    "Planeamento" to "Organizar o futuro"
                )
                val icons = listOf(Icons.Outlined.Person, Icons.Outlined.School, Icons.Outlined.TrendingUp, Icons.Outlined.MonetizationOn, Icons.Outlined.FavoriteBorder, Icons.Outlined.CalendarToday)
                
                Column(verticalArrangement = Arrangement.spacedBy(LumiereSpacing.S)) {
                    areas.chunked(2).forEachIndexed { rowIndex, rowAreas ->
                        Row(horizontalArrangement = Arrangement.spacedBy(LumiereSpacing.S)) {
                            rowAreas.forEachIndexed { colIndex, area ->
                                val index = rowIndex * 2 + colIndex
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(0.5.dp, Color(0xFF333333))
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(icons[index], contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(area.first, color = Color.White, style = MaterialTheme.typography.labelLarge)
                                            Text(area.second, color = Color.Gray, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
