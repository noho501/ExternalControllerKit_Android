package com.noho501.externalcontrollerkit.models

data class ActionEvent(
    val actionId: String,
    val deviceId: String,
    val inputId: String,
    val value: InputValue,
    val timestampMillis: Long = System.currentTimeMillis(),
)
