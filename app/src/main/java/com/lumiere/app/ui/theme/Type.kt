package com.lumiere.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.lumiere.app.R

/** Inter empacotada localmente — opção B aprovada pelo Eng. 4. */
val InterFontFamily = FontFamily(
    Font(R.font.inter_regular,  FontWeight.Normal),
    Font(R.font.inter_medium,   FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold,     FontWeight.Bold),
)

private fun inter(size: Int, lineHeight: Int, weight: FontWeight) = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = weight,
    fontSize   = size.sp,
    lineHeight = lineHeight.sp,
)

/**
 * Escala tipográfica aprovada pelo Eng. 4.
 * Roles em falta (displaySmall, headlineSmall, titleSmall, labelSmall)
 * preenchidos por proporção, conforme instrução "mantém a proporção".
 */
val LumiereTypography = Typography(
    displayLarge   = inter(32, 40, FontWeight.Bold),
    displayMedium  = inter(28, 36, FontWeight.Bold),
    displaySmall   = inter(24, 32, FontWeight.Bold),
    headlineLarge  = inter(24, 32, FontWeight.Bold),
    headlineMedium = inter(22, 28, FontWeight.SemiBold),
    headlineSmall  = inter(20, 26, FontWeight.SemiBold),
    titleLarge     = inter(20, 28, FontWeight.SemiBold),
    titleMedium    = inter(18, 24, FontWeight.SemiBold),
    titleSmall     = inter(16, 22, FontWeight.SemiBold),
    bodyLarge      = inter(16, 24, FontWeight.Normal),
    bodyMedium     = inter(14, 20, FontWeight.Normal),
    bodySmall      = inter(12, 16, FontWeight.Normal),
    labelLarge     = inter(14, 20, FontWeight.Medium),
    labelMedium    = inter(12, 16, FontWeight.Medium),
    labelSmall     = inter(11, 16, FontWeight.Medium),
)
