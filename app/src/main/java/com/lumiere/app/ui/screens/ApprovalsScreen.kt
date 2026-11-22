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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumiere.app.core.model.Suggestion
import com.lumiere.app.ui.theme.LumiereSpacing
import com.lumiere.app.ui.viewmodel.IntelligenceViewModel

@Composable
fun ApprovalsScreen(viewModel: IntelligenceViewModel, onBack: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val recommendation by viewModel.recommendation.collectAsStateWithLifecycle()
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
            Box(Modifier.fillMaxWidth().padding(LumiereSpacing.L), contentAlignment = Alignment.Center) {
                Text("As decisões históricas aparecerão aqui.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(100.dp))
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
                    Text(s.approvalClass.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(s.statement, style = MaterialTheme.typography.titleMedium, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(LumiereSpacing.M))
            Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).padding(12.dp)) {
                Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Consequência:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text("${s.impact.name} · ${s.reversibility.name}", style = MaterialTheme.typography.bodySmall)
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
fun TabPill(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
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
