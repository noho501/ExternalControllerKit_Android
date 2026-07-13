package com.noho501.externalcontrollerkit.models

import kotlinx.serialization.Serializable

@Serializable
data class Device(
    val id: String,
    val name: String,
    val kind: DeviceKind,
    val descriptor: String? = null,
    val vendorId: Int? = null,
    val productId: Int? = null,
)
