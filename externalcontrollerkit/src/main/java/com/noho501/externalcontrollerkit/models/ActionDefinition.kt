package com.noho501.externalcontrollerkit.models

data class ActionDefinition(
    val actionId: String,
    val displayTitle: String,
    val description: String? = null,
    val sortOrder: Int? = null,
)
