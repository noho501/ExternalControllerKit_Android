package com.noho501.externalcontrollerkit.demo

import com.noho501.externalcontrollerkit.models.ActionDefinition

object DemoActions {
    val actions = listOf(
        ActionDefinition(actionId = "play_pause", displayTitle = "Play / Pause", sortOrder = 0),
        ActionDefinition(actionId = "next", displayTitle = "Next", sortOrder = 1),
        ActionDefinition(actionId = "previous", displayTitle = "Previous", sortOrder = 2),
        ActionDefinition(actionId = "volume_up", displayTitle = "Volume Up", sortOrder = 3),
        ActionDefinition(actionId = "volume_down", displayTitle = "Volume Down", sortOrder = 4),
        ActionDefinition(actionId = "screenshot", displayTitle = "Screenshot", sortOrder = 5),
    )
}
