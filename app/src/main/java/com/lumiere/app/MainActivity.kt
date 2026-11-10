package com.lumiere.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lumiere.app.data.local.database.AppDatabase
import com.lumiere.app.data.repository.HabitRepository
import com.lumiere.app.data.repository.TaskRepository
import com.lumiere.app.ui.screens.ComponentGalleryScreen
import com.lumiere.app.ui.screens.HabitScreen
import com.lumiere.app.ui.screens.TaskScreen
import com.lumiere.app.ui.theme.LumiereDarkGray
import com.lumiere.app.ui.theme.LumiereGold
import com.lumiere.app.ui.theme.LumiereTheme
import com.lumiere.app.ui.viewmodel.HabitViewModel
import com.lumiere.app.ui.viewmodel.HabitViewModelFactory
import com.lumiere.app.ui.viewmodel.TaskViewModel
import com.lumiere.app.ui.viewmodel.TaskViewModelFactory
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Tasks : Screen("tasks", "M01 — Tarefas", Icons.Default.CheckCircle)
    object Habits : Screen("habits", "M02 — Hábitos", Icons.Default.Star)
    object Gallery : Screen("gallery", "🧪 Galeria (Dev)", Icons.Default.Info)
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
            LumiereTheme {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(drawerContainerColor = LumiereDarkGray) {
                            Spacer(modifier = Modifier.height(32.dp))
                            Text(
                                "LUMIÈRE",
                                color = LumiereGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                modifier = Modifier.padding(16.dp)
                            )
                            Divider(color = Color.DarkGray)
                            Spacer(modifier = Modifier.height(16.dp))

                            val items = listOf(Screen.Tasks, Screen.Habits, Screen.Gallery)
                            items.forEach { screen ->
                                NavigationDrawerItem(
                                    label = { Text(screen.label, color = if (currentRoute == screen.route) LumiereGold else Color.White) },
                                    selected = currentRoute == screen.route,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                        scope.launch { drawerState.close() }
                                    },
                                    icon = { Icon(screen.icon, contentDescription = null, tint = if (currentRoute == screen.route) LumiereGold else Color.Gray) }
                                )
                            }
                        }
                    },
                    gesturesEnabled = true
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Tasks.route,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable(Screen.Tasks.route) {
                            TaskScreen(
                                viewModel = taskViewModel,
                                onOpenDrawer = { scope.launch { drawerState.open() } }
                            )
                        }
                        composable(Screen.Habits.route) {
                            HabitScreen(
                                viewModel = habitViewModel,
                                onOpenDrawer = { scope.launch { drawerState.open() } }
                            )
                        }
                        composable(Screen.Gallery.route) {
                            ComponentGalleryScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
