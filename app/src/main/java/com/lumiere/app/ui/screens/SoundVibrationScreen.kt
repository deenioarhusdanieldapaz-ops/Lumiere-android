package com.lumiere.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
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
fun SoundVibrationScreen(viewModel: NotificationsViewModel, onBack: () -> Unit) {
    val soundEnabled by viewModel.soundEnabled.collectAsStateWithLifecycle()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsStateWithLifecycle()
    val volume by viewModel.volume.collectAsStateWithLifecycle()
    val tone by viewModel.tone.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = LumiereSpacing.M)) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = MaterialTheme.colorScheme.primary) }
            Spacer(modifier = Modifier.width(LumiereSpacing.S))
            Text("Som e Vibração", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineMedium, fontFamily = FontFamily.Serif)
        }
        Spacer(modifier = Modifier.height(LumiereSpacing.L))
        
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth().padding(horizontal = LumiereSpacing.M), shape = RoundedCornerShape(12.dp)) {
            Column {
                Row(modifier = Modifier.fillMaxWidth().padding(LumiereSpacing.M), verticalAlignment = Alignment.CenterVertically) {
                    Text("Som de Notificação", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = soundEnabled, onCheckedChange = viewModel::setSoundEnabled)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(modifier = Modifier.fillMaxWidth().padding(LumiereSpacing.M), verticalAlignment = Alignment.CenterVertically) {
                    Text("Vibração Tátil", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = vibrationEnabled, onCheckedChange = viewModel::setVibrationEnabled)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(LumiereSpacing.L))
        Text("Toque de Alerta", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(horizontal = LumiereSpacing.M, vertical = LumiereSpacing.S))
        
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth().padding(horizontal = LumiereSpacing.M), shape = RoundedCornerShape(12.dp)) {
            Column {
                listOf("Lumière Chime", "Pulso Suave", "Cristal").forEach { option ->
                    Row(modifier = Modifier.fillMaxWidth().selectable(selected = (tone == option), onClick = { viewModel.setTone(option) }).padding(LumiereSpacing.M), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = (tone == option), onClick = { viewModel.setTone(option) })
                        Spacer(modifier = Modifier.width(LumiereSpacing.S))
                        Text(option, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(LumiereSpacing.L))
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = LumiereSpacing.M), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Volume", style = MaterialTheme.typography.titleSmall)
            Text("${volume}%", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        }
        Slider(value = volume.toFloat(), onValueChange = { viewModel.setVolume(it.toInt()) }, valueRange = 0f..100f, modifier = Modifier.padding(horizontal = LumiereSpacing.M))
        Spacer(modifier = Modifier.height(48.dp))
    }
}
