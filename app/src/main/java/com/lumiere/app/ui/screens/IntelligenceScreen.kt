package com.lumiere.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumiere.app.core.engine.ConflictKind
import com.lumiere.app.core.model.Suggestion
import com.lumiere.app.ui.theme.LumiereSpacing
import com.lumiere.app.ui.viewmodel.IntelligenceViewModel

@Composable
fun IntelligenceScreen(viewModel: IntelligenceViewModel, onBack: () -> Unit) {
    val focusTrend by viewModel.focusTrend.collectAsStateWithLifecycle()
    val recommendation by viewModel.recommendation.collectAsStateWithLifecycle()
    val conflicts by viewModel.conflicts.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = LumiereSpacing.M), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Voltar", tint = MaterialTheme.colorScheme.primary) }
            Spacer(Modifier.width(LumiereSpacing.S))
            Text("Inteligência", style = MaterialTheme.typography.headlineMedium, fontFamily = FontFamily.Serif, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.weight(1f))
            if (isLoading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
        }
        Spacer(Modifier.height(LumiereSpacing.L))

        Card(Modifier.fillMaxWidth().padding(horizontal = LumiereSpacing.M), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Tendência de Foco (7 dias)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(if (focusTrend.isEmpty()) "Sem conclusões registadas" else "Conclusões por dia, normalizadas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                SparklineChart(focusTrend, Modifier.fillMaxWidth().height(80.dp))
            }
        }

        Spacer(Modifier.height(LumiereSpacing.M))

        val goal = recommendation?.goal ?: "Sem recomendações no momento"
        val suggestions = recommendation?.suggestions ?: emptyList()
        Card(Modifier.fillMaxWidth().padding(horizontal = LumiereSpacing.M), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Lightbulb, null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Recomendação", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(goal, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Spacer(Modifier.height(LumiereSpacing.L))

        Row(Modifier.fillMaxWidth().padding(horizontal = LumiereSpacing.M), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Sugestões", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("${suggestions.size} proposta(s)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(LumiereSpacing.S))

        if (suggestions.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(LumiereSpacing.M), contentAlignment = Alignment.Center) {
                Text("Sem sugestões pendentes.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            suggestions.forEach { s ->
                SuggestionCard(s, onApprove = { viewModel.approveSuggestion(s) }, onReject = { viewModel.rejectSuggestion(s) })
                Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(LumiereSpacing.L))

        Row(Modifier.fillMaxWidth().padding(horizontal = LumiereSpacing.M), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Conflitos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("${conflicts.size} detetado(s)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(LumiereSpacing.S))

        if (conflicts.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(LumiereSpacing.M), contentAlignment = Alignment.Center) {
                Text("Sem conflitos temporais.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            conflicts.take(5).forEach { c ->
                Card(Modifier.fillMaxWidth().padding(horizontal = LumiereSpacing.M, vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Warning, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(c.kind.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("${c.involvedRecordIds.size} registo(s) envolvido(s)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(100.dp))
    }
}

@Composable
fun SuggestionCard(s: Suggestion, onApprove: () -> Unit, onReject: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(horizontal = LumiereSpacing.M), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(s.statement, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("${s.approvalClass.name} · ${s.impact.name} · ${s.reversibility.name}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onApprove, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Check, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Aprovar")
                }
                OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Close, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Rejeitar")
                }
            }
        }
    }
}

@Composable
fun SparklineChart(dataPoints: List<Float>, modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val bg = primary.copy(alpha = 0.1f)
    Canvas(modifier) {
        if (dataPoints.size < 2) return@Canvas
        val maxV = dataPoints.maxOrNull() ?: 1f
        val minV = dataPoints.minOrNull() ?: 0f
        val range = if (maxV == minV) 1f else maxV - minV
        val spacing = size.width / (dataPoints.size - 1)
        val path = Path(); val fill = Path()
        dataPoints.forEachIndexed { i, v ->
            val x = i * spacing
            val y = size.height - ((v - minV) / range) * size.height * 0.8f - size.height * 0.1f
            if (i == 0) { path.moveTo(x, y); fill.moveTo(x, size.height); fill.lineTo(x, y) }
            else { path.lineTo(x, y); fill.lineTo(x, y) }
            if (i == dataPoints.size - 1) { fill.lineTo(x, size.height); fill.close() }
        }
        drawPath(fill, bg); drawPath(path, primary, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
    }
}
