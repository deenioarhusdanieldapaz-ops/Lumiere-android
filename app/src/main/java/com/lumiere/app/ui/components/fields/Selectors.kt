package com.lumiere.app.ui.components.fields

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lumiere.app.ui.theme.LumiereGold

@Composable
fun LumiereDropdown(
    label: String,
    options: List<String>,
    selected: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selected ?: label, color = LumiereGold)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = LumiereGold)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, color = MaterialTheme.colorScheme.onSurface) },
                    onClick = { onSelect(option); expanded = false }
                )
            }
        }
    }
}

@Composable
fun LumiereSegmentedControl(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        options.forEach { option ->
            FilterChip(
                selected = selected == option,
                onClick = { onSelect(option) },
                label = { Text(option) }
            )
        }
    }
}

@Composable
fun LumiereRadioGroup(
    options: List<String>,
    selected: String?,
    onSelect: (String) -> Unit
) {
    Column {
        options.forEach { option ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                RadioButton(
                    selected = selected == option,
                    onClick = { onSelect(option) }
                )
                Text(option, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun LumiereCheckboxGroup(
    options: List<String>,
    selectedOptions: List<String>,
    onToggle: (String) -> Unit
) {
    Column {
        options.forEach { option ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(
                    checked = selectedOptions.contains(option),
                    onCheckedChange = { onToggle(option) }
                )
                Text(option, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}
