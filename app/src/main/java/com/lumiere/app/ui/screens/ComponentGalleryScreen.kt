package com.lumiere.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumiere.app.ui.components.fields.*
import com.lumiere.app.ui.theme.LumiereGold
import com.lumiere.app.ui.utils.LumiereCategories
import com.lumiere.app.ui.theme.LumiereIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentGalleryScreen(onBack: () -> Unit) {
    // --- ESTADOS DA GALERIA ---
    var txt by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var pwd by remember { mutableStateOf("") }
    var masked by remember { mutableStateOf("") }
    var num by remember { mutableStateOf("") }
    var curr by remember { mutableStateOf("") }
    var step by remember { mutableIntStateOf(0) }
    var slide by remember { mutableFloatStateOf(1f) }
    var rate by remember { mutableIntStateOf(0) }
    var drop by remember { mutableStateOf<String?>(null) }
    var seg by remember { mutableStateOf("Opção 1") }
    var radio by remember { mutableStateOf<String?>(null) }
    var checks by remember { mutableStateOf(listOf<String>()) }
    var date by remember { mutableStateOf<Long?>(null) }
    var time by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var search by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("Todas") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Galeria de Componentes", color = LumiereGold, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = LumiereGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. SECÇÃO TEXTO
            SectionHeader("1. Texto")
            LumiereTextField(value = txt, onValueChange = { txt = it }, label = "LumiereTextField")
            LumiereTextArea(value = area, onValueChange = { area = it }, label = "LumiereTextArea")
            LumierePasswordField(value = pwd, onValueChange = { pwd = it }, label = "LumierePasswordField")
            LumiereMaskedField(value = masked, onValueChange = { masked = it }, label = "LumiereMaskedField", mask = "NIF")

            // 2. SECÇÃO NÚMEROS
            SectionHeader("2. Números")
            LumiereNumberField(value = num, onValueChange = { num = it }, label = "LumiereNumberField")
            LumiereCurrencyField(value = curr, onValueChange = { curr = it }, label = "LumiereCurrencyField")
            LumiereStepper(value = step, onValueChange = { step = it }, label = "LumiereStepper")
            LumiereSliderField(value = slide, onValueChange = { slide = it }, label = "LumiereSliderField")
            LumiereRatingBar(rating = rate, onRatingChange = { rate = it })

            // 3. SECÇÃO SELETORES
            SectionHeader("3. Seletores")
            LumiereDropdown(label = "LumiereDropdown", options = LumiereCategories.names, selected = drop, onSelect = { drop = it })
            LumiereSegmentedControl(options = listOf("Opção 1", "Opção 2", "Opção 3"), selected = seg, onSelect = { seg = it })
            LumiereRadioGroup(options = listOf("Receita", "Despesa"), selected = radio, onSelect = { radio = it })
            LumiereCheckboxGroup(options = listOf("Seg", "Ter", "Qua", "Qui", "Sex"), selectedOptions = checks, onToggle = { 
                checks = if (checks.contains(it)) checks - it else checks + it 
            })
            // 4. SECÇÃO DATAS
            SectionHeader("4. Datas e Tempo")
            LumiereDatePicker(selectedDate = date, onDateSelected = { date = it }, label = "LumiereDatePicker")
            LumiereTimePicker(selectedTime = time, onTimeSelected = { time = it })
            
            // 5. SECÇÃO ESTRUTURAS
            SectionHeader("5. Estruturas")
            var isExpanded by remember { mutableStateOf(true) }
            LumiereAccordion(title = "LumiereAccordion", isExpanded = isExpanded, onToggle = { isExpanded = !isExpanded }) {
                Text("Conteúdo da gaveta expansível.", color = Color.Gray)
            }
            LumiereSearchBar(query = search, onQueryChange = { search = it }, placeholder = "LumiereSearchBar")
            LumiereFilterChips(options = listOf("Todas", "Hoje", "Atrasadas"), selected = filter, onSelect = { filter = it })
            LumiereProgressBar(progress = 0.65f, label = "LumiereProgressBar")
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Spacer(modifier = Modifier.height(16.dp))
    Text(title, color = LumiereGold, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
    Divider(color = Color.DarkGray, modifier = Modifier.padding(bottom = 8.dp))
}
