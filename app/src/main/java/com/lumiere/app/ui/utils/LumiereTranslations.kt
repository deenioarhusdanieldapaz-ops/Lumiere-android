package com.lumiere.app.ui.utils

import com.lumiere.app.core.engine.ConflictKind
import com.lumiere.app.core.model.ApprovalClass
import com.lumiere.app.core.model.Impact
import com.lumiere.app.core.model.Reversibility

fun ApprovalClass.toPT(): String = when (this) {
    ApprovalClass.A0_CONSULT -> "A0 - Consulta"
    ApprovalClass.A1_PREPARE -> "A1 - Preparação"
    ApprovalClass.A2_LOW_IMPACT -> "A2 - Baixo Impacto"
    ApprovalClass.A3_RELEVANT -> "A3 - Relevante"
    ApprovalClass.A4_HIGH_IMPACT -> "A4 - Alto Impacto"
}

fun Impact.toPT(): String = when (this) {
    Impact.LOW -> "Impacto Baixo"
    Impact.MEDIUM -> "Impacto Médio"
    Impact.HIGH -> "Impacto Alto"
}

fun Reversibility.toPT(): String = when (this) {
    Reversibility.REVERSIBLE -> "Pode ser desfeito"
    Reversibility.PARTIAL -> "Parcialmente reversível"
    Reversibility.IRREVERSIBLE -> "Ação permanente"
}

fun ConflictKind.toPT(): String = when (this) {
    ConflictKind.TEMPORAL_OVERLAP -> "Cruzamento de Módulos"
    ConflictKind.CAPACITY_EXCEEDED_DAILY -> "Dia Sobrecarregado"
    ConflictKind.CAPACITY_EXCEEDED_WEEKLY -> "Semana Sobrecarregada"
}
