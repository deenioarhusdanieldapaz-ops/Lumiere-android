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
    val errorMessage: String? = null
)

data class UniversalFormData(
    val title: String,
    val fields: List<FormField>
)
