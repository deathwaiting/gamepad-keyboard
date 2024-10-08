package io.github.deathwaiting.gamepadkeyboard.utils

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.core.util.StatusPrinter
import org.slf4j.LoggerFactory
import java.util.*


object SysPropLogbackConfigurator {
    const val PROP_PREFIX = "log."
    fun configLogsFromSysProperties() {
        (LoggerFactory.getLogger("root") as Logger).level = Level.INFO

        System.getProperties().stringPropertyNames().stream()
            .filter { name: String ->
                name.startsWith(
                    PROP_PREFIX
                )
            }
            .forEach(::applyProp)

        // Print logback configuration status
        val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
        StatusPrinter.printIfErrorsOccured(loggerContext)
    }

    private fun applyProp(name: String) {
        val loggerName = name.substring(PROP_PREFIX.length)
        val levelStr = System.getProperty(name, "")
        val level = Level.toLevel(levelStr.uppercase(Locale.getDefault()), null)
        (LoggerFactory.getLogger(loggerName) as Logger).level = level
    }
}

