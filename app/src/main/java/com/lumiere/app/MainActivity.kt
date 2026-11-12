package com.lumiere.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lumiere.app.data.local.database.AppDatabase
import com.lumiere.app.data.repository.HabitRepository
import com.lumiere.app.data.repository.TaskRepository
import com.lumiere.app.ui.screens.ComponentGalleryScreen
import com.lumiere.app.ui.screens.DashboardScreen
import com.lumiere.app.ui.screens.HabitScreen
import com.lumiere.app.ui.screens.TaskScreen
import com.lumiere.app.ui.theme.LumiereTheme
import com.lumiere.app.ui.theme.LumiereThemeMode
import com.lumiere.app.ui.viewmodel.HabitViewModel
import com.lumiere.app.ui.viewmodel.HabitViewModelFactory
import com.lumiere.app.ui.viewmodel.TaskViewModel
import com.lumiere.app.ui.viewmodel.TaskViewModelFactory

sealed class Screen(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Dashboard : Screen("dashboard", "Início", Icons.Filled.Home)
    object Focus : Screen("focus", "Foco", Icons.Outlined.CenterFocusStrong)
    object Progress : Screen("progress", "Progresso", Icons.Outlined.BarChart)
    object Today : Screen("today", "Hoje", Icons.Outlined.CalendarToday)
    object More : Screen("more", "Mais", Icons.Outlined.MoreHoriz)
    
    // Rotas reais (para não quebrar o código antigo)
    object Tasks : Screen("tasks", "Tarefas", Icons.Default.CheckCircle)
    object Habits : Screen("habits", "Hábitos", Icons.Default.Star)
    object Gallery : Screen("gallery", "Galeria", Icons.Default.Info)
}

class MainActivity : ComponentActivity() {

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val habitRepository by lazy { HabitRepository(database.habitDao(), database.habitCheckInDao(), database.habitExceptionDao()) }
    private val repository by lazy { TaskRepository(database.taskDao(), database.subtaskDao(), database.projectDao(), database.taskHistoryDao()) }

    private val habitViewModel: HabitViewModel by viewModels { HabitViewModelFactory(habitRepository) }
    private val taskViewModel: TaskViewModel by viewModels { TaskViewModelFactory(repository) }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val prefs = getSharedPreferences("lumiere_prefs", Context.MODE_PRIVATE)
            var isDark by remember { mutableStateOf(prefs.getBoolean("is_dark_theme", true)) }
            val themeMode = if (isDark) LumiereThemeMode.Dark else LumiereThemeMode.Light

            LumiereTheme(mode = themeMode) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val bottomItems = listOf(Screen.Dashboard, Screen.Focus, Screen.Progress, Screen.Today, Screen.More)

                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        // Barra Flutuante (Pílula)
                        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp, start = 16.dp, end = 16.dp), contentAlignment = Alignment.BottomCenter) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                
                                // Quick Actions (Ações Rápidas)
                                Card(
                                    shape = RoundedCornerShape(50),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                ) {
                                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Nova Tarefa", color = Color.White, style = MaterialTheme.typography.labelSmall)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text("|", color = Color.Gray)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Icon(Icons.Outlined.Spa, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Novo Hábito", color = Color.White, style = MaterialTheme.typography.labelSmall)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text("|", color = Color.Gray)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Novo Evento", color = Color.White, style = MaterialTheme.typography.labelSmall)
                                    }
                                }

                                // Bottom Nav Pílula
                                Card(
                                    shape = RoundedCornerShape(50),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                                    border = BorderStroke(0.5.dp, Color(0xFF333333))
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        bottomItems.forEach { screen ->
                                            val selected = currentRoute == screen.route
                                            IconButton(
                                                onClick = {
                                                    // Rotas mock, não navegam a sério por agora
                                                },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(screen.icon, contentDescription = null, tint = if (selected) MaterialTheme.colorScheme.primary else Color.Gray)
                                                    Text(screen.label, color = if (selected) MaterialTheme.colorScheme.primary else Color.Gray, style = MaterialTheme.typography.labelSmall)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Dashboard.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Dashboard.route) {
                            DashboardScreen(
                                taskViewModel = taskViewModel,
                                habitViewModel = habitViewModel,
                                isDark = isDark,
                                onThemeToggle = {
                                    isDark = !isDark
                                    prefs.edit().putBoolean("is_dark_theme", isDark).apply()
                                }
                            )
                        }
                        composable(Screen.Tasks.route) { TaskScreen(viewModel = taskViewModel) }
                        composable(Screen.Habits.route) { HabitScreen(viewModel = habitViewModel) }
                        composable(Screen.Gallery.route) { ComponentGalleryScreen(onBack = { }) }
                        
                        // Mock routes para não crashar
                        composable(Screen.Focus.route) { Text("Ecrã de Foco", modifier = Modifier.fillMaxSize(), color = Color.White) }
                        composable(Screen.Progress.route) { Text("Ecrã de Progresso", modifier = Modifier.fillMaxSize(), color = Color.White) }
                        composable(Screen.Today.route) { Text("Ecrã de Hoje", modifier = Modifier.fillMaxSize(), color = Color.White) }
                        composable(Screen.More.route) { Text("Ecrã de Mais", modifier = Modifier.fillMaxSize(), color = Color.White) }
                    }
                }
            }
        }
    }
}
