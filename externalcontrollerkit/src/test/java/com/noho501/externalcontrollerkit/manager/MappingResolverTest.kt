package com.noho501.externalcontrollerkit.manager

import com.google.common.truth.Truth.assertThat
import com.noho501.externalcontrollerkit.models.Mapping
import org.junit.Test

class MappingResolverTest {

    @Test
    fun replaceMapping_removesDuplicateInputAndActionPerDevice() {
        val existing = listOf(
            Mapping(deviceId = "keyboard:1", inputId = "key_enter", actionId = "play"),
            Mapping(deviceId = "keyboard:1", inputId = "key_space", actionId = "pause"),
            Mapping(deviceId = "keyboard:2", inputId = "key_space", actionId = "play"),
        )

        val resolved = MappingResolver.replaceMapping(
            mappings = existing,
            deviceId = "keyboard:1",
            inputId = "key_space",
            actionId = "play",
        )

        assertThat(resolved).containsExactly(
            Mapping(deviceId = "keyboard:2", inputId = "key_space", actionId = "play"),
            Mapping(deviceId = "keyboard:1", inputId = "key_space", actionId = "play"),
        )
    }
}
