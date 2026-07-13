package com.noho501.externalcontrollerkit.providers

import android.content.Context
import android.hardware.input.InputManager
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import com.noho501.externalcontrollerkit.logger.Logger
import com.noho501.externalcontrollerkit.models.Device
import com.noho501.externalcontrollerkit.models.DeviceKind
import com.noho501.externalcontrollerkit.models.InputEvent
import com.noho501.externalcontrollerkit.models.InputValue
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class GameControllerProvider @Inject constructor(
    @ApplicationContext context: Context,
    logger: Logger,
) : AbstractInputDeviceProvider(
    inputManager = context.getSystemService(InputManager::class.java),
    logger = logger,
) {
    override val providerKind: DeviceKind = DeviceKind.GAME_CONTROLLER

    override fun handleKeyEvent(event: KeyEvent): Boolean {
        val device = lookupDevice(event.deviceId) ?: return false
        if (!matches(device)) return false
        return mutableInputEvents.tryEmit(
            InputEvent(
                deviceId = toDevice(device).id,
                inputId = InputIdFormatter.gamepadKey(event.keyCode),
                value = InputValue.button(event.action == KeyEvent.ACTION_DOWN),
                deviceKind = providerKind,
            )
        )
    }

    override fun handleMotionEvent(event: MotionEvent): Boolean {
        val device = lookupDevice(event.deviceId) ?: return false
        if (!matches(device)) return false
        val trackedAxes = listOf(
            MotionEvent.AXIS_X,
            MotionEvent.AXIS_Y,
            MotionEvent.AXIS_Z,
            MotionEvent.AXIS_RZ,
            MotionEvent.AXIS_HAT_X,
            MotionEvent.AXIS_HAT_Y,
            MotionEvent.AXIS_LTRIGGER,
            MotionEvent.AXIS_RTRIGGER,
        )
        var handled = false
        trackedAxes.forEach { axis ->
            val value = event.getAxisValue(axis)
            if (InputIdFormatter.shouldEmitAxis(value)) {
                val rawInputId = InputIdFormatter.axis(axis)
                handled = mutableInputEvents.tryEmit(
                    InputEvent(
                        deviceId = toDevice(device).id,
                        inputId = InputIdFormatter.axisLabel(rawInputId, value.toDouble()),
                        value = InputValue.axis(value.toDouble()),
                        deviceKind = providerKind,
                    )
                ) || handled
            }
        }
        return handled
    }

    override fun matches(device: InputDevice): Boolean {
        val sources = device.sources
        val isGamepad = sources and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD
        val isJoystick = sources and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK
        return isGamepad || isJoystick
    }

    override fun toDevice(device: InputDevice): Device = Device(
        id = InputIdFormatter.deviceId("gamepad", device),
        name = device.name ?: "Game Controller",
        kind = providerKind,
        descriptor = device.descriptor,
        vendorId = device.vendorId,
        productId = device.productId,
    )
}
