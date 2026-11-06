package com.lumiere.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

val LumiereGold = Color(0xFFD4AF37)
val LumiereBlack = Color(0xFF0A0A0A)
val LumiereDarkGray = Color(0xFF1E1E1E)
val LumiereTextLight = Color(0xFFF5F0EB)

private val NoirColorScheme = darkColorScheme(
    primary = LumiereGold,
    background = LumiereBlack,
    surface = LumiereDarkGray,
    onPrimary = LumiereBlack,
    onBackground = LumiereTextLight,
    onSurface = LumiereTextLight
)

@Composable
fun LumiereTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NoirColorScheme,
        content = content
    )
}
