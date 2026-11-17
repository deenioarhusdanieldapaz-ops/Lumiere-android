package com.lumiere.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumiere.app.ui.components.LumiereEmptyState
import com.lumiere.app.ui.theme.LumiereSpacing
import com.lumiere.app.ui.viewmodel.HabitViewModel
import com.lumiere.app.ui.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchScreen(
    taskViewModel: TaskViewModel,
    habitViewModel: HabitViewModel,
    onBack: () -> Unit
) {
    val tasks by taskViewModel.tasks.collectAsStateWithLifecycle()
    val habits by habitViewModel.habits.collectAsStateWithLifecycle()

    var query by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Tudo") }
    var showPinSheet by remember { mutableStateOf(false) }
    var protectedItemName by remember { mutableStateOf("") }

    val filters = listOf("Tudo", "Tarefas", "Hábitos", "Outros")

    // Lógica de Filtragem Real
    val filteredTasks = if (query.isBlank()) emptyList() else tasks.filter { it.title.contains(query, true) || (it.category?.contains(query, true) == true) }
    val filteredHabits = if (query.isBlank()) emptyList() else habits.filter { it.name.contains(query, true) || (it.category?.contains(query, true) == true) }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(horizontal = LumiereSpacing.M)) {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = LumiereSpacing.M), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Pesquisa Global", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.displaySmall, fontFamily = FontFamily.Serif)
                            Text("Encontre rapidamente o que precisa", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                
                // Barra de Pesquisa (Sticky)
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth().padding(bottom = LumiereSpacing.S),
                    placeholder = { Text("Pesquisar...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            TextButton(onClick = { query = "" }) { Text("Cancelar", color = MaterialTheme.colorScheme.primary) }
                        }
                    },
                    shape = RoundedCornerShape(50),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    singleLine = true
                )

                // Chips de Filtro
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = LumiereSpacing.S)) {
                    items(filters.size) { index ->
                        val filter = filters[index]
                        val isSelected = selectedFilter == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.Transparent,
                                selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        )
                    }
                }
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = LumiereSpacing.M),
            verticalArrangement = Arrangement.spacedBy(LumiereSpacing.M),
            contentPadding = PaddingValues(top = LumiereSpacing.M, bottom = LumiereSpacing.L)
        ) {
            if (query.isBlank()) {
                item {
                    // Histórico recente (placeholders por agora)
                    Text("PESQUISAS RECENTES", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(LumiereSpacing.M))
                    Text("Revisão Semanal", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge)
                    Text("Hábitos", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge)
                    Text("Notificações", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge)
                }
            } else if (filteredTasks.isEmpty() && filteredHabits.isEmpty()) {
                item {
                    LumiereEmptyState(
                        icon = Icons.Default.Search,
                        title = "Sem resultados",
                        description = "Nenhum resultado para '$query'. Tente outra palavra."
                    )
                }
            } else {
                // TAREFAS
                if (filteredTasks.isNotEmpty() && (selectedFilter == "Tudo" || selectedFilter == "Tarefas")) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = LumiereSpacing.S)) {
                            Text("TAREFAS (${filteredTasks.size})", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Divider(color = MaterialTheme.colorScheme.primary, thickness = 0.5.dp, modifier = Modifier.weight(1f))
                        }
                    }
                    items(filteredTasks) { task ->
                        val isProtected = task.title.contains("Protegida", true) || task.category == "Privado"
                        SearchResultCard(
                            title = task.title,
                            subtitle = task.category ?: "Tarefa",
                            icon = Icons.Outlined.CheckCircle,
                            isProtected = isProtected,
                            onClick = {
                                if (isProtected) {
                                    protectedItemName = task.title
                                    showPinSheet = true
                                }
                            }
                        )
                    }
                }

                // HÁBITOS
                if (filteredHabits.isNotEmpty() && (selectedFilter == "Tudo" || selectedFilter == "Hábitos")) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = LumiereSpacing.S)) {
                            Text("HÁBITOS (${filteredHabits.size})", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Divider(color = MaterialTheme.colorScheme.primary, thickness = 0.5.dp, modifier = Modifier.weight(1f))
                        }
                    }
                    items(filteredHabits) { habit ->
                        SearchResultCard(
                            title = habit.name,
                            subtitle = habit.category ?: "Hábito",
                            icon = Icons.Outlined.Spa,
                            isProtected = false,
                            onClick = { }
                        )
                    }
                }
            }
        }
    }

    // BottomSheet de PIN Placeholder
    if (showPinSheet) {
        ModalBottomSheet(onDismissRequest = { showPinSheet = false }, containerColor = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(LumiereSpacing.L).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(LumiereSpacing.M))
                Text("Informação Protegida", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineSmall, fontFamily = FontFamily.Serif)
                Text("Introduza o PIN para aceder a '$protectedItemName'.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(modifier = Modifier.height(LumiereSpacing.L))
                
                // Placeholder de teclado PIN
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(4) { Box(modifier = Modifier.size(16.dp).background(MaterialTheme.colorScheme.outlineVariant, CircleShape)) }
                }
                Spacer(modifier = Modifier.height(LumiereSpacing.L))
                Text("Teclado numérico será implementado na Fase de Segurança.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.height(LumiereSpacing.L))
            }
        }
    }
}

@Composable
fun SearchResultCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isProtected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, if (isProtected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(modifier = Modifier.padding(LumiereSpacing.M), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape), contentAlignment = Alignment.Center) {
                Icon(if (isProtected) Icons.Outlined.Lock else icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(LumiereSpacing.M))
            Column(modifier = Modifier.weight(1f)) {
                Text(if (isProtected) "Informação Protegida" else title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontFamily = FontFamily.Serif, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(if (isProtected) "Toque para abrir no módulo" else subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}
