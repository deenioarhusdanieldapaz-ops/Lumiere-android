package com.lumiere.app.ui.components.fields

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumiere.app.ui.theme.LumiereDarkGray
import com.lumiere.app.ui.theme.LumiereGold

@Composable
fun LumiereAccordion(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = LumiereDarkGray),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onToggle() }.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = LumiereGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = LumiereGold
                )
            }
            if (isExpanded) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
fun LumiereSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Pesquisar..."
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(placeholder) },
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = LumiereGold) },
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LumiereGold)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LumiereFilterChips(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        options.forEach { option ->
            FilterChip(
                selected = selected == option,
                onClick = { onSelect(option) },
                label = { Text(option) }
            )
        }
    }
}

@Composable
fun LumiereProgressBar(
    progress: Float,
    label: String? = null,
    color: Color = LumiereGold
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (label != null) {
            Text(label, color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            LinearProgressIndicator(
                progress = progress.coerceIn(0f, 1f),
                modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = Color.DarkGray
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("${(progress * 100).toInt()}%", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}
