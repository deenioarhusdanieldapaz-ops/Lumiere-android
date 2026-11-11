package com.lumiere.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

// Aliases legados mantidos por compatibilidade até purga no Bloco 2
val LumiereGold      = NoirPalette.Primary
val LumiereBlack     = NoirPalette.Background
val LumiereDarkGray  = NoirPalette.Surface
val LumiereTextLight = NoirPalette.OnBackground

private val NoirColorScheme = darkColorScheme(
    primary            = NoirPalette.Primary,
    onPrimary          = NoirPalette.OnPrimary,
    primaryContainer   = NoirPalette.PrimaryContainer,
    background         = NoirPalette.Background,
    onBackground       = NoirPalette.OnBackground,
    surface            = NoirPalette.Surface,
    onSurface          = NoirPalette.OnSurface,
    surfaceVariant     = NoirPalette.SurfaceVariant,
    onSurfaceVariant   = NoirPalette.OnSurfaceVariant,
    outline            = NoirPalette.Outline,
    outlineVariant     = NoirPalette.OutlineVariant,
    error              = NoirPalette.Error,
    errorContainer     = NoirPalette.ErrorContainer,
    onError            = NoirPalette.OnError,
    tertiary           = NoirPalette.Warning,
    secondary          = NoirPalette.Accent,
)

private val IvoryColorScheme = lightColorScheme(
    primary            = IvoryPalette.Primary,
    onPrimary          = IvoryPalette.OnPrimary,
    primaryContainer   = IvoryPalette.PrimaryContainer,
    background         = IvoryPalette.Background,
    onBackground       = IvoryPalette.OnBackground,
    surface            = IvoryPalette.Surface,
    onSurface          = IvoryPalette.OnSurface,
    surfaceVariant     = IvoryPalette.SurfaceVariant,
    onSurfaceVariant   = IvoryPalette.OnSurfaceVariant,
    outline            = IvoryPalette.Outline,
    outlineVariant     = IvoryPalette.OutlineVariant,
    error              = IvoryPalette.Error,
    errorContainer     = IvoryPalette.ErrorContainer,
    onError            = IvoryPalette.OnError,
    tertiary           = IvoryPalette.Warning,
    secondary          = IvoryPalette.Accent,
)

enum class LumiereThemeMode { System, Light, Dark }

object Lumiere {
    val colors: LumiereColors
        @Composable get() = LocalLumiereColors.current
}

@Composable
fun LumiereTheme(
    mode: LumiereThemeMode = LumiereThemeMode.System,
    content: @Composable () -> Unit,
) {
    val dark = when (mode) {
        LumiereThemeMode.System -> isSystemInDarkTheme()
        LumiereThemeMode.Light  -> false
        LumiereThemeMode.Dark   -> true
    }
    CompositionLocalProvider(
        LocalLumiereColors provides if (dark) NoirExtended else IvoryExtended
    ) {
        MaterialTheme(
            colorScheme = if (dark) NoirColorScheme else IvoryColorScheme,
            typography  = LumiereTypography,
            shapes      = LumiereShapes,
            content     = content,
        )
    }
}
