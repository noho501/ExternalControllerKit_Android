package com.noho501.externalcontrollerkit.ui.viewmodel

import com.noho501.externalcontrollerkit.models.ActionDefinition
import com.noho501.externalcontrollerkit.models.Device
import com.noho501.externalcontrollerkit.models.ManagerState
import com.noho501.externalcontrollerkit.models.Mapping

data class ExternalControllerUiState(
    val devices: List<Device> = emptyList(),
    val selectedDevice: Device? = null,
    val actions: List<ActionDefinition> = emptyList(),
    val mappings: List<Mapping> = emptyList(),
    val managerState: ManagerState = ManagerState(),
)
