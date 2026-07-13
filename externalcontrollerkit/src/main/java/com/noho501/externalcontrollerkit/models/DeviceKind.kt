package com.noho501.externalcontrollerkit.models

import kotlinx.serialization.Serializable

@Serializable
enum class DeviceKind {
    GAME_CONTROLLER,
    KEYBOARD,
    MIDI,
}
