package com.lumiere.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
            // Header: Wizard ou Título Normal
            if (mode == FormMode.WIZARD && data.stepInfo != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                    Text(data.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .border(1.dp, gold, RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("Passo ${data.stepInfo.currentStep} de ${data.stepInfo.totalSteps}", style = MaterialTheme.typography.labelSmall, color = gold)
                    }
                }
                Spacer(modifier = Modifier.height(LumiereSpacing.S))
                // Barra de Progresso dos Passos
                LinearProgressIndicator(
                    progress = data.stepInfo.currentStep.toFloat() / data.stepInfo.totalSteps,
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = gold,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.height(LumiereSpacing.M))
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(data.title, style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(LumiereSpacing.S))
            }

            // Campos
            data.fields.forEach { field ->
                Column(modifier = Modifier.padding(bottom = LumiereSpacing.M)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (field.isRequired) "${field.label} *" else field.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (field.isRequired) gold else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        field.hint?.let {
                            Icon(Icons.Default.Lightbulb, contentDescription = "Dica", tint = gold, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    OutlinedTextField(
                        value = formValues[field.id] ?: "",
                        onValueChange = { formValues[field.id] = it },
                        placeholder = { Text(field.placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                        isError = field.errorMessage != null,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(LumiereRadius.S),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = gold,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            errorBorderColor = MaterialTheme.colorScheme.error,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        trailingIcon = field.maxLength?.let {
                            {
                                Text("${(formValues[field.id]?.length ?: 0)}/$it", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    )
                    field.errorMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            // Rascunho
            if (data.hasDraft) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = LumiereSpacing.S)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Lumiere.colors.success, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rascunho guardado às ${data.draftTime ?: ""}", color = Lumiere.colors.success, style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(LumiereSpacing.L))

            // Botões
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(LumiereRadius.S),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = { onSubmit(formValues.toMap()) },
                    shape = RoundedCornerShape(LumiereRadius.S),
                    colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black)
                ) {
                    Text("Guardar", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }

    when (mode) {
        FormMode.QUICK, FormMode.DESTRUCTIVE -> {
            ModalBottomSheet(
                onDismissRequest = onDismiss,
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = LumiereRadius.L, topEnd = LumiereRadius.L)
            ) { content() }
        }
        FormMode.FULL, FormMode.WIZARD -> {
            Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
                Box(modifier = Modifier.padding(padding)) { content() }
            }
        }
    }
}
