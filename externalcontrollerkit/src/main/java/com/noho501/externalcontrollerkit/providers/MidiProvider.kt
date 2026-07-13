package com.noho501.externalcontrollerkit.providers

import android.content.Context
import android.media.midi.MidiDevice
import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiManager
import android.media.midi.MidiOutputPort
import android.media.midi.MidiReceiver
import android.os.Handler
import android.os.Looper
import com.noho501.externalcontrollerkit.logger.Logger
import com.noho501.externalcontrollerkit.models.Device
import com.noho501.externalcontrollerkit.models.DeviceKind
import com.noho501.externalcontrollerkit.models.InputEvent
import com.noho501.externalcontrollerkit.models.InputValue
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

@Singleton
class MidiProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logger: Logger,
) : ExternalControllerProvider {

    private val midiManager = context.getSystemService(MidiManager::class.java)
    private val mutableConnectedDevices = MutableStateFlow<List<Device>>(emptyList())
    private val mutableInputEvents = MutableSharedFlow<InputEvent>(extraBufferCapacity = 32)
    private val openDevices = linkedMapOf<Int, MidiDevice>()
    private val openPorts = mutableListOf<OpenMidiPort>()
    private var scope: CoroutineScope? = null

    private val callback = object : MidiManager.DeviceCallback() {
        override fun onDeviceAdded(device: MidiDeviceInfo) {
            scope?.launchCatching(logger) { refreshConnectedDevices() }
            openIfNeeded(device)
        }

        override fun onDeviceRemoved(device: MidiDeviceInfo) {
            closeDevice(device.id)
            scope?.launchCatching(logger) { refreshConnectedDevices() }
        }

        override fun onDeviceStatusChanged(status: android.media.midi.MidiDeviceStatus) {
            scope?.launchCatching(logger) { refreshConnectedDevices() }
        }
    }

    override val providerKind: DeviceKind = DeviceKind.MIDI
    override val connectedDevices: StateFlow<List<Device>> = mutableConnectedDevices
    override val inputEvents: SharedFlow<InputEvent> = mutableInputEvents

    override fun start(scope: CoroutineScope) {
        this.scope = scope
        midiManager.registerDeviceCallback(callback, Handler(Looper.getMainLooper()))
        scope.launchCatching(logger) {
            refreshConnectedDevices()
            midiManager.devices.forEach(::openIfNeeded)
        }
    }

    override fun stop() {
        midiManager.unregisterDeviceCallback(callback)
        openPorts.forEach { runCatching { it.port.close() } }
        openPorts.clear()
        openDevices.values.forEach { runCatching { it.close() } }
        openDevices.clear()
        mutableConnectedDevices.value = emptyList()
        scope = null
    }

    override suspend fun refreshConnectedDevices() {
        mutableConnectedDevices.value = midiManager.devices
            .map(::toDevice)
            .sortedBy { it.name.lowercase() }
    }

    private fun openIfNeeded(info: MidiDeviceInfo) {
        if (openDevices.containsKey(info.id)) return
        midiManager.openDevice(info, { midiDevice ->
            if (midiDevice == null) return@openDevice
            openDevices[info.id] = midiDevice
            repeat(info.outputPortCount) { portIndex ->
                midiDevice.openOutputPort(portIndex)?.also { port ->
                    openPorts += OpenMidiPort(info.id, port)
                    port.connect(MidiEventReceiver(toDevice(info), mutableInputEvents, logger))
                }
            }
        }, Handler(Looper.getMainLooper()))
    }

    private fun closeDevice(deviceId: Int) {
        openDevices.remove(deviceId)?.let { runCatching { it.close() } }
        openPorts.removeAll { port ->
            val shouldRemove = port.deviceId == deviceId
            if (shouldRemove) {
                runCatching { port.port.close() }
            }
            shouldRemove
        }
    }

    private data class OpenMidiPort(
        val deviceId: Int,
        val port: MidiOutputPort,
    )

    private fun toDevice(info: MidiDeviceInfo): Device {
        val name = info.properties.getString(MidiDeviceInfo.PROPERTY_NAME)
            ?: info.properties.getString(MidiDeviceInfo.PROPERTY_MANUFACTURER)
            ?: "MIDI Device ${info.id}"
        return Device(
            id = "midi:${info.id}",
            name = name,
            kind = providerKind,
            descriptor = name,
        )
    }
}

private class MidiEventReceiver(
    private val device: Device,
    private val inputEvents: MutableSharedFlow<InputEvent>,
    private val logger: Logger,
) : MidiReceiver() {

    override fun onSend(data: ByteArray, offset: Int, count: Int, timestamp: Long) {
        if (count < 2) return
        val status = data[offset].toInt() and 0xFF
        val data1 = data.getOrNull(offset + 1)?.toInt()?.and(0xFF) ?: return
        val data2 = data.getOrNull(offset + 2)?.toInt()?.and(0xFF) ?: 0
        val command = status and 0xF0
        val inputId = when (command) {
            0x80, 0x90 -> "note_$data1"
            0xB0 -> "cc_$data1"
            0xC0 -> "program_$data1"
            0xE0 -> "pitch_bend"
            else -> "midi_${command}_$data1"
        }
        val value = when (command) {
            0x80 -> InputValue.button(false)
            0x90 -> InputValue.button(data2 > 0)
            0xB0, 0xC0 -> InputValue.integer(data2.takeIf { command == 0xB0 } ?: data1)
            0xE0 -> InputValue.integer(((data2 shl 7) or data1) - 8192)
            else -> InputValue.integer(data2)
        }
        if (!inputEvents.tryEmit(
                InputEvent(
                    deviceId = device.id,
                    inputId = inputId,
                    value = value,
                    deviceKind = DeviceKind.MIDI,
                    timestampMillis = if (timestamp > 0L) timestamp else System.currentTimeMillis(),
                )
            )
        ) {
            logger.debug("Dropped MIDI input event for ${device.id}")
        }
    }
}
