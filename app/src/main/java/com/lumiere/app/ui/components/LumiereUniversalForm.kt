package com.lumiere.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lumiere.app.ui.theme.LumiereIcons
import com.lumiere.app.ui.theme.LumiereRadius
import com.lumiere.app.ui.theme.LumiereSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LumiereUniversalForm(
    mode: FormMode,
    data: UniversalFormData,
    initialValues: Map<String, String> = emptyMap(),
    onDismiss: () -> Unit,
    onSubmit: (Map<String, String>) -> Unit
) {
    val formValues = remember { mutableStateMapOf<String, String>().apply { putAll(initialValues) } }
    var currentStep by remember { mutableIntStateOf(1) }
    val totalSteps = 3
    val gold = MaterialTheme.colorScheme.primary

    val currentFields = data.fields.filter { it.step == currentStep }
    val isLastStep = currentStep == totalSteps
    
    // Verifica se o passo atual tem os campos obrigatórios preenchidos
    val isStepValid = currentFields.filter { it.isRequired }.all { !formValues[it.id].isNullOrBlank() }

    val content = @Composable {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Cabeçalho do Wizard
            Column(modifier = Modifier.padding(LumiereSpacing.M)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(data.title, style = MaterialTheme.typography.headlineSmall, color = gold, fontWeight = FontWeight.Bold)
                    if (mode != FormMode.FULL) IconButton(onClick = onDismiss) { Icon(LumiereIcons.Close, contentDescription = "Fechar", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { currentStep.toFloat() / totalSteps },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = gold,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Passo $currentStep de $totalSteps", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Corpo do Formulário (Campos do Passo Atual)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = LumiereSpacing.M)
                    .verticalScroll(rememberScrollState())
            ) {
                currentFields.forEach { field ->
                    Column(modifier = Modifier.padding(bottom = LumiereSpacing.M)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = if (field.isRequired) "${field.label} *" else field.label,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (field.isRequired) gold else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (field.hint != null) {
                            Text(field.hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(4.dp))

                        when (field.type) {
                            FormFieldType.TEXT, FormFieldType.DATE, FormFieldType.DATETIME -> {
                                OutlinedTextField(
                                    value = formValues[field.id] ?: "",
                                    onValueChange = { if (field.maxLength == null || it.length <= field.maxLength) formValues[field.id] = it },
                                    placeholder = { Text(field.placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(LumiereRadius.S),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = gold, unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    trailingIcon = {
                                        when (field.type) {
                                            FormFieldType.DATE, FormFieldType.DATETIME -> Icon(LumiereIcons.Event, contentDescription = null, tint = gold)
                                            else -> null
                                        }
                                    },
                                    supportingText = if (field.maxLength != null) {
                                        { Text("${formValues[field.id]?.length ?: 0}/${field.maxLength}", modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.End) }
                                    } else null
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
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                        ),
                                        trailingIcon = { Icon(LumiereIcons.MoreVert, contentDescription = null, tint = gold) }
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
                            FormFieldType.ATTACHMENT -> {
                                OutlinedButton(
                                    onClick = { /* Seletor nativo */ },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(LumiereRadius.S),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                ) {
                                    Icon(LumiereIcons.Tag, contentDescription = null, tint = gold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Adicionar Anexo (Max 10)", color = gold)
                                }
                            }
                        }
                    }
                }
            }

            // Rodapé de Navegação
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(LumiereSpacing.M),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentStep > 1) {
                    OutlinedButton(
                        onClick = { currentStep-- },
                        shape = RoundedCornerShape(LumiereRadius.S),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text("Voltar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(LumiereRadius.S),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Button(
                    onClick = {
                        if (isLastStep) {
                            onSubmit(formValues.toMap())
                        } else {
                            currentStep++
                        }
                    },
                    enabled = isStepValid,
                    shape = RoundedCornerShape(LumiereRadius.S),
                    colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black)
                ) {
                    Text(if (isLastStep) "Guardar" else "Seguinte", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(if (isLastStep) LumiereIcons.CheckCircle else LumiereIcons.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }

    when (mode) {
        FormMode.QUICK, FormMode.DESTRUCTIVE -> ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(topStart = LumiereRadius.L, topEnd = LumiereRadius.L)) { 
            // Para o QuickAdd, mostramos apenas o Passo 1
            Column(modifier = Modifier.padding(LumiereSpacing.M)) {
                Text("Nova Tarefa Rápida", style = MaterialTheme.typography.titleLarge, color = gold)
                Spacer(modifier = Modifier.height(LumiereSpacing.M))
                data.fields.filter { it.step == 1 }.forEach { field ->
                     OutlinedTextField(
                        value = formValues[field.id] ?: "",
                        onValueChange = { formValues[field.id] = it },
                        placeholder = { Text(field.placeholder) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = LumiereSpacing.M),
                        shape = RoundedCornerShape(LumiereRadius.S)
                    )
                }
                Button(
                    onClick = { onSubmit(formValues.toMap()) }, 
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black)
                ) { Text("Guardar") }
            }
        }
        FormMode.FULL, FormMode.WIZARD -> Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding -> Box(modifier = Modifier.padding(padding)) { content() } }
    }
}
