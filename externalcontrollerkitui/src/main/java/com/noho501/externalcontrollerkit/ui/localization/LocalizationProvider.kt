package com.noho501.externalcontrollerkit.ui.localization

import android.content.Context
import com.noho501.externalcontrollerkit.ui.R

interface LocalizationProvider {
    val title: String
    val close: String
    val resetAll: String
    val deviceLabel: String
    val noDevices: String
    val pullToRefresh: String
    val pressAnyButton: String
    val unmapped: String
    val map: String
    val cancel: String
    val clear: String
    val runtimeInputEnabled: String
}

class AndroidLocalizationProvider(context: Context) : LocalizationProvider {
    override val title: String = context.getString(R.string.external_controller_title)
    override val close: String = context.getString(R.string.external_controller_close)
    override val resetAll: String = context.getString(R.string.external_controller_reset_all)
    override val deviceLabel: String = context.getString(R.string.external_controller_device)
    override val noDevices: String = context.getString(R.string.external_controller_no_devices)
    override val pullToRefresh: String = context.getString(R.string.external_controller_pull_to_refresh)
    override val pressAnyButton: String = context.getString(R.string.external_controller_press_any_button)
    override val unmapped: String = context.getString(R.string.external_controller_unmapped)
    override val map: String = context.getString(R.string.external_controller_map)
    override val cancel: String = context.getString(R.string.external_controller_cancel)
    override val clear: String = context.getString(R.string.external_controller_clear)
    override val runtimeInputEnabled: String = context.getString(R.string.external_controller_runtime_input)
}
