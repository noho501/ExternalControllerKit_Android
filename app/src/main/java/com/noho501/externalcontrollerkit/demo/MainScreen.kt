package com.noho501.externalcontrollerkit.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.noho501.externalcontrollerkit.ui.compose.ExternalControllerConfigurationRoute
import com.noho501.externalcontrollerkit.ui.config.ExternalControllerUiConfiguration

@Composable
fun MainScreen(
    viewModel: DemoViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            DemoHomeScreen(
                uiState = uiState,
                onOpenExternalController = { navController.navigate("controller") },
            )
        }
        composable("controller") {
            ExternalControllerConfigurationRoute(
                configuration = ExternalControllerUiConfiguration(
                    actions = DemoActions.actions,
                    onClose = { navController.popBackStack() },
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DemoHomeScreen(
    uiState: DemoUiState,
    onOpenExternalController: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.app_name)) })
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Button(onClick = onOpenExternalController) {
                    Text(stringResource(R.string.open_external_controller))
                }
            }
            item {
                SummaryCard(
                    title = stringResource(R.string.current_action),
                    body = uiState.currentAction ?: stringResource(R.string.none),
                )
            }
            item {
                SummaryCard(
                    title = stringResource(R.string.connected_devices),
                    body = if (uiState.connectedDevices.isEmpty()) {
                        stringResource(R.string.none)
                    } else {
                        uiState.connectedDevices.joinToString { it.name }
                    },
                )
            }
            item {
                Text(
                    text = stringResource(R.string.sample_actions),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            items(DemoActions.actions, key = { it.actionId }) { action ->
                Text(text = "• ${action.displayTitle}")
            }
            item {
                Text(
                    text = stringResource(R.string.live_event_log),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            items(uiState.eventLog) { log ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = log,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    body: String,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = body, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
