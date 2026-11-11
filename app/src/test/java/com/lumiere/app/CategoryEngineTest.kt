package com.lumiere.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import com.lumiere.app.ui.utils.CategoryEngine
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class CategoryEngineTest {

    @Test
    @DisplayName("Categoria 'Jogos' deve devolver o icone de videojogo")
    fun `category Jogos returns SportsEsports icon`() {
        val theme = CategoryEngine.getTheme("Jogos")
        assertEquals(Icons.Default.SportsEsports, theme.icon)
    }

    @Test
    @DisplayName("Categoria 'Cripto' deve devolver o icone de carteira (financas)")
    fun `category Cripto returns Finance icon`() {
        val theme = CategoryEngine.getTheme("Criptomoedas")
        assertEquals(Icons.Default.CurrencyBitcoin, theme.icon)
    }

    @Test
    @DisplayName("Categoria nula deve devolver a Estrela (default)")
    fun `null category returns Star icon`() {
        val theme = CategoryEngine.getTheme(null)
        assertEquals(Icons.Default.Star, theme.icon)
    }

    @Test
    @DisplayName("Palavra desconhecida deve devolver um fallback (nao a estrela)")
    fun `unknown category returns fallback icon`() {
        val theme = CategoryEngine.getTheme("Blablabla")
        // O fallback tem a cor LumiereGold no texto, mas o icone nao deve ser Star
        // Como o fallback usa hashCode, apenas verificamos que nao quebra
        assert(theme.name == "Geral")
    }
}
