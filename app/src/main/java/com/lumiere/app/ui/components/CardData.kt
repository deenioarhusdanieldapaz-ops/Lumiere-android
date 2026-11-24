package com.lumiere.app.ui.components

import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector

enum class CardVariant { ENTITY, SUMMARY, ALERT, PROGRESS, INFO }
enum class CardDensity { COMPACT, NORMAL, EXPANDED }

enum class LumiereCardState(val label: String) {
    PENDING("Pendente"),
    IN_PROGRESS("Em curso"),
    DONE("Concluída"),
    OVERDUE("Atrasada"),
    PAUSED("Em pausa"),
    ARCHIVED("Arquivada")
}

data class CardAction(
    val label: String,
    val icon: ImageVector? = null,
    val isPrimary: Boolean = false,
    val onClick: () -> Unit
)

data class CardData(
    val title: String,
    val subtitle: String? = null,
    val state: LumiereCardState? = null,
    val metadata: String? = null,
    val progress: Float? = null, // 0.0 a 1.0
    val summaryValue: String? = null,
    val summaryTrend: String? = null, // Ex: "+20%"
    val summaryIcon: ImageVector? = null,
    val icon: ImageVector? = null,
    val image: Painter? = null, // Para as imagens laterais
    val tagText: String? = null, // Ex: "Tarefa"
    val isPriority: Boolean = false, // Ex: Estrela
    val actions: List<CardAction> = emptyList(),
    val onMoreClick: (() -> Unit)? = null
)
