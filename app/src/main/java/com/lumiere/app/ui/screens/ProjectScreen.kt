package com.lumiere.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumiere.app.ui.theme.LumiereRadius
import com.lumiere.app.ui.theme.LumiereSpacing
import com.lumiere.app.ui.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectScreen(viewModel: TaskViewModel, onBack: () -> Unit = {}) {
    val projects by viewModel.projects.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Projetos", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Novo Projeto */ }) {
                        Icon(Icons.Default.Add, contentDescription = "Novo", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = LumiereSpacing.M),
            verticalArrangement = Arrangement.spacedBy(LumiereSpacing.M)
        ) {
            item {
                Text("Organize as suas tarefas por projeto", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            // Dados mockados para simular a imagem
            item { ProjectCard("Trabalho", "12", "3 atrasadas • 5 hoje", 0.78f, Color(0xFFE05A5A), Icons.Default.Work) }
            item { ProjectCard("Pessoal", "8", "1 hoje", 0.45f, Color(0xFF3B82F6), Icons.Default.Home) }
            item { ProjectCard("Estudos", "5", "0 atrasadas", 0.82f, Color(0xFF2E7D32), Icons.Default.School) }
            item { ProjectCard("Sem projeto", "3", "1 atrasada", 0.33f, Color(0xFFA0A0A0), Icons.Default.Star) }

            item {
                Spacer(modifier = Modifier.height(LumiereSpacing.M))
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(LumiereRadius.M),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Icon(Icons.Default.Archive, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Arquivados", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun ProjectCard(name: String, count: String, subtitle: String, progress: Float, color: Color, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(LumiereRadius.M))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(LumiereRadius.M))
            .padding(vertical = LumiereSpacing.M),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(4.dp).height(60.dp).background(color, RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)))
        Spacer(modifier = Modifier.width(LumiereSpacing.M))
        
        Box(
            modifier = Modifier.size(48.dp).background(color.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color)
        }
        
        Spacer(modifier = Modifier.width(LumiereSpacing.M))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Text(count, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = if (subtitle.contains("atrasada")) color else MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = color,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
