package com.noho501.externalcontrollerkit.logger

import android.util.Log
import javax.inject.Inject

interface Logger {
    fun debug(message: String)
    fun error(message: String, throwable: Throwable? = null)
}

object NoOpLogger : Logger {
    override fun debug(message: String) = Unit
    override fun error(message: String, throwable: Throwable?) = Unit
}

class ConsoleLogger @Inject constructor(
    private val tag: String = "ExternalController",
) : Logger {
    override fun debug(message: String) {
        Log.d(tag, message)
    }

    override fun error(message: String, throwable: Throwable?) {
        Log.e(tag, message, throwable)
    }
}
