package com.lumiere.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.lumiere.app.ui.screens.*
import com.lumiere.app.ui.theme.LumiereTheme
import com.lumiere.app.ui.theme.LumiereThemeMode
import com.lumiere.app.ui.viewmodel.HabitViewModel
import com.lumiere.app.ui.viewmodel.HabitViewModelFactory
import com.lumiere.app.ui.viewmodel.TaskViewModel
import com.lumiere.app.ui.viewmodel.TaskViewModelFactory
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Dashboard : Screen("dashboard", "Início", Icons.Filled.Home)
    object Tasks : Screen("tasks", "Tarefas", Icons.Filled.CheckCircle)
    object Habits : Screen("habits", "Hábitos", Icons.Filled.Star)
    object Settings : Screen("settings", "Definições", Icons.Filled.Settings)
    object WeeklyReview : Screen("weekly_review", "Mais", Icons.Filled.MoreHoriz)
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
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                val bottomItems = listOf(Screen.Dashboard, Screen.Tasks, Screen.Habits, Screen.WeeklyReview)

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.surface) {
                            Spacer(modifier = Modifier.height(32.dp))
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("LUMIÈRE", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineMedium)
                            }
                            Divider(color = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(16.dp))

                            // Itens do Menu Lateral
                            listOf(Screen.Dashboard, Screen.Tasks, Screen.Habits, Screen.WeeklyReview, Screen.Settings).forEach { screen ->
                                NavigationDrawerItem(
                                    label = { Text(screen.label, color = if (currentRoute == screen.route) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) },
                                    selected = currentRoute == screen.route,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                        scope.launch { drawerState.close() }
                                    },
                                    icon = { Icon(screen.icon, contentDescription = null, tint = if (currentRoute == screen.route) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) }
                                )
                            }
                        }
                    }
                ) {
                    Scaffold(
                        containerColor = MaterialTheme.colorScheme.background,
                        bottomBar = {
                            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                                bottomItems.forEach { screen ->
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
                                        }
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
                                    onThemeToggle = { themeModeInt = if (themeMode == LumiereThemeMode.Dark) 1 else 2; prefs.edit().putInt("theme_mode", themeModeInt).apply() },
                                    onOpenDrawer = { scope.launch { drawerState.open() } },
                                    onNavigateToTasks = { navController.navigate(Screen.Tasks.route) },
                                    onNavigateToHabits = { navController.navigate(Screen.Habits.route) }
                                )
                            }
                            composable(Screen.Tasks.route) { TaskScreen(viewModel = taskViewModel, onOpenDrawer = { scope.launch { drawerState.open() } }) }
                            composable(Screen.Habits.route) { HabitScreen(viewModel = habitViewModel, onOpenDrawer = { scope.launch { drawerState.open() } }) }
                            composable(Screen.WeeklyReview.route) {
                                WeeklyReviewScreen(taskViewModel = taskViewModel, habitViewModel = habitViewModel, isDark = (themeMode == LumiereThemeMode.Dark))
                            }
                            composable(Screen.Settings.route) {
                                SettingsScreen(
                                    themeMode = themeMode,
                                    isDark = (themeMode == LumiereThemeMode.Dark),
                                    onThemeChange = { themeModeInt = when(it) { LumiereThemeMode.Light -> 1; LumiereThemeMode.System -> 0; else -> 2 }; prefs.edit().putInt("theme_mode", themeModeInt).apply() },
                                    onOpenDrawer = { scope.launch { drawerState.open() } }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
