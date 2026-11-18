package com.lumiere.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumiere.app.ui.theme.LumiereSpacing

@Composable
fun ApprovalsScreen(onBack: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = LumiereSpacing.M), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = MaterialTheme.colorScheme.primary) }
            Spacer(modifier = Modifier.width(LumiereSpacing.S))
            Text("Aprovações", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineMedium, fontFamily = FontFamily.Serif)
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.size(24.dp).background(Color.Red, CircleShape), contentAlignment = Alignment.Center) {
                Text("2", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Text("Decisões que requerem a sua atenção", modifier = Modifier.padding(horizontal = 56.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(LumiereSpacing.L))

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = LumiereSpacing.M), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TabPill("Pendentes (2)", Icons.Outlined.Schedule, selected = selectedTab == 0, modifier = Modifier.weight(1f)) { selectedTab = 0 }
            TabPill("Histórico", Icons.Outlined.History, selected = selectedTab == 1, modifier = Modifier.weight(1f)) { selectedTab = 1 }
        }

        Spacer(modifier = Modifier.height(LumiereSpacing.M))

        if (selectedTab == 0) { PendingTab() } else { HistoryTab() }
        
        // O SEGREDO PARA NÃO CORTAR NO FUNDO:
        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
fun TabPill(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val bgColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
    
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, color = contentColor, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun PendingTab() {
    Column(modifier = Modifier.padding(horizontal = LumiereSpacing.M), verticalArrangement = Arrangement.spacedBy(LumiereSpacing.M)) {
        ApprovalCard(Icons.Outlined.CalendarToday, "Reagendar Tarefa", true, "Hoje • 09:30", "A Lumière sugere mover a tarefa devido a um conflito na agenda.", "A tarefa passará para amanhã.")
        ApprovalCard(Icons.Outlined.AttachMoney, "Autorizar Orçamento", false, "Hoje • 10:00", "A Lumière solicita sua aprovação para um novo orçamento.", "Será ativado o novo orçamento.")
    }
}

@Composable
fun ApprovalCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, isHighPriority: Boolean, time: String, description: String, consequence: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(LumiereSpacing.M)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(LumiereSpacing.M))
                Column(modifier = Modifier.weight(1f)) {
                    if (isHighPriority) {
                        Row(modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(50)).padding(horizontal = 8.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Alta Prioridade", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Text(title, style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(time, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(LumiereSpacing.M))
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(LumiereSpacing.M))
            Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Consequência:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(consequence, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            Spacer(modifier = Modifier.height(LumiereSpacing.M))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("Aprovar")
                }
                OutlinedButton(onClick = { }, modifier = Modifier.weight(1f), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("Rejeitar", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun HistoryTab() {
    Column(modifier = Modifier.padding(horizontal = LumiereSpacing.M)) {
        HistoryRow("Reagendar Tarefa", "Aprovada • Hoje, 09:32", true)
        Spacer(modifier = Modifier.height(8.dp))
        HistoryRow("Autorizar Orçamento", "Rejeitada • Ontem, 16:20", false)
        Spacer(modifier = Modifier.height(8.dp))
        HistoryRow("Adicionar Hábitos", "Aprovada • Ontem, 14:15", true)
        Spacer(modifier = Modifier.height(8.dp))
        HistoryRow("Criar Lembrete", "Aprovada • 12/11/2025, 10:30", true)
        Spacer(modifier = Modifier.height(8.dp))
        HistoryRow("Eliminar Tarefa", "Rejeitada • 11/11/2025, 18:45", false)
        
        Spacer(modifier = Modifier.height(LumiereSpacing.L))
        
        // BARRA DE ESTATÍSTICAS (Formatada)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), 
            shape = RoundedCornerShape(12.dp), 
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), 
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Ícone de Gráfico
                Icon(
                    imageVector = Icons.Default.BarChart, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Total
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total de decisões", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("5", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                
                // Divisor
                Divider(modifier = Modifier.height(30.dp).width(1.dp), color = MaterialTheme.colorScheme.outlineVariant)
                
                // Aprovadas
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Aprovadas", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("3", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                }
                
                // Divisor
                Divider(modifier = Modifier.height(30.dp).width(1.dp), color = MaterialTheme.colorScheme.outlineVariant)
                
                // Rejeitadas
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Cancel, contentDescription = null, tint = Color.Red, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rejeitadas", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("2", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.Red)
                }
            }
        }
    }
}

@Composable
fun HistoryRow(title: String, subtitle: String, isApproved: Boolean) {
    // Cartão individual para cada item do histórico
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(36.dp).background(if (isApproved) Color(0xFF4CAF50) else Color.Red, CircleShape), 
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isApproved) Icons.Default.Check else Icons.Default.Close, 
                    contentDescription = null, 
                    tint = Color.White, 
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = if (isApproved) Color(0xFF4CAF50) else Color.Red)
            }
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
