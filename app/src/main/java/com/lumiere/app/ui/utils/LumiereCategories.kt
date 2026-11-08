package com.lumiere.app.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import com.lumiere.app.ui.theme.LumiereIcons

data class CategoryItem(
    val name: String,
    val icon: ImageVector,
    val area: String
)

object LumiereCategories {
    val all = listOf(
        // Trabalho & Produtividade
        CategoryItem("Trabalho", LumiereIcons.Work, "Trabalho"),
        CategoryItem("Escritório", LumiereIcons.Work, "Trabalho"),
        CategoryItem("Reuniões", Icons.Default.Refresh, "Trabalho"),
        CategoryItem("Projetos", LumiereIcons.Folder, "Trabalho"),
        CategoryItem("Carreira", Icons.Default.Star, "Trabalho"),

        // Desenvolvimento Pessoal
        CategoryItem("Estudos", LumiereIcons.School, "Desenvolvimento"),
        CategoryItem("Leitura", LumiereIcons.Notes, "Desenvolvimento"),
        CategoryItem("Cursos", LumiereIcons.School, "Desenvolvimento"),
        CategoryItem("Criatividade", Icons.Default.Star, "Desenvolvimento"),
        CategoryItem("Espiritualidade", Icons.Default.Favorite, "Desenvolvimento"),

        // Saúde & Bem-estar
        CategoryItem("Saúde", Icons.Default.Favorite, "Saúde"),
        CategoryItem("Exercício", LumiereIcons.Fitness, "Saúde"),
        CategoryItem("Alimentação", LumiereIcons.Restaurant, "Saúde"),
        CategoryItem("Sono", Icons.Default.Star, "Saúde"),
        CategoryItem("Meditação", Icons.Default.Favorite, "Saúde"),
        CategoryItem("Desporto", LumiereIcons.Fitness, "Saúde"),

        // Finanças & Negócios
        CategoryItem("Finanças", LumiereIcons.Money, "Finanças"),
        CategoryItem("Investimentos", Icons.Default.Info, "Finanças"),
        CategoryItem("Compras", LumiereIcons.ShoppingCart, "Finanças"),
        CategoryItem("Lumière Negócio", LumiereIcons.Work, "Finanças"),

        // Pessoal, Casa & Relações
        CategoryItem("Pessoal", LumiereIcons.Person, "Pessoal"),
        CategoryItem("Casa", LumiereIcons.Home, "Pessoal"),
        CategoryItem("Família", LumiereIcons.Person, "Pessoal"),
        CategoryItem("Amigos", LumiereIcons.Person, "Pessoal"),
        CategoryItem("Eventos", Icons.Default.DateRange, "Pessoal"),
        CategoryItem("Animais", Icons.Default.Favorite, "Pessoal"),
        CategoryItem("Manutenção", LumiereIcons.Settings, "Pessoal"),
        CategoryItem("Limpeza", LumiereIcons.Home, "Pessoal"),

        // Lazer & Viagens
        CategoryItem("Viagens", LumiereIcons.Flight, "Lazer"),
        CategoryItem("Hobbies", Icons.Default.Star, "Lazer"),
        CategoryItem("Música", LumiereIcons.Music, "Lazer"),

        // Sistema & Administração
        CategoryItem("Sistema", LumiereIcons.Settings, "Sistema"),
        CategoryItem("Outros", LumiereIcons.Tag, "Sistema"),
        CategoryItem("Documentos", LumiereIcons.Notes, "Sistema"),
        CategoryItem("Burocracia", Icons.Default.Info, "Sistema")
    )

    val names = all.map { it.name }

    fun getIcon(name: String?): ImageVector {
        if (name == null) return LumiereIcons.Tag
        return all.find { it.name.equals(name, ignoreCase = true) }?.icon ?: LumiereIcons.Tag
    }
}
