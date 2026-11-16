package com.lumiere.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumiere.app.ui.theme.LumiereSpacing
import com.lumiere.app.ui.viewmodel.NotificationsViewModel

@Composable
fun AlertTypesScreen(viewModel: NotificationsViewModel, onBack: () -> Unit) {
    val m01 by viewModel.m01.collectAsStateWithLifecycle()
    val m02 by viewModel.m02.collectAsStateWithLifecycle()
    val m03 by viewModel.m03.collectAsStateWithLifecycle()
    val m04 by viewModel.m04.collectAsStateWithLifecycle()
    val m05 by viewModel.m05.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = LumiereSpacing.M)) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = MaterialTheme.colorScheme.primary) }
            Spacer(modifier = Modifier.width(LumiereSpacing.S))
            Text("Tipos de Alerta", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineMedium, fontFamily = FontFamily.Serif)
        }
        Spacer(modifier = Modifier.height(LumiereSpacing.L))
        
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth().padding(horizontal = LumiereSpacing.M), shape = RoundedCornerShape(12.dp)) {
            Column {
                ModuleSwitch("Tarefas (M01)", m01, viewModel::setM01)
                ModuleSwitch("Hábitos (M02)", m02, viewModel::setM02)
                ModuleSwitch("Metas (M03)", m03, viewModel::setM03)
                ModuleSwitch("Estudos (M04)", m04, viewModel::setM04)
                ModuleSwitch("Calendário (M05)", m05, viewModel::setM05, showDivider = false)
            }
        }
        Spacer(modifier = Modifier.height(LumiereSpacing.M))
        Text("Nota: alertas críticos permanecem ativos.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = LumiereSpacing.M))
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun ModuleSwitch(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, showDivider: Boolean = true) {
    Column {
        Row(modifier = Modifier.fillMaxWidth().padding(LumiereSpacing.M), verticalAlignment = Alignment.CenterVertically) {
            Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
        if (showDivider) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}
