package com.noho501.externalcontrollerkit.models

data class InputEvent(
    val deviceId: String,
    val inputId: String,
    val value: InputValue,
    val deviceKind: DeviceKind,
    val timestampMillis: Long = System.currentTimeMillis(),
)
