package com.lumiere.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.lumiere.app.ui.theme.LumiereSpacing

@Composable
fun MetricTile(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    color: Color,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.small,
    ) {
        Column(
            modifier = Modifier.padding(LumiereSpacing.S).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                color = color,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(LumiereSpacing.XS))
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    count: String,
    color: Color,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.padding(LumiereSpacing.M)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = color)
                Spacer(modifier = Modifier.width(LumiereSpacing.S))
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.height(LumiereSpacing.S))
            Text(
                text = count,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.displayMedium,
            )
        }
    }
}
