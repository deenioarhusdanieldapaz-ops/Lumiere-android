package com.lumiere.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumiere.app.ui.theme.LumiereSpacing
import com.lumiere.app.ui.viewmodel.NotificationsViewModel

@Composable
fun NotificationSettingsScreen(
    notificationsViewModel: NotificationsViewModel,
    onBack: () -> Unit
) {
    val enabled by notificationsViewModel.enabled.collectAsStateWithLifecycle()
    val soundEnabled by notificationsViewModel.soundEnabled.collectAsStateWithLifecycle()
    val vibrationEnabled by notificationsViewModel.vibrationEnabled.collectAsStateWithLifecycle()

    val subAlpha = if (enabled) 1f else 0.4f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = LumiereSpacing.M)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        // Top Bar
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(LumiereSpacing.S))
            Text("Notificações", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineMedium, fontFamily = FontFamily.Serif)
        }

        Spacer(modifier = Modifier.height(LumiereSpacing.L))

        // Master Toggle (Cartão)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(LumiereSpacing.M),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Notificações ativas", style = MaterialTheme.typography.titleMedium)
                    Text("Recebe alertas no Lumière", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = { notificationsViewModel.setEnabled(it) }
                )
            }
        }

        if (!enabled) {
            Text(
                "Notificações desativadas. Ative para receber alertas.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = LumiereSpacing.S)
            )
        }

        Spacer(modifier = Modifier.height(LumiereSpacing.L))

        // Sub-menus
        Text("ALERTAS", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(LumiereSpacing.S))

        Column(modifier = Modifier.alpha(subAlpha)) {
            SettingsItem(
                icon = Icons.Outlined.Notifications,
                title = "Som",
                subtitle = "Tocar som ao receber alerta",
                onClick = if (enabled) ({ notificationsViewModel.setSoundEnabled(!soundEnabled) }) else null,
                trailingContent = { Switch(checked = soundEnabled, onCheckedChange = { notificationsViewModel.setSoundEnabled(it) }, enabled = enabled) }
            )
            SettingsItem(
                icon = Icons.Outlined.Notifications,
                title = "Vibração",
                subtitle = "Vibrar ao receber alerta",
                onClick = if (enabled) ({ notificationsViewModel.setVibrationEnabled(!vibrationEnabled) }) else null,
                trailingContent = { Switch(checked = vibrationEnabled, onCheckedChange = { notificationsViewModel.setVibrationEnabled(it) }, enabled = enabled) }
            )
        }
    }
}
