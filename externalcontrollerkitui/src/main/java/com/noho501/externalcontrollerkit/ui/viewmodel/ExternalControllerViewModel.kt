package com.noho501.externalcontrollerkit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noho501.externalcontrollerkit.manager.ExternalController
import com.noho501.externalcontrollerkit.models.ActionDefinition
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ExternalControllerViewModel @Inject constructor(
    private val externalController: ExternalController,
) : ViewModel() {

    private val actionDefinitions = MutableStateFlow<List<ActionDefinition>>(emptyList())

    val uiState: StateFlow<ExternalControllerUiState> = combine(
        externalController.connectedDevices,
        externalController.selectedDevice,
        externalController.mappings,
        externalController.managerState,
        actionDefinitions,
    ) { devices, selectedDevice, mappings, managerState, actions ->
        ExternalControllerUiState(
            devices = devices,
            selectedDevice = selectedDevice,
            mappings = mappings,
            managerState = managerState,
            actions = actions,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExternalControllerUiState(),
    )

    fun configure(actions: List<ActionDefinition>) {
        actionDefinitions.value = actions.sortedWith(
            compareBy<ActionDefinition> { it.sortOrder ?: Int.MAX_VALUE }
                .thenBy { it.displayTitle.lowercase() }
        )
        externalController.configure(actions)
    }

    fun selectDevice(deviceId: String?) {
        externalController.setSelectedDevice(deviceId)
    }

    fun startListening(actionId: String) {
        externalController.startListening(actionId)
    }

    fun stopListening() {
        externalController.stopListening()
    }

    fun clearMapping(actionId: String) {
        externalController.clearMapping(actionId)
    }

    fun resetAllMappings() {
        externalController.resetAllMappings()
    }

    fun setInputEnabled(enabled: Boolean) {
        externalController.setInputEnabled(enabled)
    }

    fun refreshDevices() {
        viewModelScope.launch {
            externalController.refreshConnectedDevices()
        }
    }
}
