package com.lumiere.app.ui.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lumiere.app.ui.theme.LumiereIcons
import com.lumiere.app.ui.theme.LumiereRadius
import com.lumiere.app.ui.theme.LumiereSpacing

@Composable
fun LumiereUniversalCard(
    variant: CardVariant = CardVariant.ENTITY,
    data: CardData,
    density: CardDensity = CardDensity.NORMAL,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showMoreOptionsMenu by remember { mutableStateOf(false) }

    val shape = RoundedCornerShape(LumiereRadius.M)
    val isEntity = variant == CardVariant.ENTITY
    val isCompact = density == CardDensity.COMPACT

    val stateColor = when (data.state) {
        LumiereCardState.PENDING -> Color(0xFFA0A0A0)
        LumiereCardState.IN_PROGRESS -> Color(0xFF3B82F6)
        LumiereCardState.DONE -> Color(0xFF10B981)
        LumiereCardState.OVERDUE -> MaterialTheme.colorScheme.error
        LumiereCardState.PAUSED -> Color(0xFF9C27B0)
        LumiereCardState.ARCHIVED -> Color(0xFF5A5A5A)
        null -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = shape
    ) {
        Row(
            modifier = Modifier.padding(if (isCompact) LumiereSpacing.S else LumiereSpacing.M),
            verticalAlignment = Alignment.Top
        ) {
            // 1. Ícone / Thumbnail
            Box(
                modifier = Modifier
                    .size(if (isCompact) 40.dp else 64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    )
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(LumiereIcons.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(if (isCompact) 24.dp else 32.dp))
            }

            Spacer(modifier = Modifier.width(LumiereSpacing.M))

            // 2. Conteúdo Principal
            Column(modifier = Modifier.weight(1f)) {
                // Tags Superiores
                if (!isCompact) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                        Box(
                            modifier = Modifier
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
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
                            Box(
                                modifier = Modifier
                                    .background(stateColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(data.state.label, style = MaterialTheme.typography.labelSmall, color = stateColor, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Título
                Text(
                    text = data.title,
                    style = if (isCompact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Descrição com "Ler Mais"
                if (!data.description.isNullOrBlank() && !isCompact) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = data.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isExpanded) "Ler menos" else "Ler mais",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clickable { isExpanded = !isExpanded }
                    )
                }

                // Metadados (Data, Projeto, Anexos)
                if (!isCompact && (data.metadata != null || data.subtitle != null || data.attachmentCount != null)) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        data.metadata?.let {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(LumiereIcons.Event, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        data.subtitle?.let {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(LumiereIcons.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        data.attachmentCount?.let {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(LumiereIcons.Tag, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("$it anexos", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                // Progresso
                if (data.progress != null && !isCompact) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = data.progress,
                            modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${(data.progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            // 3. Menu de 3 Pontinhos
            if (!isCompact) {
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                        Icon(LumiereIcons.MoreVert, contentDescription = "Mais opções", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        data.menuActions.forEach { action ->
                            DropdownMenuItem(
                                text = { Text(action.label) },
                                onClick = { action.onClick(); showMenu = false },
                                leadingIcon = action.icon?.let { { Icon(it, contentDescription = null) } }
                            )
                        }
                    }
                }
            }
        }

        // 4. Rodapé de Ações (Apenas se não for Compact)
        if (!isCompact && data.actions.isNotEmpty()) {
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = LumiereSpacing.M, vertical = LumiereSpacing.S),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                data.actions.forEach { action ->
                    if (action.isPrimary) {
                        Button(
                            onClick = action.onClick,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            action.icon?.let { Icon(it, contentDescription = null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)) }
                            Text(action.label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        TextButton(onClick = action.onClick) {
                            action.icon?.let { Icon(it, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)) }
                            Text(action.label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Menu Mais Opções (Rodapé)
                if (data.menuActions.isNotEmpty()) {
                    Box {
                        TextButton(onClick = { showMoreOptionsMenu = true }) {
                            Text("Mais opções", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Icon(LumiereIcons.MoreVert, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        }
                        DropdownMenu(expanded = showMoreOptionsMenu, onDismissRequest = { showMoreOptionsMenu = false }) {
                            data.menuActions.forEach { action ->
                                DropdownMenuItem(
                                    text = { Text(action.label) },
                                    onClick = { action.onClick(); showMoreOptionsMenu = false },
                                    leadingIcon = action.icon?.let { { Icon(it, contentDescription = null) } }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
