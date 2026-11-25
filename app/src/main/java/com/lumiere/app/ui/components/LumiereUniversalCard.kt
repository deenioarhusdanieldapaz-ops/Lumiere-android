package com.lumiere.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lumiere.app.ui.theme.LumiereRadius
import com.lumiere.app.ui.theme.LumiereSpacing

data class CardData(
    val title: String,
    val subtitle: String,
    val isOverdue: Boolean = false,
    val isCompleted: Boolean = false
)

@Composable
fun LumiereUniversalCard(
    data: CardData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stateColor = when {
        data.isOverdue -> MaterialTheme.colorScheme.error
        data.isCompleted -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(LumiereRadius.M))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(LumiereRadius.M))
            .clickable { onClick() }
            .padding(LumiereSpacing.M),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Indicador de Estado Lateral
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(40.dp)
                .background(stateColor, RoundedCornerShape(2.dp))
        )
        
        Spacer(modifier = Modifier.width(LumiereSpacing.M))

        // Conteúdo
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = data.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = data.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = if (data.isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Ação (Círculo de conclusão)
        Box(
            modifier = Modifier
                .size(24.dp)
                .border(2.dp, stateColor, RoundedCornerShape(12.dp))
                .background(if (data.isCompleted) stateColor else MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
        )
    }
}
