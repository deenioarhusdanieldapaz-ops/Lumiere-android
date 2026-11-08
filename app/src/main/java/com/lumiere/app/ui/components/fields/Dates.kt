package com.lumiere.app.ui.components.fields

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
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
fun LumiereDatePicker(
    selectedDate: Long?,
    onDateSelected: (Long?) -> Unit,
    label: String = "Data"
) {
    var showDialog by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick = { showDialog = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.DateRange, contentDescription = null, tint = LumiereGold)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = selectedDate?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it)) } ?: label,
            color = LumiereGold,
            fontSize = 14.sp
        )
    }

    if (showDialog) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                    showDialog = false
                }) { Text("OK", color = LumiereGold) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
