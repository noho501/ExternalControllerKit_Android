package com.noho501.externalcontrollerkit.providers

import android.view.KeyEvent
import android.view.MotionEvent
import com.noho501.externalcontrollerkit.models.Device
import com.noho501.externalcontrollerkit.models.DeviceKind
import com.noho501.externalcontrollerkit.models.InputEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface ExternalControllerProvider {
    val providerKind: DeviceKind
    val connectedDevices: StateFlow<List<Device>>
    val inputEvents: SharedFlow<InputEvent>

    fun start(scope: CoroutineScope)
    fun stop()
    suspend fun refreshConnectedDevices()
    fun handleKeyEvent(event: KeyEvent): Boolean = false
    fun handleMotionEvent(event: MotionEvent): Boolean = false
}
