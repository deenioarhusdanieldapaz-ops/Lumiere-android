package com.lumiere.app.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.lumiere.app.ui.theme.LumiereGold

// O Molde que une a Inteligência Visual
data class CategoryTheme(
    val icon: ImageVector,
    val iconColor: Color,
    val backgroundColor: Color,
    val name: String
)

object CategoryEngine {
    // O Dicionário Central. No futuro, isto pode vir de um JSON ou do Core.
    private val dictionary = mapOf(
        "saúde" to CategoryTheme(Icons.Default.LocalHospital, Color(0xFF81C784), Color(0xFF1B5E20), "Saúde"),
        "exercício" to CategoryTheme(Icons.Default.FitnessCenter, Color(0xFFFFB74D), Color(0xFFE65100), "Exercício"),
        "estudo" to CategoryTheme(Icons.Default.School, Color(0xFFCE93D8), Color(0xFF4A148C), "Estudo"),
        "leitura" to CategoryTheme(Icons.Default.MenuBook, Color(0xFFCE93D8), Color(0xFF4A148C), "Leitura"),
        "trabalho" to CategoryTheme(Icons.Default.Work, Color(0xFFFFD54F), Color(0xFF3E2723), "Trabalho"),
        "finanças" to CategoryTheme(Icons.Default.AccountBalanceWallet, Color(0xFFFFD54F), Color(0xFF3E2723), "Finanças"),
        "cripto" to CategoryTheme(Icons.Default.CurrencyBitcoin, Color(0xFFFFD54F), Color(0xFF3E2723), "Cripto"),
        "casa" to CategoryTheme(Icons.Default.Home, Color(0xFF64B5F6), Color(0xFF0D47A1), "Casa"),
        "jogos" to CategoryTheme(Icons.Default.SportsEsports, Color(0xFFF06292), Color(0xFF880E4F), "Jogos"),
        "meditação" to CategoryTheme(Icons.Default.SelfImprovement, Color(0xFF4DD0E1), Color(0xFF006064), "Meditação"),
        "animais" to CategoryTheme(Icons.Default.Pets, Color(0xFFA1887F), Color(0xFF3E2723), "Animais")
    )

    // O Cérebro: Analisa o texto e devolve o Tema
    fun getTheme(categoryName: String?): CategoryTheme {
        if (categoryName.isNullOrBlank()) return defaultTheme()
        val name = categoryName.lowercase().trim()

        // 1. Procura no dicionário (Match exato ou parcial)
        val matchedKey = dictionary.keys.firstOrNull { name.contains(it) }
        if (matchedKey != null) return dictionary[matchedKey]!!

        // 2. Fallback Inteligente (Se não encontrar, usa o algoritmo matemático)
        return fallbackTheme(name)
    }

    private fun defaultTheme() = CategoryTheme(
        Icons.Default.Star, LumiereGold, Color(0xFF332A1D), "Geral"
    )

    private fun fallbackTheme(name: String): CategoryTheme {
        val fallbackList = listOf(
            CategoryTheme(Icons.Default.Bookmark, Color(0xFF90CAF9), Color(0xFF1A237E), "Geral"),
            CategoryTheme(Icons.Default.Label, Color(0xFFA5D6A7), Color(0xFF1B5E20), "Geral"),
            CategoryTheme(Icons.Default.PushPin, Color(0xFFFFAB91), Color(0xFFBF360C), "Geral"),
            CategoryTheme(Icons.Default.Lightbulb, LumiereGold, Color(0xFF332A1D), "Geral"),
            CategoryTheme(Icons.Default.Bolt, Color(0xFFFFF59D), Color(0xFFF57F17), "Geral")
        )
        val index = Math.abs(name.hashCode()) % fallbackList.size
        return fallbackList[index]
    }
}
