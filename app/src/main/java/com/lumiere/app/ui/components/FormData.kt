package com.lumiere.app.ui.components

enum class FormMode {
    QUICK, FULL, WIZARD, DESTRUCTIVE
}

enum class FormFieldType {
    TEXT, DATE, DATETIME, DROPDOWN, PRIORITY, ATTACHMENT
}

data class FormField(
    val id: String,
    val label: String,
    val placeholder: String = "",
    val isRequired: Boolean = false,
    val hint: String? = null,
    val type: FormFieldType = FormFieldType.TEXT,
    val options: List<String> = emptyList(),
    val errorMessage: String? = null,
    val step: Int = 1, // NOVO: Define em que passo do Wizard o campo aparece
    val maxLength: Int? = null // NOVO: Para o contador de caracteres
)

data class UniversalFormData(
    val title: String,
    val fields: List<FormField>
)
