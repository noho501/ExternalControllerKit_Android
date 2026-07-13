package com.noho501.externalcontrollerkit.demo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noho501.externalcontrollerkit.manager.ExternalController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DemoViewModel @Inject constructor(
    private val externalController: ExternalController,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DemoUiState())
    val uiState: StateFlow<DemoUiState> = _uiState.asStateFlow()

    init {
        externalController.configure(DemoActions.actions)
        viewModelScope.launch {
            externalController.connectedDevices.collect { devices ->
                _uiState.update {
                    it.copy(
                        connectedDevices = devices,
                        eventLog = appendLog(it.eventLog, "Devices: ${devices.joinToString { device -> device.name }}"),
                    )
                }
            }
        }
        viewModelScope.launch {
            externalController.inputEvents.collect { event ->
                _uiState.update {
                    it.copy(
                        eventLog = appendLog(it.eventLog, "Button pressed: ${event.inputId} on ${event.deviceId}"),
                    )
                }
            }
        }
        viewModelScope.launch {
            externalController.actionEvents.collect { event ->
                _uiState.update {
                    it.copy(
                        currentAction = event.actionId,
                        eventLog = appendLog(it.eventLog, "Triggered action: ${event.actionId} from ${event.inputId}"),
                    )
                }
            }
        }
    }

    private fun appendLog(current: List<String>, message: String): List<String> =
        (listOf(message) + current).take(50)
}
