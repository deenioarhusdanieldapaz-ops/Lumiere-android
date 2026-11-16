package com.lumiere.app.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumiere.app.ui.theme.LumiereSpacing
import com.lumiere.app.ui.viewmodel.NotificationsViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SilentHoursScreen(viewModel: NotificationsViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val silenceEnabled by viewModel.silenceEnabled.collectAsStateWithLifecycle()
    val criticalAlerts by viewModel.criticalAlerts.collectAsStateWithLifecycle()

    // Estados para guardar as horas escolhidas
    var startHour by remember { mutableIntStateOf(22) }
    var startMinute by remember { mutableIntStateOf(0) }
    var endHour by remember { mutableIntStateOf(7) }
    var endMinute by remember { mutableIntStateOf(30) }

    // Estados para os dias da semana
    val allDays = listOf("S", "T", "Q", "Q", "S", "S", "D")
    var selectedDays by remember { mutableStateOf(allDays.toSet()) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = LumiereSpacing.M)) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = MaterialTheme.colorScheme.primary) }
            Spacer(modifier = Modifier.width(LumiereSpacing.S))
            Text("Horário de Silêncio", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineMedium, fontFamily = FontFamily.Serif)
        }
        Spacer(modifier = Modifier.height(LumiereSpacing.L))
        
        // Master Toggle
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
        
        // Cartão dos Horários (Clicáveis)
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth().padding(horizontal = LumiereSpacing.M), shape = RoundedCornerShape(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(LumiereSpacing.M), horizontalArrangement = Arrangement.SpaceBetween) {
                
                // Início
                Column(modifier = Modifier.weight(1f).clickable {
                    TimePickerDialog(context, { _, h, m ->
                        startHour = h; startMinute = m
                    }, startHour, startMinute, true).show()
                }) {
                    Text("Início", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(String.format(Locale.getDefault(), "%02d:%02d", startHour, startMinute), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                }
                
                // Fim
                Column(modifier = Modifier.weight(1f).clickable {
                    TimePickerDialog(context, { _, h, m ->
                        endHour = h; endMinute = m
                    }, endHour, endMinute, true).show()
                }, horizontalAlignment = Alignment.End) {
                    Text("Fim", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(String.format(Locale.getDefault(), "%02d:%02d", endHour, endMinute), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(LumiereSpacing.L))
        
        // Dias da Semana (Clicáveis)
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = LumiereSpacing.M), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            allDays.forEach { day ->
                val isSelected = selectedDays.contains(day)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedDays = if (isSelected) selectedDays - day else selectedDays + day
                    },
                    label = { Text(day) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
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
