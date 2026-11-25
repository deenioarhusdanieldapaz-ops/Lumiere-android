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
    val description: String? = null,
    val subtitle: String? = null,
    val state: LumiereCardState? = null,
    val metadata: String? = null,
    val attachmentCount: Int? = null,
    val progress: Float? = null,
    val summaryValue: String? = null,
    val summaryTrend: String? = null,
    val summaryIcon: ImageVector? = null,
    val icon: ImageVector? = null,
    val image: Painter? = null,
    val tagText: String? = null,
    val isPriority: Boolean = false,
    val actions: List<CardAction> = emptyList(),
    val onMoreClick: (() -> Unit)? = null,
    val menuActions: List<CardAction> = emptyList()
)
