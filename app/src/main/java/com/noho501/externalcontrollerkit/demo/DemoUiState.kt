package com.noho501.externalcontrollerkit.demo

import com.noho501.externalcontrollerkit.models.Device

data class DemoUiState(
    val currentAction: String? = null,
    val connectedDevices: List<Device> = emptyList(),
    val eventLog: List<String> = emptyList(),
)
