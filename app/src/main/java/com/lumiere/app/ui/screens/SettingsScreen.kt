package com.lumiere.app.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumiere.app.BuildConfig
import com.lumiere.app.ui.theme.LumiereSpacing
import com.lumiere.app.ui.theme.LumiereThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeMode: LumiereThemeMode,
    onThemeChange: (LumiereThemeMode) -> Unit,
    onOpenDrawer: () -> Unit
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("lumiere_prefs", Context.MODE_PRIVATE)
    
    // Dados do utilizador (lidos do SharedPreferences, com fallback seguro)
    val userName = prefs.getString("user_name", "Proprietário") ?: "Proprietário"
    val userEmail = prefs.getString("user_email", "email@lumiere.app") ?: "email@lumiere.app"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A150B), MaterialTheme.colorScheme.background)
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = LumiereSpacing.M),
            verticalArrangement = Arrangement.spacedBy(LumiereSpacing.M)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // TopBar
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onOpenDrawer, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.LocalFireDepartment, contentDescription = "Menu", tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(LumiereSpacing.S))
                    Text("LUMIÈRE", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineMedium, fontFamily = FontFamily.Serif)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        Icon(Icons.Outlined.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                        Box(modifier = Modifier.size(14.dp).background(Color.Red, CircleShape), contentAlignment = Alignment.Center) {
                            Text("3", color = Color.White, fontSize = 8.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(LumiereSpacing.M))
                    Box(modifier = Modifier.size(36.dp).border(1.dp, MaterialTheme.colorScheme.primary, CircleShape).padding(2.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant))
                }
            }

            // Título
            Column(modifier = Modifier.padding(top = LumiereSpacing.M)) {
                Text("Configurações", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.displaySmall, fontFamily = FontFamily.Serif)
                Text("Personalize a sua experiência Lumière", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }

            // Cartão de Perfil
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Row(modifier = Modifier.padding(LumiereSpacing.M), verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(modifier = Modifier.size(60.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape))
                        Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp).background(MaterialTheme.colorScheme.surface, CircleShape).padding(2.dp))
                    }
                    Spacer(modifier = Modifier.width(LumiereSpacing.M))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(userName, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif)
                        Text(userEmail, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    OutlinedButton(
                        onClick = { /* TODO: Editar Perfil */ },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Editar", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Seletor de Aparência
            Text("APARÊNCIA", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.sp)
            Row(
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(50)).border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(50)).padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val options = listOf(
                    Triple("Escuro", Icons.Default.DarkMode, LumiereThemeMode.Dark),
                    Triple("Claro", Icons.Default.LightMode, LumiereThemeMode.Light),
                    Triple("Sistema", Icons.Default.Settings, LumiereThemeMode.System)
                )
                options.forEach { (label, icon, mode) ->
                    val isSelected = themeMode == mode
                    Row(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(50))
                            .background(if (isSelected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
                            .border(if (isSelected) 1.dp else 0.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(50))
                            .clickable { onThemeChange(mode) }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(icon, contentDescription = null, tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(label, color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // Secções de Definições
            SettingsCategory("PREFERÊNCIAS") {
                SettingsItem(Icons.Outlined.Notifications, "Notificações", "Alertas e lembretes personalizados")
                SettingsItem(Icons.Outlined.Tune, "Módulos e Funcionalidades", "Ative ou organize os módulos")
                SettingsItem(Icons.Outlined.Palette, "Tema e Estilo", "Personalize a aparência da aplicação")
            }

            SettingsCategory("PRIVACIDADE E SEGURANÇA") {
                SettingsItem(Icons.Outlined.Shield, "Privacidade dos Dados", "Controle os seus dados")
                SettingsItem(Icons.Outlined.Fingerprint, "Autenticação Biométrica", "Use a biometria para maior segurança")
                SettingsItem(Icons.Outlined.Lock, "Cópia de Segurança e Restauração", "Proteja os seus dados")
            }

            SettingsCategory("SISTEMA") {
                SettingsItem(Icons.Outlined.Info, "Sobre o Lumière", "Versão ${BuildConfig.VERSION_NAME} • Build ${BuildConfig.VERSION_CODE}")
                SettingsItem(Icons.Outlined.HelpOutline, "Ajuda e Suporte", "Obtenha ajuda e tire dúvidas")
                SettingsItem(Icons.Outlined.Description, "Termos e Condições", "Leia os termos de utilização")
            }

            // Ação Destrutiva
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = LumiereSpacing.S, bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Row(modifier = Modifier.padding(LumiereSpacing.M).clickable { /* TODO: Apagar Dados */ }, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(LumiereSpacing.M))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Apagar todos os dados", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
                        Text("Esta ação é irreversível", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun SettingsCategory(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.sp, modifier = Modifier.padding(top = LumiereSpacing.S))
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            content()
        }
    }
}

@Composable
fun SettingsItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(modifier = Modifier.fillMaxWidth().clickable { /* TODO: Navegar */ }.padding(LumiereSpacing.M), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(LumiereSpacing.M))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
