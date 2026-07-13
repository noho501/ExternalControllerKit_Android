package com.noho501.externalcontrollerkit.manager

import com.noho501.externalcontrollerkit.models.Mapping

internal object MappingResolver {
    fun replaceMapping(
        mappings: List<Mapping>,
        deviceId: String,
        inputId: String,
        actionId: String,
    ): List<Mapping> = buildList {
        addAll(
            mappings.filterNot { mapping ->
                (mapping.deviceId == deviceId && mapping.inputId == inputId) ||
                    (mapping.deviceId == deviceId && mapping.actionId == actionId)
            }
        )
        add(Mapping(deviceId = deviceId, inputId = inputId, actionId = actionId))
    }

    fun mappingForAction(
        mappings: List<Mapping>,
        actionId: String,
        deviceId: String,
    ): Mapping? = mappings.firstOrNull { it.actionId == actionId && it.deviceId == deviceId }
}
