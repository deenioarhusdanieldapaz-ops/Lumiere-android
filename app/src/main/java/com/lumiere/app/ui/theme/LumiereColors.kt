package com.lumiere.app.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Tokens semânticos que Material3 ColorScheme não tem nativamente.
 * Acesso: Lumiere.colors.success / warning / accent
 */
data class LumiereColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val warning: Color,
    val accent: Color,
)

val NoirExtended = LumiereColors(
    success          = NoirPalette.Success,
    onSuccess        = NoirPalette.OnSuccess,
    successContainer = NoirPalette.SuccessContainer,
    warning          = NoirPalette.Warning,
    accent           = NoirPalette.Accent,
)

val IvoryExtended = LumiereColors(
    success          = IvoryPalette.Success,
    onSuccess        = IvoryPalette.OnSuccess,
    successContainer = IvoryPalette.SuccessContainer,
    warning          = IvoryPalette.Warning,
    accent           = IvoryPalette.Accent,
)

val LocalLumiereColors = staticCompositionLocalOf { NoirExtended }
