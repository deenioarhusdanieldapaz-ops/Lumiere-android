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
import com.lumiere.app.ui.components.fields.*
import com.lumiere.app.ui.utils.LumiereCategories
import com.lumiere.app.data.local.entity.Habit
import com.lumiere.app.domain.usecase.HabitKind
import com.lumiere.app.domain.usecase.kind
import com.lumiere.app.data.local.entity.HabitCheckIn
import com.lumiere.app.ui.components.MetricTile
import com.lumiere.app.ui.utils.CategoryEngine
import com.lumiere.app.ui.viewmodel.HabitViewModel
import com.lumiere.app.ui.theme.Lumiere

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitScreen(viewModel: HabitViewModel, onOpenDrawer: () -> Unit = {}) {
    val habits by viewModel.habits.collectAsState()
    val allCheckIns by viewModel.allCheckIns.collectAsState()

    val cal = java.util.Calendar.getInstance()
    val startOfDay = cal.apply { set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0); set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0) }.timeInMillis
    val endOfDay = cal.apply { set(java.util.Calendar.HOUR_OF_DAY, 23); set(java.util.Calendar.MINUTE, 59); set(java.util.Calendar.SECOND, 59); set(java.util.Calendar.MILLISECOND, 999) }.timeInMillis

    val checkInsToday = allCheckIns.filter { it.timestamp in startOfDay..endOfDay }
    val completedToday = checkInsToday.map { it.habitId }.distinct().size
    val totalHabits = habits.size
    val pendingToday = (totalHabits - completedToday).coerceAtLeast(0)
    val consistency = viewModel.calculateConsistency().toFloat()

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
                        Text("M02 — Hábitos", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
                        Text("Pequenas ações, grandes conquistas", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.primary)
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
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val currentStreak = viewModel.calculateGlobalStreak()

            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.outlineVariant,
                                strokeWidth = 8.dp
                            )
                            Text("${consistency.toInt()}%", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Consistência", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("$currentStreak dias", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall)
                                Text("Sequência atual", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("$currentStreak dias", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall)
                                Text("Recorde pessoal", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricTile(Modifier.weight(1f), "$completedToday", "Concluídos", Lumiere.colors.success)
                MetricTile(Modifier.weight(1f), "$pendingToday", "Pendentes", MaterialTheme.colorScheme.primary)
                MetricTile(Modifier.weight(1f), "${consistency.toInt()}%", "Média", Lumiere.colors.accent)
                MetricTile(Modifier.weight(1f), "$completedToday/$totalHabits", "Metas", Lumiere.colors.warning)
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Hábitos de Hoje", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
                Text("Ver todos →", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
            }

            if (habits.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(120.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(80.dp))
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("Bem-vindo ao Módulo", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                    Text("Hábitos!", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.displayLarge)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Comece agora a criar os seus hábitos e construa uma vida mais equilibrada.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
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
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Criar Meu Primeiro Hábito", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.titleSmall)
                    }
                }
            } else {
                habits.forEach { habit ->
                    val isCompletedToday = checkInsToday.any { it.habitId == habit.id && it.type == "SUCCESS" }
                    val failureToday = checkInsToday.any { it.habitId == habit.id && it.type == "FAIL" }
                    HabitItem(
                        habit = habit,
                        isCompletedToday = isCompletedToday,
                        failureToday = failureToday,
                        weeklyProgress = if (habit.kind() == HabitKind.WEEKLY_FREQUENCY) viewModel.getWeeklyProgress(habit, checkInsToday) else null,
                        intervalStatus = if (habit.kind() == HabitKind.INTERVAL) viewModel.getIntervalStatus(habit, checkInsToday) else null,
                        onStartTimer = { viewModel.startTimer(habit) },
                        onPauseTimer = { viewModel.pauseTimer(habit) },
                        onCompleteTimer = { viewModel.completeTimer(habit) },
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

            Text("Calendário de Consistência", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
            HabitWeekStrip(checkIns = checkInsToday)

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (habitDetails != null) {
        val habit = habitDetails!!
        val habitCheckIns by viewModel.getCheckInsForHabit(habit.id).collectAsState(initial = emptyList())

        ModalBottomSheet(onDismissRequest = { habitDetails = null }, containerColor = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Text(habit.name, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineMedium)
                if (habit.category != null) {
                    Text(habit.category!!, color = CategoryEngine.getTheme(habit.category).iconColor, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text("Histórico de Check-ins", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))

                if (habitCheckIns.isEmpty()) {
                    Text("Sem registos ainda.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                } else {
                    habitCheckIns.take(10).forEach { ci ->
                        val date = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(ci.timestamp))
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(date, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (ci.value != null) {
                                    Text("${ci.value} ${habit.unit ?: ""}", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall)
                                } else {
                                    Text("✓", color = Lumiere.colors.success, style = MaterialTheme.typography.bodySmall)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = { viewModel.deleteCheckIn(ci) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Apagar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // TODO(Eng.3): BUG PRE-EXISTENTE — este bloco e identico ao anterior, comentado como "MODAL: CRIAR HABITO" mas mostra detalhes. Preservado sem alterar logica. Registado para Eng.4/Eng.5.
    if (habitDetails != null) {
        val habit = habitDetails!!
        val habitCheckIns by viewModel.getCheckInsForHabit(habit.id).collectAsState(initial = emptyList())

        ModalBottomSheet(onDismissRequest = { habitDetails = null }, containerColor = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Text(habit.name, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineMedium)
                if (habit.category != null) {
                    Text(habit.category!!, color = CategoryEngine.getTheme(habit.category).iconColor, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text("Histórico de Check-ins", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))

                if (habitCheckIns.isEmpty()) {
                    Text("Sem registos ainda.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                } else {
                    habitCheckIns.take(10).forEach { ci ->
                        val date = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(ci.timestamp))
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(date, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (ci.value != null) {
                                    Text("${ci.value} ${habit.unit ?: ""}", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall)
                                } else {
                                    Text("✓", color = Lumiere.colors.success, style = MaterialTheme.typography.bodySmall)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = { viewModel.deleteCheckIn(ci) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Apagar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showCreateSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCreateSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()).fillMaxWidth()) {
                Text(
                    if (habitToEdit == null) "Novo Hábito" else "Editar Hábito",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

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

                var isMetaOpen by remember { mutableStateOf(false) }
                LumiereAccordion(title = "3. Metas e Descanso", isExpanded = isMetaOpen, onToggle = { isMetaOpen = !isMetaOpen }) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LumiereNumberField(value = newHabitTarget, onValueChange = { newHabitTarget = it }, label = "Meta", modifier = Modifier.weight(1f))
                        LumiereTextField(value = newHabitUnit, onValueChange = { newHabitUnit = it }, label = "Unidade", modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Dias de Descanso", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
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

                var isPrefOpen by remember { mutableStateOf(false) }
                LumiereAccordion(title = "4. Preferências", isExpanded = isPrefOpen, onToggle = { isPrefOpen = !isPrefOpen }) {
                    LumiereTimePicker(
                        selectedTime = preferredTime,
                        onTimeSelected = { preferredTime = it },
                        label = "Horário Preferencial"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("Lembrete", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                    }
                }

                var isCritOpen by remember { mutableStateOf(false) }
                LumiereAccordion(title = "5. Duração e Critérios", isExpanded = isCritOpen, onToggle = { isCritOpen = !isCritOpen }) {
                    LumiereNumberField(value = durationMinutes, onValueChange = { durationMinutes = it }, label = "Duração estimada (min)")
                    Spacer(modifier = Modifier.height(8.dp))
                    LumiereTextArea(value = completionCriteria, onValueChange = { completionCriteria = it }, label = "Critério de Conclusão (ex: 20 flexões)")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Período de Adaptação (não quebra streak)",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Guardar Hábito", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.titleSmall)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun HabitItem(
    habit: Habit,
    isCompletedToday: Boolean = false,
    failureToday: Boolean = false,
    todayValue: Int = 0,
    weeklyProgress: Pair<Int, Int>? = null,
    intervalStatus: Pair<Int, Int>? = null,
    onCheckIn: () -> Unit,
    onDelete: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onEdit: () -> Unit,
    onFail: () -> Unit,
    onOpenDetails: () -> Unit = {},
    onStartTimer: () -> Unit = {},
    onPauseTimer: () -> Unit = {},
    onCompleteTimer: () -> Unit = {}
) {
    val isPaused = habit.status == 1
    val kind = habit.kind()
    val (iconColor, bgColor) = Pair(CategoryEngine.getTheme(habit.category).iconColor, CategoryEngine.getTheme(habit.category).backgroundColor)

    val nowMs by androidx.compose.runtime.produceState(
        initialValue = System.currentTimeMillis(),
        key1 = habit.timerStartedAt
    ) {
        if (habit.timerStartedAt != null) {
            while (true) {
                value = System.currentTimeMillis()
                kotlinx.coroutines.delay(1000L)
            }
        }
    }
    val elapsedSec = if (habit.timerStartedAt != null) ((nowMs - habit.timerStartedAt) / 1000L).toInt() else 0
    val realTotalSec = habit.timerAccumulatedSeconds + elapsedSec
    val targetSec = (habit.durationMinutes ?: 0) * 60
    val totalSec = if (targetSec > 0 && realTotalSec > targetSec) targetSec else realTotalSec
    val totalMin = totalSec / 60
    val displaySec = totalSec % 60
    val timerText = String.format("%02d:%02d", totalMin, displaySec)

    val isDone = hasFailedOrCompleted(failureToday, isCompletedToday, kind, habit, totalSec)

    androidx.compose.runtime.LaunchedEffect(totalSec, habit.timerStartedAt) {
        if (kind == HabitKind.DURATION && habit.timerStartedAt != null) {
            val target = (habit.durationMinutes ?: 0) * 60
            if (target > 0 && totalSec >= target && !isCompletedToday) {
                onCompleteTimer()
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(52.dp).background(bgColor, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(CategoryEngine.getTheme(habit.category).icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.clickable { onOpenDetails() }
                )
                Spacer(modifier = Modifier.height(4.dp))
                when (kind) {
                    HabitKind.QUANTITATIVE -> {
                        val target = habit.targetValue ?: 0
                        val progressText = if (isCompletedToday) "$target/$target ${habit.unit ?: ""}" else "$todayValue/$target ${habit.unit ?: ""}"
                        Text(progressText, color = if (isCompletedToday) Lumiere.colors.success else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    HabitKind.DURATION -> {
                        val target = habit.durationMinutes ?: 0
                        val showText = "$timerText / $target min"
                        Text(showText, color = if (totalSec >= target * 60 && target > 0) Lumiere.colors.success else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    HabitKind.WEEKLY_FREQUENCY -> {
                        val pair = weeklyProgress ?: (0 to habit.weeklyDays.countOneBits())
                        Text("${pair.first}/${pair.second} esta semana", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    HabitKind.INTERVAL -> {
                        val pair = intervalStatus ?: (0 to habit.intervalDays)
                        val available = pair.first >= pair.second
                        Text(
                            if (available) "Disponivel hoje" else "Proximo em ${pair.second - pair.first} dia(s)",
                            color = if (available) Lumiere.colors.success else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    HabitKind.BOOLEAN -> {
                        if (isCompletedToday) Text("Concluido hoje", color = Lumiere.colors.success, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.width(1.dp).height(40.dp).background(MaterialTheme.colorScheme.outline))
            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isPaused) {
                    OutlinedButton(
                        onClick = onResume,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("Retomar", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    }
                } else if (failureToday && kind == HabitKind.BOOLEAN) {
                    OutlinedButton(
                        onClick = { },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("Falhou", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                } else if (isDone) {
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = Lumiere.colors.successContainer),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cumprido", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelSmall)
                    }
                } else {
                    if (kind == HabitKind.DURATION) {
                        if (habit.timerStartedAt != null) {
                            OutlinedButton(
                                onClick = onPauseTimer,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Pausar", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            OutlinedButton(
                                onClick = onCompleteTimer,
                                border = BorderStroke(1.dp, Lumiere.colors.success),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Concluir", color = Lumiere.colors.success, style = MaterialTheme.typography.labelSmall)
                            }
                        } else {
                            OutlinedButton(
                                onClick = onStartTimer,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Iniciar", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    } else {
                        val label = when (kind) {
                            HabitKind.QUANTITATIVE -> "+1"
                            HabitKind.WEEKLY_FREQUENCY -> "Hoje"
                            else -> "Registar"
                        }
                        OutlinedButton(
                            onClick = onCheckIn,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(label, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                var showMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Opcoes", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                        DropdownMenuItem(
                            text = { Text("Editar Habito", color = MaterialTheme.colorScheme.primary) },
                            onClick = { onEdit(); showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Registar Falha", color = Lumiere.colors.warning) },
                            onClick = { onFail(); showMenu = false }
                        )
                        if (habit.status == 0) {
                            DropdownMenuItem(
                                text = { Text("Pausar", color = MaterialTheme.colorScheme.primary) },
                                onClick = { onPause(); showMenu = false }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Retomar", color = Lumiere.colors.success) },
                                onClick = { onResume(); showMenu = false }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                            onClick = { onDelete(); showMenu = false }
                        )
                    }
                }
            }
        }
    }
}

private fun hasFailedOrCompleted(
    failureToday: Boolean, isCompletedToday: Boolean,
    kind: HabitKind, habit: Habit, totalSec: Int
): Boolean {
    if (failureToday && kind == HabitKind.BOOLEAN) return true
    return when (kind) {
        HabitKind.DURATION -> {
            val target = habit.durationMinutes ?: 0
            target > 0 && totalSec >= target * 60
        }
        else -> isCompletedToday
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
                Text(day, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier.size(36.dp)
                        .background(
                            if (hasCheckIn) Lumiere.colors.success
                            else if (isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surface,
                            CircleShape
                        )
                        .border(
                            if (isToday) 1.dp else 0.dp,
                            if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (hasCheckIn) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
                    } else {
                        Text("${calendar.get(java.util.Calendar.DAY_OF_MONTH) - todayIndex + index}", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
