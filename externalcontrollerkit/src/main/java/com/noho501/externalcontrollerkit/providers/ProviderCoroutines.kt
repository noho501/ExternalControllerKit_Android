package com.noho501.externalcontrollerkit.providers

import com.noho501.externalcontrollerkit.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal fun CoroutineScope.launchCatching(
    logger: Logger,
    block: suspend CoroutineScope.() -> Unit,
) = launch {
    runCatching { block() }
        .onFailure { logger.error("Provider coroutine failed", it) }
}
