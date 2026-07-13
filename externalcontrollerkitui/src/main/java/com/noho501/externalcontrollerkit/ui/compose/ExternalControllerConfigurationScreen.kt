package com.noho501.externalcontrollerkit.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.noho501.externalcontrollerkit.models.ActionDefinition
import com.noho501.externalcontrollerkit.models.Device
import com.noho501.externalcontrollerkit.models.Mapping
import com.noho501.externalcontrollerkit.ui.config.ExternalControllerUiConfiguration
import com.noho501.externalcontrollerkit.ui.localization.AndroidLocalizationProvider
import com.noho501.externalcontrollerkit.ui.localization.LocalizationProvider
import com.noho501.externalcontrollerkit.ui.viewmodel.ExternalControllerUiState
import com.noho501.externalcontrollerkit.ui.viewmodel.ExternalControllerViewModel
import androidx.compose.foundation.lazy.items as itemsList
import androidx.compose.foundation.lazy.grid.items as itemsGrid

@Composable
fun ExternalControllerConfigurationRoute(
    configuration: ExternalControllerUiConfiguration,
    viewModel: ExternalControllerViewModel = hiltViewModel(),
) {
    LaunchedEffect(configuration.actions) {
        viewModel.configure(configuration.actions)
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val localization = configuration.localizationProvider ?: AndroidLocalizationProvider(LocalContext.current)
    ExternalControllerConfigurationScreen(
        uiState = uiState,
        localization = localization,
        inputLabelFormatter = configuration.inputLabelFormatter,
        onSelectDevice = viewModel::selectDevice,
        onStartListening = viewModel::startListening,
        onStopListening = viewModel::stopListening,
        onClearMapping = viewModel::clearMapping,
        onResetAll = viewModel::resetAllMappings,
        onRefresh = viewModel::refreshDevices,
        onSetInputEnabled = viewModel::setInputEnabled,
        onClose = configuration.onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExternalControllerConfigurationScreen(
    uiState: ExternalControllerUiState,
    localization: LocalizationProvider,
    inputLabelFormatter: (String) -> String,
    onSelectDevice: (String?) -> Unit,
    onStartListening: (String) -> Unit,
    onStopListening: () -> Unit,
    onClearMapping: (String) -> Unit,
    onResetAll: () -> Unit,
    onRefresh: () -> Unit,
    onSetInputEnabled: (Boolean) -> Unit,
    onClose: (() -> Unit)?,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(localization.title) },
                navigationIcon = {
                    if (onClose != null) {
                        TextButton(onClick = onClose) { Text(localization.close) }
                    }
                },
                actions = {
                    TextButton(onClick = onResetAll) {
                        Text(localization.resetAll)
                    }
                },
            )
        },
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.managerState.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                DeviceSelector(
                    devices = uiState.devices,
                    selectedDevice = uiState.selectedDevice,
                    localization = localization,
                    onSelectDevice = onSelectDevice,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = localization.runtimeInputEnabled,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Switch(
                        checked = uiState.managerState.isInputEnabled,
                        onCheckedChange = onSetInputEnabled,
                    )
                }
                if (uiState.managerState.isListening) {
                    AssistChip(
                        onClick = onStopListening,
                        label = { Text(localization.pressAnyButton) },
                    )
                }
                ActionMappings(
                    actions = uiState.actions,
                    selectedDevice = uiState.selectedDevice,
                    mappings = uiState.mappings,
                    listeningActionId = uiState.managerState.listeningActionId,
                    unmappedLabel = localization.unmapped,
                    mapLabel = localization.map,
                    cancelLabel = localization.cancel,
                    clearLabel = localization.clear,
                    inputLabelFormatter = inputLabelFormatter,
                    onStartListening = onStartListening,
                    onStopListening = onStopListening,
                    onClearMapping = onClearMapping,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviceSelector(
    devices: List<Device>,
    selectedDevice: Device?,
    localization: LocalizationProvider,
    onSelectDevice: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            readOnly = true,
            value = selectedDevice?.name ?: localization.noDevices,
            onValueChange = {},
            label = { Text(localization.deviceLabel) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            devices.forEach { device ->
                DropdownMenuItem(
                    text = { Text(device.name) },
                    onClick = {
                        expanded = false
                        onSelectDevice(device.id)
                    },
                )
            }
        }
    }
}

@Composable
private fun ActionMappings(
    actions: List<ActionDefinition>,
    selectedDevice: Device?,
    mappings: List<Mapping>,
    listeningActionId: String?,
    unmappedLabel: String,
    mapLabel: String,
    cancelLabel: String,
    clearLabel: String,
    inputLabelFormatter: (String) -> String,
    onStartListening: (String) -> Unit,
    onStopListening: () -> Unit,
    onClearMapping: (String) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val itemsModifier = Modifier.fillMaxWidth()

        if (this.maxWidth >= 700.dp) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 280.dp),
                modifier = itemsModifier,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsGrid(actions, key = { it.actionId }) { action ->
                    ActionMappingCard(
                        action = action,
                        selectedDevice = selectedDevice,
                        mapping = selectedDevice?.let { device ->
                            mappings.firstOrNull { it.actionId == action.actionId && it.deviceId == device.id }
                        },
                        isListening = listeningActionId == action.actionId,
                        unmappedLabel = unmappedLabel,
                        mapLabel = mapLabel,
                        cancelLabel = cancelLabel,
                        clearLabel = clearLabel,
                        inputLabelFormatter = inputLabelFormatter,
                        onStartListening = onStartListening,
                        onStopListening = onStopListening,
                        onClearMapping = onClearMapping,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = itemsModifier,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsList(actions, key = { it.actionId }) { action ->
                    ActionMappingCard(
                        action = action,
                        selectedDevice = selectedDevice,
                        mapping = selectedDevice?.let { device ->
                            mappings.firstOrNull { it.actionId == action.actionId && it.deviceId == device.id }
                        },
                        isListening = listeningActionId == action.actionId,
                        unmappedLabel = unmappedLabel,
                        mapLabel = mapLabel,
                        cancelLabel = cancelLabel,
                        clearLabel = clearLabel,
                        inputLabelFormatter = inputLabelFormatter,
                        onStartListening = onStartListening,
                        onStopListening = onStopListening,
                        onClearMapping = onClearMapping,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionMappingCard(
    action: ActionDefinition,
    selectedDevice: Device?,
    mapping: Mapping?,
    isListening: Boolean,
    unmappedLabel: String,
    mapLabel: String,
    cancelLabel: String,
    clearLabel: String,
    inputLabelFormatter: (String) -> String,
    onStartListening: (String) -> Unit,
    onStopListening: () -> Unit,
    onClearMapping: (String) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(action.displayTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            action.description?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
            HorizontalDivider()
            Text(
                text = mapping?.let { inputLabelFormatter(it.inputId) } ?: unmappedLabel,
                style = MaterialTheme.typography.bodyLarge,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    enabled = selectedDevice != null,
                    onClick = {
                        if (isListening) {
                            onStopListening()
                        } else {
                            onStartListening(action.actionId)
                        }
                    },
                ) {
                    Text(if (isListening) cancelLabel else mapLabel)
                }
                if (mapping != null) {
                    OutlinedButton(onClick = { onClearMapping(action.actionId) }) {
                        Text(clearLabel)
                    }
                }
            }
        }
    }
}
