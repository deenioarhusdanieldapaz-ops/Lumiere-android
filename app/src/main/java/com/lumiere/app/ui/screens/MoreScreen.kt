package com.lumiere.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.lumiere.app.ui.theme.LumiereSpacing

data class MoreItem(val title: String, val subtitle: String, val icon: ImageVector, val route: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onNavigateToSearch: () -> Unit,
    onNavigateToReview: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToApprovals: () -> Unit
) {
    val items = listOf(
        MoreItem("Pesquisa Global", "Encontre tudo", Icons.Outlined.Search, "search"),
        MoreItem("Revisão Semanal", "O seu progresso", Icons.Outlined.BarChart, "review"),
        MoreItem("Notificações", "Alertas e resumos", Icons.Outlined.Notifications, "notifications"),
        MoreItem("Aprovações", "Decisões pendentes", Icons.Outlined.CheckCircle, "approvals"),
        MoreItem("Metas (Brevemente)", "Módulo M03", Icons.Outlined.EmojiEvents, "goals")
    )

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = LumiereSpacing.M)) {
        Spacer(modifier = Modifier.height(32.dp))
        Text("Mais", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.displaySmall, fontFamily = FontFamily.Serif)
        Text("Ferramentas e módulos", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        
        Spacer(modifier = Modifier.height(LumiereSpacing.L))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(LumiereSpacing.M),
            verticalArrangement = Arrangement.spacedBy(LumiereSpacing.M)
        ) {
            items(items) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable {
                        when (item.route) {
                            "search" -> onNavigateToSearch()
                            "review" -> onNavigateToReview()
                            "notifications" -> onNavigateToNotifications()
                            "approvals" -> onNavigateToApprovals()
                        }
                    },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(LumiereSpacing.M)) {
                        Icon(item.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(LumiereSpacing.S))
                        Text(item.title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall)
                        Text(item.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
