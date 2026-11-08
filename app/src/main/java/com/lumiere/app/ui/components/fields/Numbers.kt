package com.lumiere.app.ui.components.fields

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumiere.app.ui.theme.LumiereGold

@Composable
fun LumiereNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.isEmpty() || it.matches(Regex("\\d*\\.?\\d*"))) onValueChange(it) },
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LumiereGold)
    )
}

@Composable
fun LumiereCurrencyField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    currencySymbol: String = "€",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.isEmpty() || it.matches(Regex("\\d*\\.?\\d{0,2}"))) onValueChange(it) },
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        leadingIcon = { Text(currencySymbol, color = LumiereGold) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LumiereGold)
    )
}

@Composable
fun LumiereStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f))
        IconButton(onClick = { if (value > 0) onValueChange(value - 1) }) {
            Text("−", color = LumiereGold, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Text(value.toString(), color = LumiereGold)
        IconButton(onClick = { onValueChange(value + 1) }) {
            Text("+", color = LumiereGold, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LumiereSliderField(
    value: Float,
    onValueChange: (Float) -> Unit,
    label: String,
    valueRange: ClosedFloatingPointRange<Float> = 1f..5f,
    steps: Int = 4
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("$label: ${value.toInt()}", color = MaterialTheme.colorScheme.onSurface)
        Slider(value = value, onValueChange = onValueChange, valueRange = valueRange, steps = steps)
    }
}

@Composable
fun LumiereRatingBar(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    maxRating: Int = 5
) {
    Row {
        for (i in 1..maxRating) {
            IconButton(onClick = { onRatingChange(i) }) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "$i Estrelas",
                    tint = if (i <= rating) LumiereGold else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
