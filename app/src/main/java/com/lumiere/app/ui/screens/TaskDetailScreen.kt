package com.lumiere.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lumiere.app.data.local.entity.Task
import com.lumiere.app.ui.theme.LumiereRadius
import com.lumiere.app.ui.theme.LumiereSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    task: Task,
    onBack: () -> Unit,
    onSave: (Task) -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description ?: "") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Editar tarefa", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar") }
                },
                actions = {
                    IconButton(onClick = { onSave(task.copy(title = title, description = description)) }) {
                        Icon(Icons.Default.Check, contentDescription = "Guardar", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().padding(LumiereSpacing.M)) {
                Button(
                    onClick = { onSave(task.copy(title = title, description = description)) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black),
                    shape = RoundedCornerShape(LumiereRadius.M)
                ) {
                    Text("Guardar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = LumiereSpacing.M)
        ) {
            // Secção 1: Informação
            SectionHeader(icon = Icons.Default.Info, title = "Informação")
            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text("Título") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                shape = RoundedCornerShape(LumiereRadius.S),
                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surface)
            )
            OutlinedTextField(
                value = description, onValueChange = { description = it },
                label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(LumiereRadius.S),
                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surface)
            )

            Spacer(modifier = Modifier.height(LumiereSpacing.M))

            // Secção 2: Planeamento
            SectionHeader(icon = Icons.Default.CalendarToday, title = "Planeamento")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DetailRow("Data de início", "Hoje, 09:00")
                DetailRow("Prazo", "Hoje, 18:00")
            }
            Spacer(modifier = Modifier.height(8.dp))
            DetailRow("Duração estimada", "45 min")

            Spacer(modifier = Modifier.height(LumiereSpacing.M))

            // Secção 3: Organização
            SectionHeader(icon = Icons.Default.Star, title = "Organização")
            Text("Prioridade", style = MaterialTheme.typography.bodySmall)
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = false, onClick = {}, label = { Text("Baixa") })
                FilterChip(selected = true, onClick = {}, label = { Text("Média") })
                FilterChip(selected = false, onClick = {}, label = { Text("Alta") })
            }
            
            Text("Tags", style = MaterialTheme.typography.bodySmall)
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SuggestionChip(onClick = {}, label = { Text("urgente") })
                SuggestionChip(onClick = {}, label = { Text("equipa") })
                IconButton(onClick = {}) { Icon(Icons.Default.Add, contentDescription = null) }
            }

            Spacer(modifier = Modifier.height(LumiereSpacing.M))

            // Secção 4: Anexos
            SectionHeader(icon = Icons.Default.AttachFile, title = "Anexos")
            OutlinedButton(
                onClick = {}, modifier = Modifier.fillMaxWidth().height(48.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Adicionar anexo", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(LumiereSpacing.L))
        }
    }
}

@Composable
private fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
    }
}
