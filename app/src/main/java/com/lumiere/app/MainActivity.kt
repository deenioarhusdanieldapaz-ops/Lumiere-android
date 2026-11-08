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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumiere.app.data.local.database.AppDatabase
import com.lumiere.app.data.repository.HabitRepository
import com.lumiere.app.data.repository.TaskRepository
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

class MainActivity : ComponentActivity() {

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val habitRepository by lazy { HabitRepository(database.habitDao(), database.habitCheckInDao(), database.habitExceptionDao()) }
    private val repository by lazy { TaskRepository(database.taskDao(), database.subtaskDao(), database.projectDao(), database.taskHistoryDao()) }
    private val habitViewModel: HabitViewModel by viewModels { HabitViewModelFactory(habitRepository) }
    private val viewModel: TaskViewModel by viewModels {
        TaskViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LumiereTheme {
                var showHabits by remember { mutableStateOf(false) }
                var showGallery by remember { mutableStateOf(false) }
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val scope = rememberCoroutineScope()

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

                            NavigationDrawerItem(
                                label = { Text("M01 — Tarefas", color = if (!showHabits) LumiereGold else Color.White) },
                                selected = !showHabits,
                                onClick = { showHabits = false; scope.launch { drawerState.close() } },
                                icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = LumiereGold) }
                            )
                            NavigationDrawerItem(
                                label = { Text("M02 — Hábitos", color = if (showHabits) LumiereGold else Color.White) },
                                selected = showHabits,
                                onClick = { showHabits = true; showGallery = false; scope.launch { drawerState.close() } },
                                icon = { Icon(Icons.Default.Star, contentDescription = null, tint = LumiereGold) }
                            )
                            Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp))
                            NavigationDrawerItem(
                                label = { Text("🧪 Galeria (Dev)", color = if (showGallery) LumiereGold else Color.Gray) },
                                selected = showGallery,
                                onClick = { showGallery = true; showHabits = false; scope.launch { drawerState.close() } },
                                icon = { Icon(Icons.Default.Info, contentDescription = null, tint = if (showGallery) LumiereGold else Color.Gray) }
                            )
                        }
                    },
                    gesturesEnabled = true
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        if (showGallery) {
                            com.lumiere.app.ui.screens.ComponentGalleryScreen(
                                onBack = { showGallery = false }
                            )
                        } else if (showHabits) {
                            HabitScreen(
                                viewModel = habitViewModel,
                                onOpenDrawer = { scope.launch { drawerState.open() } }
                            )
                        } else {
                            TaskScreen(
                                viewModel = viewModel,
                                onOpenDrawer = { scope.launch { drawerState.open() } }
                            )
                        }
                    }
                }
            }
        }
    }
}
