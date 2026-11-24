package com.lumiere.app.ui.components

import androidx.compose.ui.graphics.vector.ImageVector

enum class FormMode { QUICK, FULL, WIZARD, DESTRUCTIVE }

data class FormStep(
    val currentStep: Int,
    val totalSteps: Int,
    val stepTitle: String
)

data class FormField(
    val id: String,
    val label: String,
    val placeholder: String = "",
    val isRequired: Boolean = false,
    val errorMessage: String? = null,
    val hint: String? = null, // Para a lâmpada
    val maxLength: Int? = null, // Para o contador (0/100)
    val icon: ImageVector? = null // Para o ícone no topo do form
)

data class UniversalFormData(
    val title: String,
    val description: String? = null,
    val stepInfo: FormStep? = null,
    val fields: List<FormField> = emptyList(),
    val hasDraft: Boolean = false,
    val draftTime: String? = null
)
