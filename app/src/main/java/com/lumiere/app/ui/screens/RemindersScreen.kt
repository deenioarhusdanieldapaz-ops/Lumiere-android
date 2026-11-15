package com.lumiere.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.lumiere.app.ui.theme.LumiereSpacing

@Composable
fun RemindersScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Spacer(modifier = Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = LumiereSpacing.M)) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = MaterialTheme.colorScheme.primary) }
            Spacer(modifier = Modifier.width(LumiereSpacing.S))
            Text("Lembretes", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineMedium, fontFamily = FontFamily.Serif)
        }
        Spacer(modifier = Modifier.height(LumiereSpacing.L))

        Column(modifier = Modifier.padding(horizontal = LumiereSpacing.M), verticalArrangement = Arrangement.spacedBy(LumiereSpacing.M)) {
            ReminderCard(title = "Tarefas (M01)", defaultOption = "15 min antes")
            ReminderCard(title = "Calendário (M05)", defaultOption = "1 hora antes")
            ReminderCard(title = "Finanças (M06)", defaultOption = "1 dia antes")
        }
    }
}

@Composable
fun ReminderCard(title: String, defaultOption: String) {
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(defaultOption) }

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(LumiereSpacing.M)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(LumiereSpacing.S))
            Box {
                OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(selected, modifier = Modifier.weight(1f))
                    Text("▼")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    listOf("5 min antes", "15 min antes", "30 min antes", "1 hora antes", "1 dia antes").forEach { option ->
                        DropdownMenuItem(text = { Text(option) }, onClick = { selected = option; expanded = false })
                    }
                }
            }
        }
    }
}
