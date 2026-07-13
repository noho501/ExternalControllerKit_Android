package com.noho501.externalcontrollerkit.manager

import android.view.KeyEvent
import android.view.MotionEvent
import com.noho501.externalcontrollerkit.logger.Logger
import com.noho501.externalcontrollerkit.models.ActionDefinition
import com.noho501.externalcontrollerkit.models.ActionEvent
import com.noho501.externalcontrollerkit.models.Device
import com.noho501.externalcontrollerkit.models.InputEvent
import com.noho501.externalcontrollerkit.models.ManagerState
import com.noho501.externalcontrollerkit.models.Mapping
import com.noho501.externalcontrollerkit.providers.ExternalControllerProvider
import com.noho501.externalcontrollerkit.providers.GameControllerProvider
import com.noho501.externalcontrollerkit.providers.KeyboardProvider
import com.noho501.externalcontrollerkit.providers.MidiProvider
import com.noho501.externalcontrollerkit.storage.MappingStorage
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Singleton
class ExternalController @Inject constructor(
    gameControllerProvider: GameControllerProvider,
    keyboardProvider: KeyboardProvider,
    midiProvider: MidiProvider,
    private val mappingStorage: MappingStorage,
    private val logger: Logger,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val providers: List<ExternalControllerProvider> = listOf(
        gameControllerProvider,
        keyboardProvider,
        midiProvider,
    )

    private val _connectedDevices = MutableStateFlow<List<Device>>(emptyList())
    private val _selectedDevice = MutableStateFlow<Device?>(null)
    private val _mappings = MutableStateFlow<List<Mapping>>(emptyList())
    private val _managerState = MutableStateFlow(ManagerState())
    private val _actionEvents = MutableSharedFlow<ActionEvent>(extraBufferCapacity = 32)
    private val _inputEvents = MutableSharedFlow<InputEvent>(extraBufferCapacity = 32)
    private val _actions = MutableStateFlow<List<ActionDefinition>>(emptyList())

    val connectedDevices: StateFlow<List<Device>> = _connectedDevices.asStateFlow()
    val selectedDevice: StateFlow<Device?> = _selectedDevice.asStateFlow()
    val mappings: StateFlow<List<Mapping>> = _mappings.asStateFlow()
    val managerState: StateFlow<ManagerState> = _managerState.asStateFlow()
    val actionEvents: SharedFlow<ActionEvent> = _actionEvents.asSharedFlow()
    val inputEvents: SharedFlow<InputEvent> = _inputEvents.asSharedFlow()
    val actionDefinitions: StateFlow<List<ActionDefinition>> = _actions.asStateFlow()

    init {
        scope.launch {
            _mappings.value = mappingStorage.loadMappings()
        }
        providers.forEach { provider ->
            provider.start(scope)
            scope.launch {
                provider.connectedDevices.collectLatest { synchronizeDevices() }
            }
            scope.launch {
                provider.inputEvents.collectLatest(::handleInputEvent)
            }
        }
        scope.launch {
            refreshConnectedDevices()
        }
    }

    fun configure(actions: List<ActionDefinition>) {
        _actions.value = actions.sortedWith(
            compareBy<ActionDefinition> { it.sortOrder ?: Int.MAX_VALUE }
                .thenBy { it.displayTitle.lowercase() }
        )
    }

    suspend fun refreshConnectedDevices() {
        _managerState.update { it.copy(isRefreshing = true) }
        providers.forEach { provider ->
            runCatching { provider.refreshConnectedDevices() }
                .onFailure { logger.error("Failed to refresh ${provider.providerKind}", it) }
        }
        synchronizeDevices()
        _managerState.update { it.copy(isRefreshing = false) }
    }

    fun setSelectedDevice(deviceId: String?) {
        val selected = deviceId?.let { id -> _connectedDevices.value.firstOrNull { it.id == id } }
            ?: _connectedDevices.value.firstOrNull()
        _selectedDevice.value = selected
    }

    fun setInputEnabled(enabled: Boolean) {
        _managerState.update { it.copy(isInputEnabled = enabled) }
    }

    fun startListening(actionId: String) {
        _managerState.update { it.copy(listeningActionId = actionId) }
    }

    fun stopListening() {
        _managerState.update { it.copy(listeningActionId = null) }
    }

    fun mappingFor(actionId: String, deviceId: String): Mapping? =
        MappingResolver.mappingForAction(_mappings.value, actionId, deviceId)

    fun clearMapping(actionId: String, deviceId: String? = _selectedDevice.value?.id) {
        val activeDeviceId = deviceId ?: return
        scope.launch {
            _mappings.update { current ->
                current.filterNot { it.actionId == actionId && it.deviceId == activeDeviceId }
            }
            persistMappings(clearWhenEmpty = _mappings.value.isEmpty())
        }
    }

    fun resetAllMappings() {
        scope.launch {
            _mappings.value = emptyList()
            persistMappings(clearWhenEmpty = true)
        }
    }

    fun onKeyEvent(event: KeyEvent): Boolean = providers.any { it.handleKeyEvent(event) }

    fun onMotionEvent(event: MotionEvent): Boolean = providers.any { it.handleMotionEvent(event) }

    private suspend fun handleInputEvent(event: InputEvent) {
        _inputEvents.emit(event)
        val selectedDeviceId = _selectedDevice.value?.id
        val listeningActionId = _managerState.value.listeningActionId
        if (listeningActionId != null) {
            if (event.deviceId == selectedDeviceId && event.value.isAssignmentCandidate()) {
                assign(event.deviceId, event.inputId, listeningActionId)
                stopListening()
            }
            return
        }
        if (!_managerState.value.isInputEnabled || !event.value.isTriggeringValue()) return
        val mapping = _mappings.value.firstOrNull {
            it.deviceId == event.deviceId && it.inputId == event.inputId
        } ?: return
        _actionEvents.emit(
            ActionEvent(
                actionId = mapping.actionId,
                deviceId = event.deviceId,
                inputId = event.inputId,
                value = event.value,
                timestampMillis = event.timestampMillis,
            )
        )
    }

    private fun assign(deviceId: String, inputId: String, actionId: String) {
        scope.launch {
            _mappings.update { current ->
                MappingResolver.replaceMapping(current, deviceId, inputId, actionId)
            }
            persistMappings(clearWhenEmpty = false)
        }
    }

    private suspend fun persistMappings(clearWhenEmpty: Boolean) {
        runCatching {
            if (clearWhenEmpty && _mappings.value.isEmpty()) {
                mappingStorage.clearMappings()
            } else {
                mappingStorage.saveMappings(_mappings.value)
            }
        }.onFailure { logger.error("Failed to persist mappings", it) }
    }

    private fun synchronizeDevices() {
        val devices = providers
            .flatMap { it.connectedDevices.value }
            .distinctBy { it.id }
            .sortedWith(compareBy<Device> { it.kind.name }.thenBy { it.name.lowercase() })
        _connectedDevices.value = devices
        val selectedId = _selectedDevice.value?.id
        _selectedDevice.value = devices.firstOrNull { it.id == selectedId } ?: devices.firstOrNull()
    }
}
