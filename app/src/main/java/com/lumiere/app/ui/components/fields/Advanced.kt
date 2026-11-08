package com.lumiere.app.ui.components.fields

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumiere.app.ui.theme.LumiereDarkGray
import com.lumiere.app.ui.theme.LumiereGold

@Composable
fun LumiereDynamicList(
    items: List<String>,
    onAdd: () -> Unit,
    onRemove: (Int) -> Unit,
    onItemChange: (Int, String) -> Unit,
    label: String = "Item"
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = item,
                    onValueChange = { onItemChange(index, it) },
                    label = { Text("$label ${index + 1}") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LumiereGold)
                )
                IconButton(onClick = { onRemove(index) }) {
                    Icon(Icons.Default.Close, contentDescription = "Remover", tint = Color.Red)
                }
            }
        }
        OutlinedButton(onClick = onAdd, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Add, contentDescription = null, tint = LumiereGold)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Adicionar $label", color = LumiereGold)
        }
    }
}

@Composable
fun LumiereTagSelector(
    tags: List<String>,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit
) {
    var newTag by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newTag,
                onValueChange = { newTag = it },
                label = { Text("Nova etiqueta") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LumiereGold)
            )
            IconButton(onClick = {
                if (newTag.isNotBlank()) { onAddTag(newTag.trim()); newTag = "" }
            }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar", tint = LumiereGold)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tags) { tag ->
                Row(
                    modifier = Modifier
                        .background(LumiereGold.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(tag, color = LumiereGold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        tint = LumiereGold,
                        modifier = Modifier.size(16.dp).clickable { onRemoveTag(tag) }
                    )
                }
            }
        }
    }
}

@Composable
fun LumiereColorPicker(
    colors: List<Color>,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(colors) { color ->
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color, CircleShape)
                    .border(
                        width = if (color == selectedColor) 3.dp else 1.dp,
                        color = if (color == selectedColor) Color.White else Color.Gray,
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(color) }
            )
        }
    }
}

@Composable
fun LumiereIconPicker(
    icons: List<androidx.compose.ui.graphics.vector.ImageVector>,
    selectedIcon: androidx.compose.ui.graphics.vector.ImageVector?,
    onIconSelected: (androidx.compose.ui.graphics.vector.ImageVector) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(icons) { icon ->
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (icon == selectedIcon) LumiereGold.copy(alpha = 0.3f) else LumiereDarkGray,
                        RoundedCornerShape(8.dp)
                    )
                    .border(
                        width = if (icon == selectedIcon) 2.dp else 1.dp,
                        color = if (icon == selectedIcon) LumiereGold else Color.Gray,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onIconSelected(icon) },
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = LumiereGold)
            }
        }
    }
}
