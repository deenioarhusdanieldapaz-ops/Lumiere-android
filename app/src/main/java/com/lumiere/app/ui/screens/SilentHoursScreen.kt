package com.lumiere.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumiere.app.ui.theme.LumiereSpacing
import com.lumiere.app.ui.viewmodel.NotificationsViewModel

@Composable
fun SilentHoursScreen(viewModel: NotificationsViewModel, onBack: () -> Unit) {
    val silenceEnabled by viewModel.silenceEnabled.collectAsStateWithLifecycle()
    val criticalAlerts by viewModel.criticalAlerts.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = LumiereSpacing.M)) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = MaterialTheme.colorScheme.primary) }
            Spacer(modifier = Modifier.width(LumiereSpacing.S))
            Text("Horário de Silêncio", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineMedium, fontFamily = FontFamily.Serif)
        }
        Spacer(modifier = Modifier.height(LumiereSpacing.L))
        
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth().padding(horizontal = LumiereSpacing.M), shape = RoundedCornerShape(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(LumiereSpacing.M), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Ativar Silêncio", style = MaterialTheme.typography.bodyLarge)
                    Text("Programado", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = silenceEnabled, onCheckedChange = viewModel::setSilenceEnabled)
            }
        }
        
        Spacer(modifier = Modifier.height(LumiereSpacing.M))
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth().padding(horizontal = LumiereSpacing.M), shape = RoundedCornerShape(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(LumiereSpacing.M), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Início", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("22:00", style = MaterialTheme.typography.headlineSmall)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Fim", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("07:30", style = MaterialTheme.typography.headlineSmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(LumiereSpacing.L))
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = LumiereSpacing.M), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("S", "T", "Q", "Q", "S", "S", "D").forEach { day ->
                FilterChip(selected = true, onClick = {}, label = { Text(day) }, modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(LumiereSpacing.L))
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth().padding(horizontal = LumiereSpacing.M), shape = RoundedCornerShape(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(LumiereSpacing.M), verticalAlignment = Alignment.CenterVertically) {
                Text("Permitir Alertas Críticos", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                Switch(checked = criticalAlerts, onCheckedChange = viewModel::setCriticalAlerts)
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
    }
}
