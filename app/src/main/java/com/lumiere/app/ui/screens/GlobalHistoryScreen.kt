package com.lumiere.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumiere.app.ui.theme.LumiereSpacing

@Composable
fun GlobalHistoryScreen(onBack: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = LumiereSpacing.M), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = MaterialTheme.colorScheme.primary) }
            Spacer(modifier = Modifier.width(LumiereSpacing.S))
            Text("Histórico Global", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineMedium, fontFamily = FontFamily.Serif)
        }

        Spacer(modifier = Modifier.height(LumiereSpacing.M))

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = LumiereSpacing.M).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(50)).padding(4.dp)) {
            val tabs = listOf("Tudo", "Módulos", "Sistema")
            tabs.forEachIndexed { index, title ->
                Box(modifier = Modifier.weight(1f).background(if (selectedTab == index) Color(0xFFA67C2E) else Color.Transparent, RoundedCornerShape(50)).clickable { selectedTab = index }.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                    Text(title, color = if (selectedTab == index) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(LumiereSpacing.L))

        Text("Hoje", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = LumiereSpacing.M))
        Spacer(modifier = Modifier.height(LumiereSpacing.S))
        
        TimelineItem(icon = Icons.Default.Check, iconColor = Color(0xFF4CAF50), iconBg = Color(0xFFE8F5E9), title = "Tarefa M01 concluída", time = "09:30", badgeText = "Sucesso", badgeBg = Color(0xFFE8F5E9), badgeTextColor = Color(0xFF2E7D32), isLast = false)
        TimelineItem(icon = Icons.Outlined.History, iconColor = Color(0xFFA67C2E), iconBg = Color(0xFFFFF8E1), title = "Aprovação no Centro", time = "10:15", badgeText = "Pendente", badgeBg = Color(0xFFFFF8E1), badgeTextColor = Color(0xFFA67C2E), isLast = false)
        TimelineItem(icon = Icons.Outlined.MenuBook, iconColor = Color(0xFF1976D2), iconBg = Color(0xFFE3F2FD), title = "Novo artigo lido", time = "11:20", badgeText = "Informação", badgeBg = Color(0xFFE3F2FD), badgeTextColor = Color(0xFF1976D2), isLast = true)

        Spacer(modifier = Modifier.height(LumiereSpacing.L))

        Text("Ontem", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = LumiereSpacing.M))
        Spacer(modifier = Modifier.height(LumiereSpacing.S))
        
        TimelineItem(icon = Icons.Default.Check, iconColor = Color(0xFF4CAF50), iconBg = Color(0xFFE8F5E9), title = "Meta M03 atualizada", time = "18:45", badgeText = "Atualização", badgeBg = Color(0xFFE3F2FD), badgeTextColor = Color(0xFF1976D2), isLast = true)
        
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun TimelineItem(icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color, iconBg: Color, title: String, time: String, badgeText: String, badgeBg: Color, badgeTextColor: Color, isLast: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = LumiereSpacing.M)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(40.dp)) {
            Box(modifier = Modifier.size(32.dp).background(iconBg, CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
            }
            if (!isLast) {
                Box(modifier = Modifier.width(2.dp).height(50.dp).background(MaterialTheme.colorScheme.outlineVariant))
            }
        }
        
        Spacer(modifier = Modifier.width(LumiereSpacing.S))
        
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f).padding(bottom = 12.dp)) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Text(time, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box(modifier = Modifier.background(badgeBg, RoundedCornerShape(50)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(badgeText, color = badgeTextColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
