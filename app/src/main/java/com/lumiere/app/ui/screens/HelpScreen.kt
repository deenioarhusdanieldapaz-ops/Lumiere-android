package com.lumiere.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lumiere.app.ui.theme.LumiereSpacing

@Composable
fun HelpScreen(onBack: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = LumiereSpacing.M), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = MaterialTheme.colorScheme.primary) }
            Spacer(modifier = Modifier.width(LumiereSpacing.S))
            Text("Ajuda", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineMedium, fontFamily = FontFamily.Serif)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.Search, contentDescription = "Pesquisar", tint = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(LumiereSpacing.M))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Pesquisar ajuda...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = LumiereSpacing.M),
            shape = RoundedCornerShape(50),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(LumiereSpacing.L))

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = LumiereSpacing.M).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(50)).padding(4.dp)) {
            Box(modifier = Modifier.weight(1f).background(if (selectedTab == 0) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(50)).clickable { selectedTab = 0 }.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                Text("Artigos", color = if (selectedTab == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            }
            Box(modifier = Modifier.weight(1f).background(if (selectedTab == 1) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(50)).clickable { selectedTab = 1 }.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                Text("Tutoriais", color = if (selectedTab == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(LumiereSpacing.L))

        Column(modifier = Modifier.padding(horizontal = LumiereSpacing.M), verticalArrangement = Arrangement.spacedBy(LumiereSpacing.S)) {
            Row(horizontalArrangement = Arrangement.spacedBy(LumiereSpacing.S)) {
                HelpCategoryCard("Primeiros Passos", Icons.Outlined.RocketLaunch, Modifier.weight(1f))
                HelpCategoryCard("Módulos", Icons.Outlined.MenuBook, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(LumiereSpacing.S)) {
                HelpCategoryCard("Conta", Icons.Outlined.Person, Modifier.weight(1f))
                HelpCategoryCard("Sistema", Icons.Outlined.Settings, Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(LumiereSpacing.L))
        Text("Artigos Recomendados", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = LumiereSpacing.M))
        Spacer(modifier = Modifier.height(LumiereSpacing.S))

        HelpArticleCard("Como usar o Centro de Aprovações", "2 min de leitura")
        HelpArticleCard("Dicas para uma vida mais produtiva", "3 min de leitura")
        
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun HelpCategoryCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp), modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = Color(0xFFA67C2E), modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun HelpArticleCard(title: String, readTime: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = LumiereSpacing.M, vertical = 4.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(60.dp).background(Color(0xFFA67C2E).copy(alpha = 0.2f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Article, contentDescription = null, tint = Color(0xFFA67C2E))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(readTime, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
