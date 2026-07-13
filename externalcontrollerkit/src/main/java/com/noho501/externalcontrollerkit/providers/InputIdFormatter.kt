package com.noho501.externalcontrollerkit.providers

import android.view.InputDevice
import android.view.KeyEvent
import kotlin.math.abs

internal object InputIdFormatter {
    fun keyboardKey(keyCode: Int): String = "key_" + KeyEvent.keyCodeToString(keyCode)
        .removePrefix("KEYCODE_")
        .lowercase()

    fun gamepadKey(keyCode: Int): String = when (keyCode) {
        KeyEvent.KEYCODE_BUTTON_A -> "button_a"
        KeyEvent.KEYCODE_BUTTON_B -> "button_b"
        KeyEvent.KEYCODE_BUTTON_X -> "button_x"
        KeyEvent.KEYCODE_BUTTON_Y -> "button_y"
        KeyEvent.KEYCODE_BUTTON_L1 -> "left_shoulder"
        KeyEvent.KEYCODE_BUTTON_R1 -> "right_shoulder"
        KeyEvent.KEYCODE_BUTTON_L2 -> "left_trigger"
        KeyEvent.KEYCODE_BUTTON_R2 -> "right_trigger"
        KeyEvent.KEYCODE_BUTTON_THUMBL -> "left_stick_button"
        KeyEvent.KEYCODE_BUTTON_THUMBR -> "right_stick_button"
        KeyEvent.KEYCODE_DPAD_UP -> "dpad_up"
        KeyEvent.KEYCODE_DPAD_DOWN -> "dpad_down"
        KeyEvent.KEYCODE_DPAD_LEFT -> "dpad_left"
        KeyEvent.KEYCODE_DPAD_RIGHT -> "dpad_right"
        KeyEvent.KEYCODE_BUTTON_START -> "button_start"
        KeyEvent.KEYCODE_BUTTON_SELECT -> "button_select"
        else -> keyboardKey(keyCode)
    }

    fun axis(axis: Int): String = when (axis) {
        MotionAxis.LEFT_X -> "left_stick_x"
        MotionAxis.LEFT_Y -> "left_stick_y"
        MotionAxis.RIGHT_X -> "right_stick_x"
        MotionAxis.RIGHT_Y -> "right_stick_y"
        MotionAxis.HAT_X -> "dpad_hat_x"
        MotionAxis.HAT_Y -> "dpad_hat_y"
        MotionAxis.LTRIGGER -> "left_trigger_axis"
        MotionAxis.RTRIGGER -> "right_trigger_axis"
        else -> "axis_${axis}"
    }

    fun axisLabel(inputId: String, value: Double): String {
        if (!inputId.startsWith("dpad_hat_")) return inputId
        return when (inputId) {
            "dpad_hat_x" -> if (value < 0) "dpad_left" else "dpad_right"
            "dpad_hat_y" -> if (value < 0) "dpad_up" else "dpad_down"
            else -> inputId
        }
    }

    fun deviceId(prefix: String, device: InputDevice): String = buildString {
        append(prefix)
        append(':')
        append(device.descriptor ?: device.id)
    }

    fun shouldEmitAxis(value: Float, threshold: Float = 0.5f): Boolean = abs(value) >= threshold
}

private object MotionAxis {
    const val LEFT_X = android.view.MotionEvent.AXIS_X
    const val LEFT_Y = android.view.MotionEvent.AXIS_Y
    const val RIGHT_X = android.view.MotionEvent.AXIS_Z
    const val RIGHT_Y = android.view.MotionEvent.AXIS_RZ
    const val HAT_X = android.view.MotionEvent.AXIS_HAT_X
    const val HAT_Y = android.view.MotionEvent.AXIS_HAT_Y
    const val LTRIGGER = android.view.MotionEvent.AXIS_LTRIGGER
    const val RTRIGGER = android.view.MotionEvent.AXIS_RTRIGGER
}
