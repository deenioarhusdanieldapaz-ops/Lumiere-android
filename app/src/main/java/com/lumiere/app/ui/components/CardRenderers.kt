package com.lumiere.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lumiere.app.ui.theme.LumiereIcons
import com.lumiere.app.ui.theme.LumiereRadius
import com.lumiere.app.ui.theme.LumiereSpacing

// --- FUNÇÃO AUXILIAR PARA COR DO ESTADO ---
@Composable
fun getStateColor(state: LumiereCardState?): Color {
    return when (state) {
        LumiereCardState.PENDING -> Color(0xFFA0A0A0)
        LumiereCardState.IN_PROGRESS -> Color(0xFF3B82F6)
        LumiereCardState.DONE -> Color(0xFF10B981)
        LumiereCardState.OVERDUE -> MaterialTheme.colorScheme.error
        LumiereCardState.PAUSED -> Color(0xFF9C27B0)
        LumiereCardState.ARCHIVED -> Color(0xFF5A5A5A)
        null -> MaterialTheme.colorScheme.primary
    }
}

// --- 1. ENTITY (A Tarefa Completa) ---
@Composable
fun EntityCardContent(data: CardData, density: CardDensity) {
    val isCompact = density == CardDensity.COMPACT
    val stateColor = getStateColor(data.state)
    var isExpanded by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.padding(if (isCompact) LumiereSpacing.S else LumiereSpacing.M),
        verticalAlignment = Alignment.Top
    ) {
        // Ícone
        Box(
            modifier = Modifier
                .size(if (isCompact) 40.dp else 64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), MaterialTheme.colorScheme.surfaceVariant)))
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(data.icon ?: LumiereIcons.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(if (isCompact) 24.dp else 32.dp))
        }

        Spacer(modifier = Modifier.width(LumiereSpacing.M))

        // Conteúdo
        Column(modifier = Modifier.weight(1f)) {
            if (!isCompact) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                    Box(modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text(data.tagText ?: "Tarefa", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (data.isPriority) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(LumiereIcons.Star, contentDescription = "Prioritária", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Prioritária", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (data.state != null && data.state != LumiereCardState.PENDING) {
                        Box(modifier = Modifier.background(stateColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                            Text(data.state.label, style = MaterialTheme.typography.labelSmall, color = stateColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Text(data.title, style = if (isCompact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)

            if (!data.description.isNullOrBlank() && !isCompact) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(data.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = if (isExpanded) Int.MAX_VALUE else 2, overflow = TextOverflow.Ellipsis)
                Text(if (isExpanded) "Ler menos" else "Ler mais", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp).clickable { isExpanded = !isExpanded })
            }

            if (!isCompact && (data.metadata != null || data.subtitle != null || data.attachmentCount != null)) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    data.metadata?.let { m -> Row(verticalAlignment = Alignment.CenterVertically) { Icon(LumiereIcons.Event, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp)); Spacer(Modifier.width(4.dp)); Text(m, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                    data.subtitle?.let { s -> Row(verticalAlignment = Alignment.CenterVertically) { Icon(LumiereIcons.Folder, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp)); Spacer(Modifier.width(4.dp)); Text(s, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                    data.attachmentCount?.let { count -> Row(verticalAlignment = Alignment.CenterVertically) { Icon(LumiereIcons.Tag, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp)); Spacer(Modifier.width(4.dp)); Text("$count anexos", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                }
            }
        }

        if (!isCompact) {
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) { Icon(LumiereIcons.MoreVert, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    data.menuActions.forEach { action -> DropdownMenuItem(text = { Text(action.label) }, onClick = { action.onClick(); showMenu = false }, leadingIcon = action.icon?.let { { Icon(it, null) } }) }
                }
            }
        }
    }

    if (!isCompact && data.actions.isNotEmpty()) {
        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = LumiereSpacing.M, vertical = LumiereSpacing.S), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            data.actions.forEach { action ->
                if (action.isPrimary) {
                    Button(onClick = action.onClick, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp), modifier = Modifier.height(36.dp)) {
                        action.icon?.let { Icon(it, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)) }; Text(action.label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                } else {
                    TextButton(onClick = action.onClick) { action.icon?.let { Icon(it, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)) }; Text(action.label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
        }
    }
}

// --- 2. SUMMARY (Os Resumos do Topo) ---
@Composable
fun SummaryCardContent(data: CardData) {
    Column(modifier = Modifier.padding(LumiereSpacing.M).fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            data.summaryIcon?.let { Icon(it, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)) }
            Text(data.summaryValue ?: "0", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(Modifier.height(4.dp))
        Text(data.title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        data.summaryTrend?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = Color(0xFF10B981), fontWeight = FontWeight.Bold) }
    }
}

// --- 3. PROGRESS (O Gráfico Circular) ---
@Composable
fun ProgressCardContent(data: CardData) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = primaryColor.copy(alpha = 0.2f)
    
    Row(modifier = Modifier.padding(LumiereSpacing.M), verticalAlignment = Alignment.CenterVertically) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(color = trackColor, startAngle = -90f, sweepAngle = 360f, useCenter = false, style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round))
                drawArc(color = primaryColor, startAngle = -90f, sweepAngle = (data.progress ?: 0f) * 360f, useCenter = false, style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round))
            }
            Text("${((data.progress ?: 0f) * 100).toInt()}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(LumiereSpacing.M))
        Column {
            Text(data.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            data.description?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

// --- 4. ALERT (O Aviso Vermelho) ---
@Composable
fun AlertCardContent(data: CardData) {
    Box(modifier = Modifier.background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f), MaterialTheme.colorScheme.surface))).fillMaxWidth()) {
        Row(modifier = Modifier.padding(LumiereSpacing.M), verticalAlignment = Alignment.CenterVertically) {
            Icon(LumiereIcons.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(LumiereSpacing.M))
            Column {
                Text(data.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                data.description?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}

// --- 5. INFO (O Momento de Foco) ---
@Composable
fun InfoCardContent(data: CardData) {
    Row(modifier = Modifier.padding(LumiereSpacing.M), verticalAlignment = Alignment.CenterVertically) {
        data.image?.let {
            Image(painter = it, contentDescription = null, modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)))
            Spacer(Modifier.width(LumiereSpacing.M))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(data.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            data.description?.let { Text(it, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis) }
        }
    }
}

// --- 6. COMPACT (Lista Densa) ---
@Composable
fun CompactCardContent(data: CardData) {
    val stateColor = getStateColor(data.state)
    Row(modifier = Modifier.padding(LumiereSpacing.S), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(24.dp).background(stateColor, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            if (data.state == LumiereCardState.DONE) Icon(LumiereIcons.CheckCircle, null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(LumiereSpacing.S))
        Column(modifier = Modifier.weight(1f)) {
            Text(data.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            data.metadata?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        data.actions.firstOrNull { it.isPrimary }?.let { action ->
            TextButton(onClick = action.onClick) { Text(action.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) }
        }
    }
}
