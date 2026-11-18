package com.lumiere.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumiere.app.ui.theme.LumiereSpacing
import com.lumiere.app.ui.viewmodel.IntelligenceViewModel

@Composable
fun LifePlanningScreen(viewModel: IntelligenceViewModel, onBack: () -> Unit) {
    val lifeAreas by viewModel.lifeAreas.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = LumiereSpacing.M), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = MaterialTheme.colorScheme.primary) }
            Spacer(modifier = Modifier.width(LumiereSpacing.S))
            Text("Planeamento", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineMedium, fontFamily = FontFamily.Serif)
        }

        Spacer(modifier = Modifier.height(LumiereSpacing.L))

        Column(modifier = Modifier.padding(horizontal = LumiereSpacing.M), verticalArrangement = Arrangement.spacedBy(LumiereSpacing.S)) {
            // Linha 1
            Row(horizontalArrangement = Arrangement.spacedBy(LumiereSpacing.S)) {
                LifeAreaCard(lifeAreas.getOrNull(0), Icons.Outlined.Favorite, Modifier.weight(1f))
                LifeAreaCard(lifeAreas.getOrNull(1), Icons.Outlined.School, Modifier.weight(1f))
            }
            // Linha 2
            Row(horizontalArrangement = Arrangement.spacedBy(LumiereSpacing.S)) {
                LifeAreaCard(lifeAreas.getOrNull(2), Icons.Outlined.Bolt, Modifier.weight(1f))
                LifeAreaCard(lifeAreas.getOrNull(3), Icons.Outlined.AttachMoney, Modifier.weight(1f))
            }
            // Linha 3 (Card único largo)
            LifeAreaCard(lifeAreas.getOrNull(4), Icons.Outlined.Description, Modifier.fillMaxWidth())
        }

        Spacer(modifier = Modifier.height(LumiereSpacing.L))
        
        OutlinedButton(
            onClick = { },
            modifier = Modifier.fillMaxWidth().padding(horizontal = LumiereSpacing.M),
            shape = RoundedCornerShape(50)
        ) {
            Icon(Icons.Outlined.BarChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ver Plano Completo", color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun LifeAreaCard(area: com.lumiere.app.ui.viewmodel.LifeAreaProgress?, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    if (area == null) return
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(area.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = area.progress,
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(area.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
