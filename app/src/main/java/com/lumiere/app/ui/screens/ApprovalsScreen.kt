package com.lumiere.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumiere.app.core.engine.ApprovalDecision
import com.lumiere.app.core.engine.ApprovalDecisionKind
import com.lumiere.app.core.model.Suggestion
import com.lumiere.app.core.time.TimeUtils
import com.lumiere.app.ui.theme.LumiereSpacing
import com.lumiere.app.ui.utils.toPT
import com.lumiere.app.ui.viewmodel.IntelligenceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private data class DecisionVisual(
    val icon: ImageVector,
    val tint: Color,
    val title: String,
    val subtitle: String,
    val highlightPartial: Boolean = false
)

private fun visualFor(dec: ApprovalDecision): DecisionVisual = when (dec.decision) {
    ApprovalDecisionKind.APPROVED -> DecisionVisual(
        Icons.Default.CheckCircle, Color(0xFF4CAF50), "Aprovado",
        "Decidido automaticamente"
    )
    ApprovalDecisionKind.PARTIAL -> DecisionVisual(
        Icons.Default.Warning, Color(0xFFFFC107), "Parcialmente aplicado",
        "Algumas alterações falharam",
        highlightPartial = true
    )
    ApprovalDecisionKind.REJECTED -> DecisionVisual(
        Icons.Default.Cancel, Color(0xFFF44336), "Rejeitado",
        "Por: Proprietário"
    )
    ApprovalDecisionKind.FAILED -> DecisionVisual(
        Icons.Default.Error, Color(0xFFB71C1C), "Falhou ao aplicar",
        "Nenhuma alteração efetuada"
    )
    ApprovalDecisionKind.EXPIRED -> DecisionVisual(
        Icons.Default.History, Color(0xFF757575), "Expirado",
        "Fora do prazo de validade"
    )
}

private fun groupHistoryByDate(history: List<ApprovalDecision>): Map<String, List<ApprovalDecision>> {
    val todayKey = TimeUtils.dayKey(System.currentTimeMillis(), TimeZone.getDefault())
    val yesterdayKey = todayKey - 1
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    return history.groupBy { dec ->
        val key = TimeUtils.dayKey(dec.decidedAtMillis, TimeZone.getDefault())
        when (key) {
            todayKey -> "Hoje"
            yesterdayKey -> "Ontem"
            else -> sdf.format(Date(dec.decidedAtMillis))
        }
    }
}

@Composable
fun ApprovalsScreen(viewModel: IntelligenceViewModel, onBack: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val recommendation by viewModel.recommendation.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val suggestions = recommendation?.suggestions ?: emptyList()

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = LumiereSpacing.M), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Voltar", tint = MaterialTheme.colorScheme.primary) }
            Spacer(Modifier.width(LumiereSpacing.S))
            Text("Aprovações", style = MaterialTheme.typography.headlineMedium, fontFamily = FontFamily.Serif, color = MaterialTheme.colorScheme.onSurface)
            if (suggestions.isNotEmpty()) {
                Spacer(Modifier.width(8.dp))
                Box(Modifier.size(24.dp).background(Color.Red, CircleShape), contentAlignment = Alignment.Center) {
                    Text("${suggestions.size}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Text("Decisões que requerem a sua atenção", Modifier.padding(horizontal = 56.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(LumiereSpacing.L))

        Row(Modifier.fillMaxWidth().padding(horizontal = LumiereSpacing.M), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TabPill("Pendentes (${suggestions.size})", Icons.Outlined.Schedule, selectedTab == 0, Modifier.weight(1f)) { selectedTab = 0 }
            TabPill("Histórico", Icons.Outlined.History, selectedTab == 1, Modifier.weight(1f)) { selectedTab = 1 }
        }
        Spacer(Modifier.height(LumiereSpacing.M))

        if (selectedTab == 0) {
            if (suggestions.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(LumiereSpacing.L), contentAlignment = Alignment.Center) {
                    Text("Sem aprovações pendentes.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Column(Modifier.padding(horizontal = LumiereSpacing.M), verticalArrangement = Arrangement.spacedBy(LumiereSpacing.M)) {
                    suggestions.forEach { s -> RealApprovalCard(s, viewModel) }
                }
            }
        } else {
            if (history.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(LumiereSpacing.L), contentAlignment = Alignment.Center) {
                    Text("Ainda não tomaste decisões.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                val grouped = groupHistoryByDate(history.take(20))
                Column(Modifier.padding(horizontal = LumiereSpacing.M), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    grouped.forEach { (dateLabel, items) ->
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.DateRange, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(dateLabel, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                            }
                            items.forEach { dec -> DecisionRow(dec) }
                        }
                    }
                    OutlinedButton(
                        onClick = { /* TODO: implementar paginação futura */ },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(4.dp))
                        Text("Ver histórico mais antigo", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        Spacer(Modifier.height(100.dp))
    }
}

@Composable
private fun DecisionRow(dec: ApprovalDecision) {
    val v = visualFor(dec)
    val bg = if (v.highlightPartial) v.tint.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surface
    val border = if (v.highlightPartial) BorderStroke(1.dp, v.tint.copy(alpha = 0.6f)) else null
    
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeLabel = sdf.format(Date(dec.decidedAtMillis))
    
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bg),
        border = border
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).background(v.tint.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(v.icon, null, tint = v.tint, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(v.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(v.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.width(8.dp))
            Text(timeLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun RealApprovalCard(s: Suggestion, viewModel: IntelligenceViewModel) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(LumiereSpacing.M)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Lightbulb, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(LumiereSpacing.S))
                Column(Modifier.weight(1f)) {
                    Text(s.approvalClass.toPT(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(s.statement, style = MaterialTheme.typography.titleMedium, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(LumiereSpacing.M))
            Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).padding(12.dp)) {
                Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Porquê?", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(s.why, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(4.dp))
                    Text("Impacto:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(s.impactDescription, style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(LumiereSpacing.M))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.approveSuggestion(s) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Check, null, Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Aprovar")
                }
                OutlinedButton(onClick = { viewModel.rejectSuggestion(s) }, modifier = Modifier.weight(1f), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)) {
                    Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Rejeitar", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun TabPill(title: String, icon: ImageVector, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val bgColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
    Box(modifier = modifier.height(48.dp).clip(RoundedCornerShape(50)).background(bgColor).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = contentColor, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(title, color = contentColor, fontWeight = FontWeight.SemiBold)
        }
    }
}
