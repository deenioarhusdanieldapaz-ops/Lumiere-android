package com.lumiere.app.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumiere.app.ui.components.LumiereEmptyState
import com.lumiere.app.ui.theme.LumiereSpacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

data class ApprovalItem(
    val id: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val description: String,
    val consequence: String,
    val priority: String? = null,
    val time: String,
    val isApproved: Boolean? = null // null = pendente, true = aprovado, false = rejeitado
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApprovalCenterScreen(onBack: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0=Pendentes, 1=Histórico
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Estado local (Mock controlado para validar UI)
    val allItems = remember {
        mutableStateListOf(
            ApprovalItem("1", Icons.Outlined.CalendarToday, "Reagendar Tarefa", "A Lumière sugere mover a tarefa devido a um conflito na agenda.", "A tarefa passará para amanhã.", "Alta Prioridade", "Hoje · 09:30", null),
            ApprovalItem("2", Icons.Outlined.MonetizationOn, "Autorizar Orçamento", "A Lumière solicita sua aprovação para um novo orçamento.", "Será ativado o novo orçamento.", null, "Hoje · 10:00", null),
            ApprovalItem("3", Icons.Outlined.CheckCircle, "Adicionar Hábitos", "Sugestão de novo hábito baseado no teu padrão.", "O hábito será adicionado à tua rotina.", null, "Ontem · 14:15", true),
            ApprovalItem("4", Icons.Outlined.Notifications, "Criar Lembrete", "Lembrete para rever a reunião de amanhã.", "O lembrete será agendado.", null, "12/11/2025, 10:30", true),
            ApprovalItem("5", Icons.Outlined.Delete, "Eliminar Tarefa", "Tarefa duplicada detetada.", "A tarefa será removida.", null, "11/11/2025, 18:45", false)
        )
    }

    val pendingItems = allItems.filter { it.isApproved == null }
    val historyItems = allItems.filter { it.isApproved != null }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = LumiereSpacing.M, vertical = LumiereSpacing.M),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Aprovações", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.displaySmall, fontFamily = FontFamily.Serif)
                if (pendingItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(24.dp).background(Color.Red, CircleShape), contentAlignment = Alignment.Center) {
                        Text("${pendingItems.size}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = LumiereSpacing.M)) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]), color = MaterialTheme.colorScheme.primary, height = 2.dp)
                },
                divider = {}
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Schedule, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (selectedTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pendentes (${pendingItems.size})", color = if (selectedTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.History, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (selectedTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Histórico", color = if (selectedTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                })
            }
            Spacer(modifier = Modifier.height(LumiereSpacing.M))
            // Conteúdo das Tabs
            if (selectedTab == 0) {
                if (pendingItems.isEmpty()) {
                    LumiereEmptyState(
                        icon = Icons.Outlined.Shield,
                        title = "Tudo em dia",
                        description = "Não há decisões pendentes. Aproveita o teu dia."
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(LumiereSpacing.M), contentPadding = PaddingValues(bottom = LumiereSpacing.L)) {
                        items(pendingItems) { item ->
                            ApprovalCard(
                                item = item,
                                onApprove = {
                                    val index = allItems.indexOf(item)
                                    allItems[index] = item.copy(isApproved = true)
                                    scope.launch { snackbarHostState.showSnackbar("Aprovado: ${item.title}") }
                                },
                                onReject = {
                                    val index = allItems.indexOf(item)
                                    allItems[index] = item.copy(isApproved = false)
                                    scope.launch { snackbarHostState.showSnackbar("Rejeitado: ${item.title}") }
                                }
                            )
                        }
                    }
                }
            } else {
                // Aba Histórico
                if (historyItems.isEmpty()) {
                    LumiereEmptyState(icon = Icons.Outlined.History, title = "Sem histórico", description = "Ainda não tomaste nenhuma decisão.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(LumiereSpacing.M), contentPadding = PaddingValues(bottom = LumiereSpacing.L)) {
                        items(historyItems) { item ->
                            HistoryCard(item = item)
                        }
                        item {
                            SummaryCard(total = historyItems.size, approved = historyItems.count { it.isApproved == true }, rejected = historyItems.count { it.isApproved == false })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ApprovalCard(item: ApprovalItem, onApprove: () -> Unit, onReject: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.background(Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surface))).padding(LumiereSpacing.M)) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(item.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(LumiereSpacing.M))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontFamily = FontFamily.Serif)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        if (item.priority != null) {
                            Surface(shape = RoundedCornerShape(50), color = Color.Transparent, border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)) {
                                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(item.priority, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        Surface(shape = RoundedCornerShape(50), color = Color.Transparent, border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(item.time, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(LumiereSpacing.M))
                Text(item.description, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(LumiereSpacing.M))
                
                // Consequência
                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Consequência:", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                            Text(item.consequence, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(LumiereSpacing.M))
                
                // Botões
                Row(horizontalArrangement = Arrangement.spacedBy(LumiereSpacing.M)) {
                    Button(onClick = onApprove, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(50)) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Aprovar", color = MaterialTheme.colorScheme.onPrimary)
                    }
                    OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(50)) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rejeitar", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryCard(item: ApprovalItem) {
    val isApproved = item.isApproved == true
    val color = if (isApproved) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
    val icon = if (isApproved) Icons.Default.Check else Icons.Default.Close

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(modifier = Modifier.padding(LumiereSpacing.M), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(color, CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(LumiereSpacing.M))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontFamily = FontFamily.Serif)
                Text("${if (isApproved) "Aprovada" else "Rejeitada"} · ${item.time}", color = color, style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SummaryCard(total: Int, approved: Int, rejected: Int) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = LumiereSpacing.M),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.padding(LumiereSpacing.M), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.BarChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Total de decisões", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    Text("$total", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
                }
            }
            Divider(modifier = Modifier.height(24.dp).width(1.dp), color = MaterialTheme.colorScheme.outlineVariant)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text("Aprovadas", color = Color(0xFF4CAF50), style = MaterialTheme.typography.labelSmall)
                    Text("$approved", color = Color(0xFF4CAF50), style = MaterialTheme.typography.titleMedium)
                }
            }
            Divider(modifier = Modifier.height(24.dp).width(1.dp), color = MaterialTheme.colorScheme.outlineVariant)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text("Rejeitadas", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    Text("$rejected", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
