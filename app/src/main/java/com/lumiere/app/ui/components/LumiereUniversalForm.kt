package com.lumiere.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lumiere.app.ui.theme.Lumiere
import com.lumiere.app.ui.theme.LumiereRadius
import com.lumiere.app.ui.theme.LumiereSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LumiereUniversalForm(
    mode: FormMode,
    data: UniversalFormData,
    onDismiss: () -> Unit,
    onSubmit: (Map<String, String>) -> Unit
) {
    val formValues = remember { mutableStateMapOf<String, String>() }
    val gold = MaterialTheme.colorScheme.primary

    val content = @Composable {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LumiereSpacing.M)
                .verticalScroll(rememberScrollState())
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(data.title, style = MaterialTheme.typography.titleLarge)
                if (mode != FormMode.FULL) IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Fechar", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            Spacer(modifier = Modifier.height(LumiereSpacing.M))

            data.fields.forEach { field ->
                Column(modifier = Modifier.padding(bottom = LumiereSpacing.M)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (field.isRequired) "${field.label} *" else field.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (field.isRequired) gold else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        field.hint?.let { Icon(Icons.Default.Lightbulb, contentDescription = "Dica", tint = gold, modifier = Modifier.size(18.dp)) }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    when (field.type) {
                        FormFieldType.TEXT, FormFieldType.DATE -> {
                            OutlinedTextField(
                                value = formValues[field.id] ?: "",
                                onValueChange = { formValues[field.id] = it },
                                placeholder = { Text(field.placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                                isError = field.errorMessage != null,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(LumiereRadius.S),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = gold, unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                ),
                                trailingIcon = if (field.type == FormFieldType.DATE) { { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) } } else null
                            )
                        }
                        FormFieldType.DROPDOWN -> {
                            var expanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = formValues[field.id] ?: field.placeholder,
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(LumiereRadius.S),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = gold, unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    ),
                                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
                                )
                                Box(modifier = Modifier.matchParentSize().clickable { expanded = true })
                                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                    field.options.forEach { option ->
                                        DropdownMenuItem(text = { Text(option) }, onClick = { formValues[field.id] = option; expanded = false })
                                    }
                                }
                            }
                        }
                        FormFieldType.PRIORITY -> {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                field.options.forEach { option ->
                                    val isSelected = formValues[field.id] == option
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { formValues[field.id] = option },
                                        label = { Text(option, color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface) },
                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = gold)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(LumiereSpacing.L))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(LumiereRadius.S), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.width(LumiereSpacing.S))
                Button(onClick = { onSubmit(formValues.toMap()) }, shape = RoundedCornerShape(LumiereRadius.S), colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black)) {
                    Text("Guardar", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }

    when (mode) {
        FormMode.QUICK, FormMode.DESTRUCTIVE -> ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(topStart = LumiereRadius.L, topEnd = LumiereRadius.L)) { content() }
        FormMode.FULL, FormMode.WIZARD -> Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding -> Box(modifier = Modifier.padding(padding)) { content() } }
    }
}
