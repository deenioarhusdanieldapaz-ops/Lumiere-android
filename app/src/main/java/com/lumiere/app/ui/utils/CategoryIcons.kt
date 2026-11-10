package com.lumiere.app.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

fun getCategoryIcon(categoryName: String?): ImageVector {
    if (categoryName.isNullOrBlank()) return Icons.Default.Star
    val name = categoryName.lowercase()

    return when {
        // SAÚDE E BEM-ESTAR
        name.contains("saúd") || name.contains("saud") || name.contains("médic") || name.contains("agua") -> Icons.Default.Favorite
        name.contains("exerc") || name.contains("ginás") || name.contains("ginas") || name.contains("corrid") -> Icons.Default.DirectionsRun
        name.contains("medit") || name.contains("yoga") || name.contains("paz") -> Icons.Default.SelfImprovement
        
        // ESTUDO E TRABALHO
        name.contains("estud") || name.contains("curso") || name.contains("aula") -> Icons.Default.School
        name.contains("leitur") || name.contains("livro") -> Icons.Default.MenuBook
        name.contains("trabalho") || name.contains("escrit") || name.contains("reuni") -> Icons.Default.Work
        name.contains("program") || name.contains("códig") || name.contains("codig") -> Icons.Default.Code
        
        // FINANÇAS E COMPRAS
        name.contains("financ") || name.contains("dinheir") || name.contains("poupan") -> Icons.Default.AccountBalanceWallet
        name.contains("compra") || name.contains("mercado") || name.contains("supermerc") -> Icons.Default.ShoppingCart
        
        // CASA E PESSOAL
        name.contains("casa") || name.contains("lar") || name.contains("limpez") -> Icons.Default.Home
        name.contains("pessoal") || name.contains("eu") -> Icons.Default.Person
        
        // LAZER E TEMPO LIVRE
        name.contains("comid") || name.contains("restaur") || name.contains("cozinh") -> Icons.Default.Restaurant
        name.contains("viag") || name.contains("avião") || name.contains("aviao") || name.contains("férias") -> Icons.Default.Flight
        name.contains("músic") || name.contains("music") -> Icons.Default.MusicNote
        name.contains("filme") || name.contains("séri") || name.contains("cinema") -> Icons.Default.Movie
        name.contains("jogo") || name.contains("game") || name.contains("jogar") -> Icons.Default.SportsEsports
        name.contains("animal") || name.contains("pet") || name.contains("cão") || name.contains("gato") -> Icons.Default.Pets
        
        // TEMPO E ORGANIZAÇÃO
        name.contains("tempo") || name.contains("hora") || name.contains("relógio") -> Icons.Default.AccessTime
        name.contains("data") || name.contains("calendár") || name.contains("agenda") -> Icons.Default.CalendarMonth
        
        // FALLBACK
        else -> Icons.Default.Star
    }
}
