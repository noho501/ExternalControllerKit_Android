package com.noho501.externalcontrollerkit.models

import kotlin.math.abs
import kotlinx.serialization.Serializable

@Serializable
data class InputValue(
    val kind: String,
    val boolValue: Boolean? = null,
    val doubleValue: Double? = null,
    val integerValue: Int? = null,
) {
    fun isAssignmentCandidate(threshold: Double = 0.5): Boolean = when (kind) {
        KIND_BUTTON -> boolValue == true
        KIND_AXIS -> abs(doubleValue ?: 0.0) >= threshold
        KIND_INTEGER -> (integerValue ?: 0) > 0
        else -> false
    }

    fun isTriggeringValue(threshold: Double = 0.5): Boolean = isAssignmentCandidate(threshold)

    companion object {
        const val KIND_BUTTON = "button"
        const val KIND_AXIS = "axis"
        const val KIND_INTEGER = "integer"

        fun button(isPressed: Boolean) = InputValue(kind = KIND_BUTTON, boolValue = isPressed)
        fun axis(value: Double) = InputValue(kind = KIND_AXIS, doubleValue = value)
        fun integer(value: Int) = InputValue(kind = KIND_INTEGER, integerValue = value)
    }
}
