package com.lumiere.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.Switch
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumiere.app.ui.components.fields.*
import com.lumiere.app.ui.utils.LumiereCategories
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
    val completedToday = checkInsToday.map { it.habitId }.distinct().size
    val totalHabits = habits.size
    val pendingToday = (totalHabits - completedToday).coerceAtLeast(0)
    val consistency = if (totalHabits > 0) (completedToday.toFloat() / totalHabits.toFloat()) * 100 else 0f

    // Dica #4: State Hoisting (Elevação de Estado)
    var showCreateSheet by remember { mutableStateOf(false) }
    var newHabitName by remember { mutableStateOf("") }
    var newHabitCategory by remember { mutableStateOf("") }
    var newHabitDescription by remember { mutableStateOf("") }
    var newHabitNotes by remember { mutableStateOf("") }
    var newHabitFrequency by remember { mutableStateOf("DAILY") }
    var newHabitTarget by remember { mutableStateOf("") }
    var newHabitUnit by remember { mutableStateOf("") }
    var newHabitInterval by remember { mutableStateOf("1") }
    var restDaysState by remember { mutableIntStateOf(0) }
    var reminderEnabled by remember { mutableStateOf(false) }
    var durationMinutes by remember { mutableStateOf("") }
    var completionCriteria by remember { mutableStateOf("") }
    var isAdaptationPeriod by remember { mutableStateOf(false) }
    var selectedDays by remember { mutableStateOf(127) }
    var habitToEdit by remember { mutableStateOf<Habit?>(null) }
    var preferredTime by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var habitDetails by remember { mutableStateOf<Habit?>(null) }

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
                onClick = { 
                    habitToEdit = null
                    newHabitName = ""
                    newHabitCategory = ""
                    newHabitTarget = ""
                    newHabitUnit = ""
                    selectedDays = 127
                    preferredTime = null
                    showCreateSheet = true 
                },
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
                        onClick = { 
                    habitToEdit = null
                    newHabitName = ""
                    newHabitCategory = ""
                    newHabitTarget = ""
                    newHabitUnit = ""
                    selectedDays = 127
                    preferredTime = null
                    showCreateSheet = true 
                },
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
                    val failureToday = checkInsToday.any { it.habitId == habit.id && it.type == "FAIL" }
                    HabitItem(
                        habit = habit,
                        isCompletedToday = isCompletedToday,
                        failureToday = failureToday,
                        onCheckIn = {
                            val currentVal = viewModel.getTodayValueForHabit(habit.id, checkInsToday)
                            viewModel.addCheckInValue(habit.id, currentVal, habit.targetValue)
                        },
                        onDelete = { viewModel.deleteHabit(habit) },
                        onPause = { viewModel.pauseHabit(habit) },
                        onResume = { viewModel.resumeHabit(habit) },
                        onEdit = {
                            habitToEdit = habit
                            newHabitName = habit.name
                            newHabitCategory = habit.category ?: ""
                            newHabitDescription = habit.description ?: ""
                            newHabitNotes = habit.notes ?: ""
                            newHabitFrequency = habit.frequencyType
                            newHabitTarget = habit.targetValue?.toString() ?: ""
                            newHabitUnit = habit.unit ?: ""
                            selectedDays = habit.weeklyDays
                            newHabitInterval = habit.intervalDays.toString()
                            restDaysState = habit.restDays
                            preferredTime = habit.preferredTime?.let {
                                val parts = it.split(":")
                                if (parts.size == 2) Pair(parts[0].toIntOrNull() ?: 0, parts[1].toIntOrNull() ?: 0) else null
                            }
                            durationMinutes = habit.durationMinutes?.toString() ?: ""
                            completionCriteria = habit.completionCriteria ?: ""
                            isAdaptationPeriod = habit.isAdaptationPeriod
                            showCreateSheet = true
                        },
                        onFail = { viewModel.addFailureCheckIn(habit.id) },
                        todayValue = viewModel.getTodayValueForHabit(habit.id, checkInsToday),
                        onOpenDetails = { habitDetails = habit }
                    )
                }
            }
            
            // Passo 1: Calendário Semanal
            Text("Calendário de Consistência", color = LumiereGold, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(top = 16.dp))
            HabitWeekStrip(checkIns = checkInsToday)

            Spacer(modifier = Modifier.height(16.dp))
        }
    }


    // MODAL: Detalhes do Hábito
    if (habitDetails != null) {
        val habit = habitDetails!!
        val habitCheckIns by viewModel.getCheckInsForHabit(habit.id).collectAsState(initial = emptyList())
        
        ModalBottomSheet(onDismissRequest = { habitDetails = null }, containerColor = LumiereDarkGray) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Text(habit.name, color = LumiereGold, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                if (habit.category != null) {
                    Text(habit.category!!, color = Color.Gray, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Histórico de Check-ins", color = LumiereGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                
                if (habitCheckIns.isEmpty()) {
                    Text("Sem registos ainda.", color = Color.Gray, fontSize = 12.sp)
                } else {
                    habitCheckIns.take(10).forEach { ci ->
                        val date = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(ci.timestamp))
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(date, color = Color.Gray, fontSize = 12.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (ci.value != null) {
                                    Text("${ci.value} ${habit.unit ?: ""}", color = Color.White, fontSize = 12.sp)
                                } else {
                                    Text("✓", color = Color(0xFF2E7D32), fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = { viewModel.deleteCheckIn(ci) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Apagar", tint = Color.Red, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // MODAL: CRIAR HÁBITO
    if (habitDetails != null) {
        val habit = habitDetails!!
        val habitCheckIns by viewModel.getCheckInsForHabit(habit.id).collectAsState(initial = emptyList())
        
        ModalBottomSheet(onDismissRequest = { habitDetails = null }, containerColor = LumiereDarkGray) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Text(habit.name, color = LumiereGold, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                if (habit.category != null) {
                    Text(habit.category!!, color = Color.Gray, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Histórico de Check-ins", color = LumiereGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                
                if (habitCheckIns.isEmpty()) {
                    Text("Sem registos ainda.", color = Color.Gray, fontSize = 12.sp)
                } else {
                    habitCheckIns.take(10).forEach { ci ->
                        val date = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(ci.timestamp))
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(date, color = Color.Gray, fontSize = 12.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (ci.value != null) {
                                    Text("${ci.value} ${habit.unit ?: ""}", color = Color.White, fontSize = 12.sp)
                                } else {
                                    Text("✓", color = Color(0xFF2E7D32), fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = { viewModel.deleteCheckIn(ci) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Apagar", tint = Color.Red, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showCreateSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCreateSheet = false },
            containerColor = LumiereDarkGray
        ) {
            Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()).fillMaxWidth()) {
                Text(if (habitToEdit == null) "Novo Hábito" else "Editar Hábito", color = LumiereGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                // GAVETA 1: IDENTIFICAÇÃO
                var isIdentOpen by remember { mutableStateOf(true) }
                LumiereAccordion(title = "1. Identificação", isExpanded = isIdentOpen, onToggle = { isIdentOpen = !isIdentOpen }) {
                    LumiereTextField(value = newHabitName, onValueChange = { newHabitName = it }, label = "Nome do Hábito")
                    Spacer(modifier = Modifier.height(8.dp))
                    LumiereDropdown(
                        label = "Selecionar Categoria",
                        options = LumiereCategories.names,
                        selected = newHabitCategory.ifBlank { null },
                        onSelect = { newHabitCategory = it }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LumiereTextArea(value = newHabitDescription, onValueChange = { newHabitDescription = it }, label = "Detalhes (Descrição)")
                    Spacer(modifier = Modifier.height(8.dp))
                    LumiereTextArea(value = newHabitNotes, onValueChange = { newHabitNotes = it }, label = "Notas Pessoais")
                }

                // GAVETA 2: FREQUÊNCIA
                var isFreqOpen by remember { mutableStateOf(true) }
                LumiereAccordion(title = "2. Frequência", isExpanded = isFreqOpen, onToggle = { isFreqOpen = !isFreqOpen }) {
                    LumiereDropdown(
                        label = "Tipo de Frequência",
                        options = listOf("Diário", "Semanal", "Dias Escolhidos", "Intervalo"),
                        selected = when(newHabitFrequency) {
                            "DAILY" -> "Diário"
                            "WEEKLY" -> "Semanal"
                            "CUSTOM" -> "Dias Escolhidos"
                            "INTERVAL" -> "Intervalo"
                            else -> "Diário"
                        },
                        onSelect = { 
                            newHabitFrequency = when(it) {
                                "Diário" -> "DAILY"
                                "Semanal" -> "WEEKLY"
                                "Dias Escolhidos" -> "CUSTOM"
                                "Intervalo" -> "INTERVAL"
                                else -> "DAILY"
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (newHabitFrequency == "INTERVAL") {
                        LumiereNumberField(value = newHabitInterval, onValueChange = { newHabitInterval = it }, label = "A cada quantos dias?")
                    } else if (newHabitFrequency == "CUSTOM") {
                        val weekDays = listOf("Seg" to 1, "Ter" to 2, "Qua" to 4, "Qui" to 8, "Sex" to 16, "Sáb" to 32, "Dom" to 64)
                        LumiereCheckboxGroup(
                            options = weekDays.map { it.first },
                            selectedOptions = weekDays.filter { (selectedDays and it.second) != 0 }.map { it.first },
                            onToggle = { dayName ->
                                val bit = weekDays.find { it.first == dayName }?.second ?: 0
                                selectedDays = if ((selectedDays and bit) != 0) selectedDays - bit else selectedDays + bit
                            }
                        )
                    }
                }

                // GAVETA 3: METAS E DESCANSO
                var isMetaOpen by remember { mutableStateOf(false) }
                LumiereAccordion(title = "3. Metas e Descanso", isExpanded = isMetaOpen, onToggle = { isMetaOpen = !isMetaOpen }) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LumiereNumberField(value = newHabitTarget, onValueChange = { newHabitTarget = it }, label = "Meta", modifier = Modifier.weight(1f))
                        LumiereTextField(value = newHabitUnit, onValueChange = { newHabitUnit = it }, label = "Unidade", modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Dias de Descanso", color = Color.Gray, fontSize = 12.sp)
                    val weekDays = listOf("Seg" to 1, "Ter" to 2, "Qua" to 4, "Qui" to 8, "Sex" to 16, "Sáb" to 32, "Dom" to 64)
                    val restDaysList = weekDays.filter { (restDaysState and it.second) != 0 }.map { it.first }
                    LumiereCheckboxGroup(
                        options = weekDays.map { it.first },
                        selectedOptions = restDaysList,
                        onToggle = { dayName ->
                            val bit = weekDays.find { it.first == dayName }?.second ?: 0
                            restDaysState = if ((restDaysState and bit) != 0) restDaysState - bit else restDaysState + bit
                        }
                    )
                }

                // GAVETA 4: PREFERÊNCIAS
                var isPrefOpen by remember { mutableStateOf(false) }
                LumiereAccordion(title = "4. Preferências", isExpanded = isPrefOpen, onToggle = { isPrefOpen = !isPrefOpen }) {
                    LumiereTimePicker(
                        selectedTime = preferredTime,
                        onTimeSelected = { preferredTime = it },
                        label = "Horário Preferencial"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("Lembrete", color = Color.White, modifier = Modifier.weight(1f))
                        Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                    }
                }

                // GAVETA 5: DURAÇÃO E CRITÉRIOS
                var isCritOpen by remember { mutableStateOf(false) }
                LumiereAccordion(title = "5. Duração e Critérios", isExpanded = isCritOpen, onToggle = { isCritOpen = !isCritOpen }) {
                    LumiereNumberField(value = durationMinutes, onValueChange = { durationMinutes = it }, label = "Duração estimada (min)")
                    Spacer(modifier = Modifier.height(8.dp))
                    LumiereTextArea(value = completionCriteria, onValueChange = { completionCriteria = it }, label = "Critério de Conclusão (ex: 20 flexões)")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("Período de Adaptação (não quebra streak)", color = Color.White, modifier = Modifier.weight(1f), fontSize = 12.sp)
                        Switch(checked = isAdaptationPeriod, onCheckedChange = { isAdaptationPeriod = it })
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        if (newHabitName.isNotBlank()) {
                            val timeStr = preferredTime?.let { String.format("%02d:%02d", it.first, it.second) }
                            val habit = Habit(
                                name = newHabitName,
                                category = newHabitCategory.ifBlank { null },
                                description = newHabitDescription.ifBlank { null },
                                notes = newHabitNotes.ifBlank { null },
                                frequencyType = newHabitFrequency,
                                weeklyDays = selectedDays,
                                intervalDays = newHabitInterval.toIntOrNull() ?: 1,
                                restDays = restDaysState,
                                targetValue = newHabitTarget.toIntOrNull(),
                                unit = newHabitUnit.ifBlank { null },
                                preferredTime = timeStr,
                                durationMinutes = durationMinutes.toIntOrNull(),
                                completionCriteria = completionCriteria.ifBlank { null },
                                isAdaptationPeriod = isAdaptationPeriod
                            )
                            if (habitToEdit != null) {
                                viewModel.updateHabit(habitToEdit!!.copy(
                                    name = habit.name, category = habit.category, description = habit.description, notes = habit.notes, frequencyType = habit.frequencyType,
                                    weeklyDays = habit.weeklyDays, intervalDays = habit.intervalDays,
                                    targetValue = habit.targetValue, unit = habit.unit, preferredTime = habit.preferredTime
                                ))
                            } else {
                                viewModel.addHabit(habit)
                            }
                            habitToEdit = null
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
fun HabitItem(habit: Habit, isCompletedToday: Boolean = false, failureToday: Boolean = false, todayValue: Int = 0, onCheckIn: () -> Unit, onDelete: () -> Unit, onPause: () -> Unit, onResume: () -> Unit, onEdit: () -> Unit, onFail: () -> Unit, onOpenDetails: () -> Unit = {}) {
    val isPaused = habit.status == 1
    val isCompleted = isCompletedToday
    val hasFailed = failureToday
    
    val (iconColor, bgColor) = getCategoryColor(habit.category)

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = LumiereDarkGray),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF333333))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Ícone Colorido
            Box(
                modifier = Modifier.size(52.dp).background(bgColor, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(getCategoryIcon(habit.category), contentDescription = null, tint = iconColor, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))

            // Textos
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                    maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.clickable { onOpenDetails() }
                )
                if (habit.targetValue != null && habit.unit != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        val progressText = if (isCompletedToday) "${habit.targetValue}/${habit.targetValue} ${habit.unit}" else "$todayValue/${habit.targetValue} ${habit.unit}"
                        Text(progressText, color = if (isCompletedToday) Color(0xFF4CAF50) else Color.Gray, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color(0xFF444444)))
            Spacer(modifier = Modifier.width(8.dp))

            // Ações
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isPaused) {
                    OutlinedButton(onClick = onResume, border = BorderStroke(1.dp, Color.Gray), shape = RoundedCornerShape(20.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                        Text("Retomar", color = Color.Gray, fontSize = 11.sp)
                    }
                } else if (hasFailed) {
                    OutlinedButton(onClick = { }, border = BorderStroke(1.dp, Color.Red), shape = RoundedCornerShape(20.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                        Text("Falhou", color = Color.Red, fontSize = 11.sp)
                    }
                } else if (isCompleted) {
                    Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)), shape = RoundedCornerShape(20.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cumprido", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    OutlinedButton(onClick = onCheckIn, border = BorderStroke(1.dp, LumiereGold), shape = RoundedCornerShape(20.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = LumiereGold, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (habit.targetValue != null) "+1" else "Registar", color = LumiereGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                var showMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Opções", tint = Color.Gray)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.background(LumiereDarkGray)) {
                        DropdownMenuItem(text = { Text("Editar Hábito", color = LumiereGold) }, onClick = { onEdit(); showMenu = false })
                        DropdownMenuItem(text = { Text("Registar Falha", color = Color(0xFFFF9800)) }, onClick = { onFail(); showMenu = false })
                        if (habit.status == 0) {
                            DropdownMenuItem(text = { Text("Pausar", color = LumiereGold) }, onClick = { onPause(); showMenu = false })
                        } else {
                            DropdownMenuItem(text = { Text("Retomar", color = Color.Green) }, onClick = { onResume(); showMenu = false })
                        }
                        DropdownMenuItem(text = { Text("Eliminar", color = Color.Red) }, onClick = { onDelete(); showMenu = false })
                    }
                }
            }
        }
    }
}

fun getCategoryColor(category: String?): Pair<Color, Color> {
    val name = category?.lowercase() ?: ""
    return when {
        name.contains("saúd") || name.contains("água") || name.contains("agua") -> Pair(Color(0xFF1976D2), Color(0xFFE3F2FD))
        name.contains("exerc") || name.contains("desport") -> Pair(Color(0xFFF57C00), Color(0xFFFFF3E0))
        name.contains("estud") || name.contains("leit") || name.contains("livro") -> Pair(Color(0xFF7B1FA2), Color(0xFFF3E5F5))
        name.contains("medit") || name.contains("bem-estar") -> Pair(Color(0xFF388E3C), Color(0xFFE8F5E9))
        name.contains("financ") || name.contains("dinheir") -> Pair(Color(0xFF00796B), Color(0xFFE0F2F1))
        else -> Pair(LumiereGold, Color(0xFF332A1D))
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
