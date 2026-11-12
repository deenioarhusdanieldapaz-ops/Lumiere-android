package com.lumiere.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Tasks : Screen("tasks")
    object Habits : Screen("habits")
    object Gallery : Screen("gallery")
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

                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        // Ações Rápidas Fixas ao Fundo (sem flutuar)
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp,
                            shadowElevation = 8.dp,
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).navigationBarsPadding(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { /* Abrir Nova Tarefa */ }) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Nova Tarefa", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                                }
                                Box(modifier = Modifier.width(1.dp).height(20.dp).background(MaterialTheme.colorScheme.outline))
                                
                                TextButton(onClick = { /* Abrir Novo Hábito */ }) {
                                    Icon(Icons.Outlined.Spa, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Novo Hábito", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                                }
                                Box(modifier = Modifier.width(1.dp).height(20.dp).background(MaterialTheme.colorScheme.outline))
                                
                                TextButton(onClick = { /* Abrir Novo Evento */ }) {
                                    Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Novo Evento", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
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
                                },
                                onNavigateToTasks = { navController.navigate(Screen.Tasks.route) },
                                onNavigateToHabits = { navController.navigate(Screen.Habits.route) }
                            )
                        }
                        composable(Screen.Tasks.route) {
                            TaskScreen(
                                viewModel = taskViewModel,
                                onOpenDrawer = { navController.popBackStack() }
                            )
                        }
                        composable(Screen.Habits.route) {
                            HabitScreen(
                                viewModel = habitViewModel,
                                onOpenDrawer = { navController.popBackStack() }
                            )
                        }
                        composable(Screen.Gallery.route) {
                            ComponentGalleryScreen(onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}
