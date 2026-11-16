package com.lumiere.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumiere.app.ui.theme.LumiereIcons
import com.lumiere.app.ui.theme.LumiereSpacing
import com.lumiere.app.ui.viewmodel.NotificationsViewModel

@Composable
fun NotificationSettingsScreen(
    notificationsViewModel: NotificationsViewModel,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val enabled by notificationsViewModel.enabled.collectAsStateWithLifecycle()
        val alphaDisabled = if (enabled) 1f else 0.4f

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()).padding(horizontal = LumiereSpacing.M)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = MaterialTheme.colorScheme.primary) }
            Spacer(modifier = Modifier.width(LumiereSpacing.S))
            Text("Notificações", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineMedium, fontFamily = FontFamily.Serif)
        }
        Spacer(modifier = Modifier.height(LumiereSpacing.L))

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(LumiereSpacing.M), verticalAlignment = Alignment.CenterVertically) {
                Icon(LumiereIcons.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(LumiereSpacing.M))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Notificações ativas", style = MaterialTheme.typography.titleMedium)
                    Text("Recebe alertas no Lumière", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = enabled, onCheckedChange = { notificationsViewModel.setEnabled(it) })
            }
        }

        if (!enabled) {
            Spacer(modifier = Modifier.height(LumiereSpacing.S))
            Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).padding(LumiereSpacing.M), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(LumiereSpacing.M))
                Text("Notificações desativadas. Ative para receber alertas.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(LumiereSpacing.L))
        Text("ALERTAS", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = LumiereSpacing.S))
        
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().alpha(alphaDisabled)) {
            Column {
                SettingsItem(icon = Icons.Outlined.Shield, title = "Aprovações", subtitle = "", onClick = { if(enabled) onNavigate("approvals") })
                SettingsItem(icon = Icons.Outlined.History, title = "Atividade e histórico", subtitle = "", onClick = { if(enabled) onNavigate("history") })
                SettingsItem(icon = LumiereIcons.Event, title = "Lembretes", subtitle = "", onClick = { if(enabled) onNavigate("reminders") }, showDivider = false)
            }
        }

        Spacer(modifier = Modifier.height(LumiereSpacing.L))
        Text("PERSONALIZAÇÃO", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = LumiereSpacing.S))
        
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().alpha(alphaDisabled)) {
            Column {
                SettingsItem(icon = LumiereIcons.Settings, title = "Tipos de alerta", subtitle = "", onClick = { if(enabled) onNavigate("types") })
                SettingsItem(
                    icon = Icons.Outlined.VolumeUp, title = "Som e vibração", subtitle = "", 
                    onClick = { if(enabled) onNavigate("sound") }
                )
                SettingsItem(icon = Icons.Outlined.DarkMode, title = "Horário de silêncio", subtitle = "", onClick = { if(enabled) onNavigate("silence") }, showDivider = false)
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
    }
}
