package com.noho501.externalcontrollerkit.ui.config

import com.noho501.externalcontrollerkit.models.ActionDefinition
import com.noho501.externalcontrollerkit.ui.localization.LocalizationProvider

data class ExternalControllerUiConfiguration(
    val actions: List<ActionDefinition>,
    val localizationProvider: LocalizationProvider? = null,
    val inputLabelFormatter: (String) -> String = { inputId ->
        inputId.replace('_', ' ').replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    },
    val onClose: (() -> Unit)? = null,
)
