package com.noho501.externalcontrollerkit.providers

import android.hardware.input.InputManager
import android.view.InputDevice
import com.noho501.externalcontrollerkit.logger.Logger
import com.noho501.externalcontrollerkit.models.Device
import com.noho501.externalcontrollerkit.models.DeviceKind
import com.noho501.externalcontrollerkit.models.InputEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

internal abstract class AbstractInputDeviceProvider(
    private val inputManager: InputManager,
    private val logger: Logger,
) : ExternalControllerProvider, InputManager.InputDeviceListener {

    protected val mutableConnectedDevices = MutableStateFlow<List<Device>>(emptyList())
    protected val mutableInputEvents = MutableSharedFlow<InputEvent>(extraBufferCapacity = 32)
    protected var scope: CoroutineScope? = null

    override val connectedDevices: StateFlow<List<Device>> = mutableConnectedDevices
    override val inputEvents: SharedFlow<InputEvent> = mutableInputEvents

    override fun start(scope: CoroutineScope) {
        this.scope = scope
        inputManager.registerInputDeviceListener(this, null)
    }

    override fun stop() {
        inputManager.unregisterInputDeviceListener(this)
        scope = null
        mutableConnectedDevices.value = emptyList()
    }

    override suspend fun refreshConnectedDevices() {
        val devices = inputManager.inputDeviceIds
            .mapNotNull { inputManager.getInputDevice(it) }
            .filter(::matches)
            .map(::toDevice)
            .sortedWith(compareBy<Device> { it.kind.name }.thenBy { it.name.lowercase() })
        mutableConnectedDevices.value = devices
        logger.debug("${providerKind.name} devices refreshed: ${devices.size}")
    }

    override fun onInputDeviceAdded(deviceId: Int) {
        scope?.let { refreshAsync(it) }
    }

    override fun onInputDeviceRemoved(deviceId: Int) {
        scope?.let { refreshAsync(it) }
    }

    override fun onInputDeviceChanged(deviceId: Int) {
        scope?.let { refreshAsync(it) }
    }

    protected fun lookupDevice(deviceId: Int): InputDevice? = inputManager.getInputDevice(deviceId)

    protected abstract fun matches(device: InputDevice): Boolean
    protected abstract fun toDevice(device: InputDevice): Device
    override abstract val providerKind: DeviceKind

    private fun refreshAsync(scope: CoroutineScope) {
        scope.launchCatching(logger) { refreshConnectedDevices() }
    }
}
