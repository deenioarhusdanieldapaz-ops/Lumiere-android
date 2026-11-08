package com.lumiere.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumiere.app.ui.utils.getCategoryIcon
import com.lumiere.app.data.local.entity.Habit
import com.lumiere.app.data.local.entity.HabitCheckIn
import com.lumiere.app.ui.theme.LumiereDarkGray
import com.lumiere.app.ui.theme.LumiereGold
import com.lumiere.app.ui.viewmodel.HabitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitScreen(viewModel: HabitViewModel, onOpenDrawer: () -> Unit = {}) {
    val habits by viewModel.habits.collectAsState()
    val allCheckIns by viewModel.allCheckIns.collectAsState()

    // Cálculo de métricas reais
    val cal = java.util.Calendar.getInstance()
    val startOfDay = cal.apply { set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0); set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0) }.timeInMillis
    val endOfDay = cal.apply { set(java.util.Calendar.HOUR_OF_DAY, 23); set(java.util.Calendar.MINUTE, 59); set(java.util.Calendar.SECOND, 59); set(java.util.Calendar.MILLISECOND, 999) }.timeInMillis
    
    val checkInsToday = allCheckIns.filter { it.timestamp in startOfDay..endOfDay }
    val completedToday = checkInsToday.size
    val totalHabits = habits.size
    val pendingToday = (totalHabits - completedToday).coerceAtLeast(0)
    val consistency = if (totalHabits > 0) (completedToday.toFloat() / totalHabits.toFloat()) * 100 else 0f

    // Dica #4: State Hoisting (Elevação de Estado)
    var showCreateSheet by remember { mutableStateOf(false) }
    var newHabitName by remember { mutableStateOf("") }
    var newHabitCategory by remember { mutableStateOf("") }
    var newHabitFrequency by remember { mutableStateOf("DAILY") }
    var newHabitTarget by remember { mutableStateOf("") }
    var newHabitUnit by remember { mutableStateOf("") }
    var selectedDays by remember { mutableStateOf(127) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("M02 — Hábitos", color = LumiereGold, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Pequenas ações, grandes conquistas", color = Color.Gray, fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = LumiereGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateSheet = true },
                containerColor = LumiereGold,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Novo Hábito")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()), // CORREÇÃO DO SCROLL
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cálculo do Streak (Sequência)
            val currentStreak = if (habits.isNotEmpty()) viewModel.calculateStreak(habits.first().id) else 0

            // HERO CARD
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = CardDefaults.cardColors(containerColor = LumiereDarkGray),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFF4A3F38))
            ) {
                Row(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                            CircularProgressIndicator(
                                progress = consistency / 100f,
                                modifier = Modifier.size(80.dp),
                                color = LumiereGold,
                                trackColor = Color.DarkGray,
                                strokeWidth = 8.dp
                            )
                            Text("${consistency.toInt()}%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Consistência", color = Color.Gray, fontSize = 11.sp)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = LumiereGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("$currentStreak dias", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Sequência atual", color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("$currentStreak dias", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Recorde pessoal", color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // INDICADORES COMPACTOS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricTile(Modifier.weight(1f), "$completedToday", "Concluídos", Color.Green)
                MetricTile(Modifier.weight(1f), "$pendingToday", "Pendentes", LumiereGold)
                MetricTile(Modifier.weight(1f), "${consistency.toInt()}%", "Média", Color(0xFF9C27B0))
                MetricTile(Modifier.weight(1f), "$completedToday/$totalHabits", "Metas", Color(0xFFFF9800))
            }

            // TÍTULO "HÁBITOS DE HOJE"
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Hábitos de Hoje", color = LumiereGold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Ver todos →", color = LumiereGold, fontSize = 12.sp)
            }

            // LISTA DE HÁBITOS OU ESTADO VAZIO
            if (habits.isEmpty()) {
                // ESTADO VAZIO PREMIUM
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(120.dp).background(LumiereGold.copy(alpha = 0.05f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = LumiereGold, modifier = Modifier.size(80.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text("Bem-vindo ao Módulo", color = Color.White, fontSize = 24.sp, textAlign = TextAlign.Center)
                    Text("Hábitos!", color = LumiereGold, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        "Comece agora a criar os seus hábitos e construa uma vida mais equilibrada.",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Button(
                        onClick = { showCreateSheet = true },
                        modifier = Modifier.fillMaxWidth(0.9f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LumiereGold),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Criar Meu Primeiro Hábito", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            } else {
                // LISTA REAL DE HÁBITOS
                habits.forEach { habit ->
                    // Dica #8: Usa a lista global checkInsToday (evita flickering)
                    val isCompletedToday = checkInsToday.any { it.habitId == habit.id }
                    HabitItem(
                        habit = habit,
                        isCompletedToday = isCompletedToday,
                        onCheckIn = {
                            viewModel.addCheckIn(com.lumiere.app.data.local.entity.HabitCheckIn(habitId = habit.id))
                        },
                        onDelete = { viewModel.deleteHabit(habit) }
                    )
                }
            }
            
            // Passo 1: Calendário Semanal
            Text("Calendário de Consistência", color = LumiereGold, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(top = 16.dp))
            HabitWeekStrip(checkIns = checkInsToday)

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // MODAL: CRIAR HÁBITO    // MODAL: CRIAR HÁBITO
    if (showCreateSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCreateSheet = false },
            containerColor = LumiereDarkGray
        ) {
            Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()).fillMaxWidth()) {
                Text("Novo Hábito", color = LumiereGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = newHabitName,
                    onValueChange = { newHabitName = it },
                    label = { Text("Nome do Hábito") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LumiereGold)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newHabitCategory,
                    onValueChange = { newHabitCategory = it },
                    label = { Text("Categoria (ex: Saúde)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LumiereGold)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text("Frequência", color = Color.Gray, fontSize = 12.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    FilterChip(selected = newHabitFrequency == "DAILY", onClick = { newHabitFrequency = "DAILY" }, label = { Text("Diário") })
                    FilterChip(selected = newHabitFrequency == "WEEKLY", onClick = { newHabitFrequency = "WEEKLY" }, label = { Text("Semanal") })
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newHabitTarget,
                        onValueChange = { newHabitTarget = it },
                        label = { Text("Meta (ex: 8)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LumiereGold)
                    )
                    OutlinedTextField(
                        value = newHabitUnit,
                        onValueChange = { newHabitUnit = it },
                        label = { Text("Unidade (ex: copos)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LumiereGold)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Dias da semana", color = Color.Gray, fontSize = 12.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    val days = listOf("S" to 1, "T" to 2, "Q" to 4, "Q" to 8, "S" to 16, "S" to 32, "D" to 64)
                    days.forEach { (label, bit) ->
                        val isSelected = (selectedDays and bit) != 0
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedDays = if (isSelected) selectedDays - bit else selectedDays + bit },
                            label = { Text(label, fontSize = 10.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        if (newHabitName.isNotBlank()) {
                            viewModel.addHabit(Habit(
                                name = newHabitName,
                                category = newHabitCategory.ifBlank { null },
                                frequencyType = newHabitFrequency,
                                weeklyDays = selectedDays,
                                targetValue = newHabitTarget.toIntOrNull(),
                                unit = newHabitUnit.ifBlank { null }
                            ))
                            // Limpar os campos (State Hoisting)
                            newHabitName = ""
                            newHabitCategory = ""
                            newHabitTarget = ""
                            newHabitUnit = ""
                            showCreateSheet = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LumiereGold)
                ) {
                    Text("Guardar Hábito", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun MetricTile(modifier: Modifier, value: String, label: String, color: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = LumiereDarkGray),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, color = Color.Gray, fontSize = 10.sp, maxLines = 1)
        }
    }
}

@Composable
fun HabitItem(habit: Habit, isCompletedToday: Boolean = false, onCheckIn: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = LumiereDarkGray),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícone (simplificado por agora)
            Box(
                modifier = Modifier.size(48.dp).background(Color(0xFF332A1D), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(getCategoryIcon(habit.category), contentDescription = null, tint = LumiereGold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(habit.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (habit.targetValue != null && habit.unit != null) {
                    Text("Meta: ${habit.targetValue} ${habit.unit}", color = Color.Gray, fontSize = 12.sp)
                }
            }
            
            // Menu de 3 pontinhos
            var showMenu by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Opções", tint = Color.Gray)
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.background(LumiereDarkGray)) {
                    DropdownMenuItem(
                        text = { Text("Eliminar", color = Color.Red) },
                        onClick = { onDelete(); showMenu = false }
                    )
                }
            }

            // Botão de Check-in (Estado Dinâmico)
            if (isCompletedToday) {
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cumprido", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                OutlinedButton(
                    onClick = onCheckIn,
                    border = BorderStroke(1.dp, LumiereGold),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = LumiereGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Registar", color = LumiereGold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun HabitWeekStrip(checkIns: List<HabitCheckIn>) {
    val days = listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom")
    val calendar = java.util.Calendar.getInstance()
    val today = calendar.get(java.util.Calendar.DAY_OF_WEEK)
    val todayIndex = if (today == 1) 6 else today - 2
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEachIndexed { index, day ->
            val isToday = index == todayIndex
            val hasCheckIn = checkIns.any { 
                val cal = java.util.Calendar.getInstance().apply { timeInMillis = it.timestamp }
                val ciDay = if (cal.get(java.util.Calendar.DAY_OF_WEEK) == 1) 6 else cal.get(java.util.Calendar.DAY_OF_WEEK) - 2
                ciDay == index 
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(day, color = Color.Gray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier.size(36.dp)
                        .background(
                            if (hasCheckIn) Color(0xFF2E7D32) else if (isToday) LumiereGold.copy(alpha=0.2f) else LumiereDarkGray,
                            CircleShape
                        )
                        .border(if (isToday) 1.dp else 0.dp, if (isToday) LumiereGold else Color.Transparent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (hasCheckIn) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    } else {
                        Text("${calendar.get(java.util.Calendar.DAY_OF_MONTH) - todayIndex + index}", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
