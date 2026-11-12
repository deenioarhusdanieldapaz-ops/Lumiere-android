package com.lumiere.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.lumiere.app.R

val InterFontFamily = FontFamily(
    Font(R.font.inter_regular,  FontWeight.Normal),
    Font(R.font.inter_medium,   FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold,     FontWeight.Bold),
)

val PlayfairFontFamily = FontFamily(
    Font(R.font.playfair_regular, FontWeight.Normal)
)

private fun inter(size: Int, lineHeight: Int, weight: FontWeight) = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = weight,
    fontSize   = size.sp,
    lineHeight = lineHeight.sp,
)

private fun playfair(size: Int, lineHeight: Int, weight: FontWeight) = TextStyle(
    fontFamily = PlayfairFontFamily,
    fontWeight = weight,
    fontSize   = size.sp,
    lineHeight = lineHeight.sp,
)

val LumiereTypography = Typography(
    displayLarge   = playfair(32, 40, FontWeight.Bold), // Usado no Hero Card
    displayMedium  = playfair(28, 36, FontWeight.Bold),
    displaySmall   = inter(24, 32, FontWeight.Bold),
    headlineLarge  = playfair(24, 32, FontWeight.Bold),
    headlineMedium = playfair(22, 28, FontWeight.Bold), // Usado no Título do TopBar
    headlineSmall  = inter(20, 26, FontWeight.SemiBold),
    titleLarge     = playfair(20, 28, FontWeight.Bold),
    titleMedium    = inter(18, 24, FontWeight.SemiBold),
    titleSmall     = inter(16, 24, FontWeight.SemiBold),
    bodyLarge      = inter(16, 24, FontWeight.Normal),
    bodyMedium     = inter(14, 20, FontWeight.Normal),
    bodySmall      = inter(12, 16, FontWeight.Normal),
    labelLarge     = inter(14, 20, FontWeight.Medium),
    labelMedium    = inter(12, 16, FontWeight.Medium),
    labelSmall     = inter(11, 16, FontWeight.Medium),
)
