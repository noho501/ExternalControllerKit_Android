package com.noho501.externalcontrollerkit.models

data class ManagerState(
    val isInputEnabled: Boolean = true,
    val isRefreshing: Boolean = false,
    val listeningActionId: String? = null,
) {
    val isListening: Boolean get() = listeningActionId != null
}
