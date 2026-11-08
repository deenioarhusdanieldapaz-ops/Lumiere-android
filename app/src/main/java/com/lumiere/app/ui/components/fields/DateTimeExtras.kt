package com.lumiere.app.ui.components.fields

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumiere.app.ui.theme.LumiereGold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LumiereTimePicker(
    selectedTime: Pair<Int, Int>?,
    onTimeSelected: (Pair<Int, Int>?) -> Unit,
    label: String = "Hora"
) {
    var showDialog by remember { mutableStateOf(false) }
    val timeText = selectedTime?.let { String.format("%02d:%02d", it.first, it.second) } ?: label

    OutlinedButton(onClick = { showDialog = true }, modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.Default.Refresh, contentDescription = null, tint = LumiereGold, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(timeText, color = LumiereGold, fontSize = 14.sp)
    }

    if (showDialog) {
        val timeState = rememberTimePickerState(
            initialHour = selectedTime?.first ?: 12,
            initialMinute = selectedTime?.second ?: 0
        )
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Escolher Hora", color = LumiereGold) },
            text = { TimePicker(state = timeState) },
            confirmButton = {
                TextButton(onClick = {
                    onTimeSelected(Pair(timeState.hour, timeState.minute))
                    showDialog = false
                }) { Text("OK", color = LumiereGold) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LumiereDateTimeRange(
    startDate: Long?,
    endDate: Long?,
    onStartSelected: (Long?) -> Unit,
    onEndSelected: (Long?) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        var showStartDialog by remember { mutableStateOf(false) }
        var showEndDialog by remember { mutableStateOf(false) }

        OutlinedButton(onClick = { showStartDialog = true }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.DateRange, contentDescription = null, tint = LumiereGold, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Início: " + (startDate?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it)) } ?: "—"),
                color = LumiereGold, fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = { showEndDialog = true }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.DateRange, contentDescription = null, tint = LumiereGold, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Fim: " + (endDate?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it)) } ?: "—"),
                color = LumiereGold, fontSize = 12.sp
            )
        }

        if (showStartDialog) {
            val state = rememberDatePickerState(initialSelectedDateMillis = startDate)
            DatePickerDialog(
                onDismissRequest = { showStartDialog = false },
                confirmButton = {
                    TextButton(onClick = { onStartSelected(state.selectedDateMillis); showStartDialog = false }) {
                        Text("OK", color = LumiereGold)
                    }
                },
                dismissButton = { TextButton(onClick = { showStartDialog = false }) { Text("Cancelar") } }
            ) { DatePicker(state = state) }
        }

        if (showEndDialog) {
            val state = rememberDatePickerState(initialSelectedDateMillis = endDate)
            DatePickerDialog(
                onDismissRequest = { showEndDialog = false },
                confirmButton = {
                    TextButton(onClick = { onEndSelected(state.selectedDateMillis); showEndDialog = false }) {
                        Text("OK", color = LumiereGold)
                    }
                },
                dismissButton = { TextButton(onClick = { showEndDialog = false }) { Text("Cancelar") } }
            ) { DatePicker(state = state) }
        }
    }
}

@Composable
fun LumiereCountdownTimer(
    remainingSeconds: Int,
    isRunning: Boolean,
    onToggle: () -> Unit
) {
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val display = String.format("%02d:%02d", minutes, seconds)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(display, color = LumiereGold, fontSize = 32.sp)
            Button(
                onClick = onToggle,
                colors = ButtonDefaults.buttonColors(containerColor = LumiereGold)
            ) {
                Text(if (isRunning) "Pausar" else "Iniciar", color = androidx.compose.ui.graphics.Color.Black)
            }
        }
    }
}
