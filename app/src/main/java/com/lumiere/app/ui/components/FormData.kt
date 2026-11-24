package com.lumiere.app.ui.components

import androidx.compose.ui.graphics.vector.ImageVector

enum class FormMode { QUICK, FULL, WIZARD, DESTRUCTIVE }
enum class FormFieldType { TEXT, DATE, PRIORITY, DROPDOWN }

data class FormStep(
    val currentStep: Int,
    val totalSteps: Int,
    val stepTitle: String
)

data class FormField(
    val id: String,
    val label: String,
    val placeholder: String = "",
    val type: FormFieldType = FormFieldType.TEXT,
    val isRequired: Boolean = false,
    val errorMessage: String? = null,
    val hint: String? = null,
    val maxLength: Int? = null,
    val icon: ImageVector? = null,
    val options: List<String> = emptyList() // Para Dropdowns e Prioridades
)

data class UniversalFormData(
    val title: String,
    val description: String? = null,
    val stepInfo: FormStep? = null,
    val fields: List<FormField> = emptyList(),
    val hasDraft: Boolean = false,
    val draftTime: String? = null
)
