package com.lumiere.app.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import com.lumiere.app.ui.theme.LumiereIcons

fun getCategoryIcon(categoryName: String?): ImageVector {
    if (categoryName == null) return Icons.Default.Star
    val name = categoryName.lowercase()
    return when {
        name.contains("saúd") || name.contains("saud") || name.contains("médic") || name.contains("agua") -> Icons.Default.Favorite
        name.contains("exerc") || name.contains("ginás") || name.contains("ginas") || name.contains("corrid") -> LumiereIcons.Fitness
        name.contains("estud") || name.contains("curso") || name.contains("aula") -> LumiereIcons.School
        name.contains("trabalho") || name.contains("escrit") || name.contains("reuni") -> LumiereIcons.Work
        name.contains("financ") || name.contains("dinheir") -> LumiereIcons.Money
        name.contains("casa") || name.contains("lar") -> LumiereIcons.Home
        name.contains("pessoal") -> LumiereIcons.Person
        name.contains("compra") || name.contains("mercado") -> LumiereIcons.ShoppingCart
        name.contains("leitur") || name.contains("livro") -> LumiereIcons.Notes
        name.contains("comid") || name.contains("restaur") -> LumiereIcons.Restaurant
        name.contains("viag") || name.contains("avião") || name.contains("aviao") -> LumiereIcons.Flight
        name.contains("program") || name.contains("códig") -> LumiereIcons.Code
        name.contains("músic") -> LumiereIcons.Music
        name.contains("filme") || name.contains("séri") -> LumiereIcons.Movie
        else -> Icons.Default.Star
    }
}
