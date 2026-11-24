package com.lumiere.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumiere.app.ui.theme.Lumiere
import com.lumiere.app.ui.theme.LumiereRadius
import com.lumiere.app.ui.theme.LumiereSpacing

@Composable
fun LumiereUniversalCard(
    variant: CardVariant,
    data: CardData,
    modifier: Modifier = Modifier,
    density: CardDensity = CardDensity.NORMAL,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(LumiereRadius.M)
    val gold = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)

    val stateColor = when (data.state) {
        LumiereCardState.PENDING -> Color(0xFFA0A0A0)
        LumiereCardState.IN_PROGRESS -> Color(0xFF3B82F6)
        LumiereCardState.DONE -> Lumiere.colors.success
        LumiereCardState.OVERDUE -> MaterialTheme.colorScheme.error
        LumiereCardState.PAUSED -> Color(0xFF9C27B0)
        LumiereCardState.ARCHIVED -> Color(0xFF5A5A5A)
        null -> gold
    }

    val baseModifier = modifier
        .fillMaxWidth()
        .clip(shape)
        .background(surfaceColor)
        .border(1.dp, outlineColor, shape)
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)

    when (variant) {
        CardVariant.SUMMARY -> SummaryCard(data, baseModifier, gold)
        CardVariant.PROGRESS -> ProgressCard(data, baseModifier, gold)
        CardVariant.ALERT -> AlertCard(data, baseModifier)
        CardVariant.INFO -> InfoCard(data, baseModifier, gold)
        CardVariant.ENTITY -> EntityCard(data, baseModifier, stateColor, gold, density)
    }
}

@Composable
private fun SummaryCard(data: CardData, modifier: Modifier, gold: Color) {
    Box(modifier = modifier.height(110.dp)) {
        // Gráfico de ondas no fundo (Canvas)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(0f, size.height * 0.8f)
                cubicTo(
                    size.width * 0.2f, size.height * 0.9f,
                    size.width * 0.4f, size.height * 0.6f,
                    size.width * 0.6f, size.height * 0.7f
                )
                cubicTo(
                    size.width * 0.8f, size.height * 0.8f,
                    size.width * 0.9f, size.height * 0.5f,
                    size.width, size.height * 0.6f
                )
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(gold.copy(alpha = 0.2f), Color.Transparent)
                )
            )
        }
        Column(modifier = Modifier.padding(LumiereSpacing.M).fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                data.summaryIcon?.let {
                    Icon(it, contentDescription = null, tint = gold, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(LumiereSpacing.S))
                }
                Text(data.summaryValue ?: "0", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            }
            Text(data.title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            data.summaryTrend?.let {
                Text(it, style = MaterialTheme.typography.labelMedium, color = Lumiere.colors.success, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ProgressCard(data: CardData, modifier: Modifier, gold: Color) {
    Row(modifier = modifier.padding(LumiereSpacing.M), verticalAlignment = Alignment.CenterVertically) {
        // Donut Chart (Canvas)
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = gold.copy(alpha = 0.2f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 12f, cap = StrokeCap.Round)
                )
                drawArc(
                    color = gold,
                    startAngle = -90f,
                    sweepAngle = (data.progress ?: 0f) * 360f,
                    useCenter = false,
                    style = Stroke(width = 12f, cap = StrokeCap.Round)
                )
            }
            Text("${((data.progress ?: 0f) * 100).toInt()}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(modifier = Modifier.width(LumiereSpacing.M))
        Column(modifier = Modifier.weight(1f)) {
            Text(data.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            data.subtitle?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(LumiereSpacing.S))
            data.actions.firstOrNull()?.let { action ->
                Button(
                    onClick = action.onClick,
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black)
                ) {
                    Text(action.label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AlertCard(data: CardData, modifier: Modifier) {
    val redGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF3A0A0A), Color(0xFF1E1E1E))
    )
    Box(modifier = modifier.background(redGradient)) {
        Row(modifier = Modifier.padding(LumiereSpacing.M), verticalAlignment = Alignment.CenterVertically) {
            data.icon?.let {
                Icon(it, contentDescription = null, tint = Color.Red, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.width(LumiereSpacing.M))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(data.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFFF6B6B))
                data.subtitle?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            data.actions.firstOrNull()?.let { action ->
                TextButton(onClick = action.onClick) {
                    Text(action.label, color = Color(0xFFFF6B6B), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun InfoCard(data: CardData, modifier: Modifier, gold: Color) {
    Row(modifier = modifier.padding(LumiereSpacing.S), verticalAlignment = Alignment.CenterVertically) {
        data.image?.let {
            Image(painter = it, contentDescription = null, modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp)))
            Spacer(modifier = Modifier.width(LumiereSpacing.M))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(data.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            data.subtitle?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Spacer(modifier = Modifier.height(LumiereSpacing.S))
            data.actions.firstOrNull()?.let { action ->
                Button(
                    onClick = action.onClick,
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black)
                ) {
                    Text(action.label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun EntityCard(data: CardData, modifier: Modifier, stateColor: Color, gold: Color, density: CardDensity) {
    Row(
        modifier = modifier.padding(if (density == CardDensity.COMPACT) LumiereSpacing.S else LumiereSpacing.M),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Imagem ou Ícone Grande com gradiente
        Box(
            modifier = Modifier
                .size(if (density == CardDensity.COMPACT) 40.dp else 80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.radialGradient(colors = listOf(gold.copy(alpha = 0.3f), Color.Transparent)))
                .border(1.dp, gold.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (data.image != null) {
                Image(painter = data.image, contentDescription = null, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)))
            } else {
                data.icon?.let { Icon(it, contentDescription = null, tint = gold, modifier = Modifier.size(32.dp)) }
            }
        }
        
        Spacer(modifier = Modifier.width(LumiereSpacing.M))
        
        Column(modifier = Modifier.weight(1f)) {
            // Tags superiores
            Row(verticalAlignment = Alignment.CenterVertically) {
                data.tagText?.let {
                    Box(
                        modifier = Modifier.border(1.dp, gold.copy(alpha = 0.5f), RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(it, style = MaterialTheme.typography.labelSmall, color = gold)
                    }
                    Spacer(modifier = Modifier.width(LumiereSpacing.S))
                }
                if (data.isPriority) {
                    Icon(Icons.Default.Star, contentDescription = "Prioritária", tint = gold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Prioritária", style = MaterialTheme.typography.labelSmall, color = gold)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(data.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            data.metadata?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            data.progress?.let {
                Spacer(modifier = Modifier.height(LumiereSpacing.S))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = it,
                        modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = gold,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${(it * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            
            if (data.actions.isNotEmpty() && density != CardDensity.COMPACT) {
                Spacer(modifier = Modifier.height(LumiereSpacing.S))
                Row(horizontalArrangement = Arrangement.spacedBy(LumiereSpacing.S)) {
                    data.actions.forEach { action ->
                        if (action.isPrimary) {
                            Button(
                                onClick = action.onClick, 
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black)
                            ) {
                                Text(action.label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            TextButton(onClick = action.onClick) {
                                Text(action.label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
        
        // Chip de Estado e Menu
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.SpaceBetween) {
            data.state?.let {
                Box(
                    modifier = Modifier
                        .background(stateColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(it.label, style = MaterialTheme.typography.labelSmall, color = stateColor, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            data.onMoreClick?.let { more ->
                IconButton(onClick = more, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Mais opções", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
