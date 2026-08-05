package com.noho501.externalcontrollerkit.providers

import android.content.Context
import android.hardware.input.InputManager
import android.view.InputDevice
import android.view.KeyEvent
import com.noho501.externalcontrollerkit.logger.Logger
import com.noho501.externalcontrollerkit.models.Device
import com.noho501.externalcontrollerkit.models.DeviceKind
import com.noho501.externalcontrollerkit.models.InputEvent
import com.noho501.externalcontrollerkit.models.InputValue
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeyboardProvider @Inject constructor(
    @ApplicationContext context: Context,
    logger: Logger,
) : AbstractInputDeviceProvider(
    inputManager = context.getSystemService(InputManager::class.java),
    logger = logger,
) {
    override val providerKind: DeviceKind = DeviceKind.KEYBOARD

    override fun handleKeyEvent(event: KeyEvent, shouldConsume: (String, String) -> Boolean): Boolean {
        val device = lookupDevice(event.deviceId) ?: return false
        if (!matches(device)) return false

        val deviceId = toDevice(device).id
        val inputId = InputIdFormatter.keyboardKey(event.keyCode)

        if (!shouldConsume(deviceId, inputId)) return false

        return mutableInputEvents.tryEmit(
            InputEvent(
                deviceId = deviceId,
                inputId = inputId,
                value = InputValue.button(event.action == KeyEvent.ACTION_DOWN),
                deviceKind = providerKind,
            )
        )
    }

    override fun matches(device: InputDevice): Boolean {
        val sources = device.sources
        val isKeyboard = sources and InputDevice.SOURCE_KEYBOARD == InputDevice.SOURCE_KEYBOARD
        return isKeyboard && !device.isVirtual
    }

    override fun toDevice(device: InputDevice): Device = Device(
        id = InputIdFormatter.deviceId("keyboard", device),
        name = device.name ?: "Keyboard",
        kind = providerKind,
        descriptor = device.descriptor,
        vendorId = device.vendorId,
        productId = device.productId,
    )
}
