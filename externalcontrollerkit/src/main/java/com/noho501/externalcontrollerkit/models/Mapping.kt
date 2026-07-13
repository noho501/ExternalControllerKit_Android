package com.noho501.externalcontrollerkit.models

import kotlinx.serialization.Serializable

@Serializable
data class Mapping(
    val deviceId: String,
    val inputId: String,
    val actionId: String,
)
