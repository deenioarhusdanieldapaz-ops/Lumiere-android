package com.lumiere.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lumiere.app.data.local.database.AppDatabase
import com.lumiere.app.data.repository.HabitRepository
import com.lumiere.app.data.repository.TaskRepository
import com.lumiere.app.ui.screens.*
import com.lumiere.app.ui.theme.LumiereTheme
import com.lumiere.app.ui.theme.LumiereThemeMode
import com.lumiere.app.ui.viewmodel.HabitViewModel
import com.lumiere.app.ui.viewmodel.HabitViewModelFactory
import com.lumiere.app.ui.viewmodel.TaskViewModel
import com.lumiere.app.ui.viewmodel.TaskViewModelFactory

sealed class Screen(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Dashboard : Screen("dashboard", "Início", Icons.Filled.Home)
    object Tasks : Screen("tasks", "Tarefas", Icons.Filled.CheckCircle)
    object Habits : Screen("habits", "Hábitos", Icons.Filled.Star)
    object Settings : Screen("settings", "Mais", Icons.Filled.MoreHoriz)
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
            // Alterado para guardar o ThemeMode (0=System, 1=Light, 2=Dark)
            var themeModeInt by remember { mutableStateOf(prefs.getInt("theme_mode", 2)) }
            val themeMode = when(themeModeInt) {
                1 -> LumiereThemeMode.Light
                0 -> LumiereThemeMode.System
                else -> LumiereThemeMode.Dark
            }

            LumiereTheme(mode = themeMode) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val items = listOf(Screen.Dashboard, Screen.Tasks, Screen.Habits, Screen.Settings)

                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                            items.forEach { screen ->
                                NavigationBarItem(
                                    icon = { Icon(screen.icon, contentDescription = screen.label) },
                                    label = { Text(screen.label) },
                                    selected = currentRoute == screen.route,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(navController = navController, startDestination = Screen.Dashboard.route, modifier = Modifier.padding(innerPadding)) {
                        composable(Screen.Dashboard.route) {
                            DashboardScreen(
                                taskViewModel = taskViewModel,
                                habitViewModel = habitViewModel,
                                isDark = (themeMode == LumiereThemeMode.Dark),
                                onThemeToggle = {
                                    val newMode = if (themeMode == LumiereThemeMode.Dark) 1 else 2
                                    themeModeInt = newMode
                                    prefs.edit().putInt("theme_mode", newMode).apply()
                                },
                                onOpenDrawer = { }, // Podes ligar o Drawer aqui se quiseres
                                onNavigateToTasks = { navController.navigate(Screen.Tasks.route) },
                                onNavigateToHabits = { navController.navigate(Screen.Habits.route) }
                            )
                        }
                        composable(Screen.Tasks.route) { TaskScreen(viewModel = taskViewModel) }
                        composable(Screen.Habits.route) { HabitScreen(viewModel = habitViewModel) }
                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                themeMode = themeMode,
                                onThemeChange = { 
                                    themeModeInt = when(it) { LumiereThemeMode.Light -> 1; LumiereThemeMode.System -> 0; else -> 2 }
                                    prefs.edit().putInt("theme_mode", themeModeInt).apply()
                                },
                                onOpenDrawer = { }
                            )
                        }
                    }
                }
            }
        }
    }
}
