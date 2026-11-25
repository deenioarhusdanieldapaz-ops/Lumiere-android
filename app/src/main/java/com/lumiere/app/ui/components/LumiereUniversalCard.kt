package com.lumiere.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lumiere.app.ui.theme.LumiereRadius

@Composable
fun LumiereUniversalCard(
    variant: CardVariant,
    data: CardData,
    density: CardDensity = CardDensity.NORMAL,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // 1. A Moldura Base (O Tema é aplicado aqui)
    val shape = RoundedCornerShape(LumiereRadius.M)
    val outlineColor = if (data.state == LumiereCardState.OVERDUE) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, outlineColor, shape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = shape
    ) {
        // 2. O Router (Encaminha para o Chef certo)
        when (variant) {
            CardVariant.ENTITY -> EntityCardContent(data = data, density = density)
            CardVariant.SUMMARY -> SummaryCardContent(data = data)
            CardVariant.PROGRESS -> ProgressCardContent(data = data)
            CardVariant.ALERT -> AlertCardContent(data = data)
            CardVariant.INFO -> InfoCardContent(data = data)
            CardVariant.COMPACT -> CompactCardContent(data = data)
        }
    }
}
