package com.lumiere.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumiere.app.ui.theme.LumiereDarkGray
import com.lumiere.app.ui.theme.LumiereGold
import com.lumiere.app.ui.viewmodel.HabitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitScreen(viewModel: HabitViewModel, onOpenDrawer: () -> Unit = {}) {
    // Dica #4: State Hoisting (Elevação de Estado)
    var showCreateSheet by remember { mutableStateOf(false) }
    var newHabitName by remember { mutableStateOf("") }
    var newHabitCategory by remember { mutableStateOf("") }
    var newHabitFrequency by remember { mutableStateOf("DAILY") }

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
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // HERO CARD (O anel de 78%, sequência e recorde)
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = CardDefaults.cardColors(containerColor = LumiereDarkGray),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4A3F38))
            ) {
                Row(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Anel de Consistência (Simulado por agora)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                            CircularProgressIndicator(
                                progress = 0.78f,
                                modifier = Modifier.size(80.dp),
                                color = LumiereGold,
                                trackColor = Color.DarkGray,
                                strokeWidth = 8.dp
                            )
                            Text("78%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Consistência", color = Color.Gray, fontSize = 11.sp)
                    }

                    // Streak (Sequência e Recorde)
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = LumiereGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("21 dias", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Sequência atual", color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("48 dias", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Recorde pessoal", color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
            
            // 1. INDICADORES COMPACTOS (4 cartões)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Concluídos
                MetricTile(Modifier.weight(1f), "0", "Concluídos", Color.Green)
                // Pendentes
                MetricTile(Modifier.weight(1f), "0", "Pendentes", LumiereGold)
                // Média
                MetricTile(Modifier.weight(1f), "0%", "Média", Color(0xFF9C27B0))
                // Metas
                MetricTile(Modifier.weight(1f), "0/0", "Metas", Color(0xFFFF9800))
            }

            // 2. TÍTULO "HÁBITOS DE HOJE"
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Hábitos de Hoje", color = LumiereGold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Ver todos →", color = LumiereGold, fontSize = 12.sp)
            }

            // 3. ESTADO VAZIO PREMIUM (Design do Eng. 11)
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Estrela Gigante
                Box(
                    modifier = Modifier.size(120.dp).background(LumiereGold.copy(alpha = 0.05f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = LumiereGold, modifier = Modifier.size(80.dp))
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text("Bem-vindo ao Módulo", color = Color.White, fontSize = 24.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Text("Hábitos!", color = LumiereGold, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "Comece agora a criar os seus hábitos e construa uma vida mais equilibrada.",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Botão Principal
                Button(
                    onClick = { showCreateSheet = true }, // Usando o estado elevado (Tip #4)
                    modifier = Modifier.fillMaxWidth(0.9f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LumiereGold),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Criar Meu Primeiro Hábito", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Botão Secundário (Tutorial)
                OutlinedButton(
                    onClick = { /* Ação do tutorial */ },
                    modifier = Modifier.fillMaxWidth(0.9f).height(56.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LumiereGold),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = LumiereGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ver Tutorial", color = LumiereGold, fontSize = 16.sp)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Dica da Lumière
                Card(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4A3F38))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = LumiereGold, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Dica da Lumière", color = LumiereGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Pequenos hábitos, grandes mudanças!", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
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
