package com.lumiere.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumiere.app.ui.components.LumiereEmptyState
import com.lumiere.app.ui.components.MetricTile
import com.lumiere.app.ui.theme.LumiereSpacing
import com.lumiere.app.ui.viewmodel.HabitViewModel
import com.lumiere.app.ui.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    taskViewModel: TaskViewModel,
    habitViewModel: HabitViewModel,
    onOpenDrawer: () -> Unit
) {
    val tasks by taskViewModel.tasks.collectAsStateWithLifecycle()
    val habits by habitViewModel.habits.collectAsStateWithLifecycle()

    // Lógica simples (Mock) até o Core (Eng. 7) estar pronto
    val overdueTasks = tasks.filter { !it.isCompleted && it.dueDate != null && it.dueDate < System.currentTimeMillis() }
    val todayTasks = tasks.filter { !it.isCompleted && it.dueDate != null && it.dueDate >= System.currentTimeMillis() }
    val totalHabits = habits.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Início", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = LumiereSpacing.M),
            verticalArrangement = Arrangement.spacedBy(LumiereSpacing.M)
        ) {
            // 1. Saudação
            val dateStr = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("pt", "PT")).format(Date())
            Column(modifier = Modifier.padding(top = LumiereSpacing.M)) {
                Text("Olá, Proprietário", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.headlineMedium)
                Text(dateStr.replaceFirstChar { it.uppercase() }, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
            }

            // 2. AttentionCard (Avisos)
            if (overdueTasks.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(modifier = Modifier.padding(LumiereSpacing.M), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(LumiereSpacing.S))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Atenção Necessária", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall)
                            Text("Tens ${overdueTasks.size} tarefa(s) atrasada(s).", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // 3. FocusToday (Métricas)
            Text("Foco de Hoje", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = LumiereSpacing.S))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(LumiereSpacing.S)) {
                MetricTile(Modifier.weight(1f), "${todayTasks.size}", "Tarefas Hoje", MaterialTheme.colorScheme.primary)
                MetricTile(Modifier.weight(1f), "$totalHabits", "Hábitos Ativos", MaterialTheme.colorScheme.secondary)
            }

            // 4. InsightCard (Core Mock)
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = LumiereSpacing.S),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = MaterialTheme.shapes.medium,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(modifier = Modifier.padding(LumiereSpacing.M), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(LumiereSpacing.M))
                    Column {
                        Text("Insight Lumière", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (overdueTasks.isNotEmpty()) "Sugiro focares-te primeiro nas tarefas atrasadas para limpar o teu quadro mental."
                                   else "Tudo em ordem! Aproveita para adiantar algum hábito ou planear a semana.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // 5. Empty State Global (se não houver nada)
            if (tasks.isEmpty() && habits.isEmpty()) {
                Spacer(modifier = Modifier.height(LumiereSpacing.L))
                LumiereEmptyState(
                    icon = Icons.Default.Star,
                    title = "Dia Limpo",
                    description = "Não tens tarefas nem hábitos. Aproveita para descansar ou criar um novo objetivo.",
                    actionLabel = "Criar Tarefa",
                    onActionClick = { /* Futuro: Navegar para Tarefas */ }
                )
            }
            
            Spacer(modifier = Modifier.height(LumiereSpacing.L))
        }
    }
}
