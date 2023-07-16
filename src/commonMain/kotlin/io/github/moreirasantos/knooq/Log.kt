package io.github.moreirasantos.knooq

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.Marker
import io.github.oshai.kotlinlogging.KLogger as ActualKLogger

internal object KnooqMarker : Marker {
    override fun getName() = "KNOOQ"
}

internal class KLogger(private val actualKLogger: ActualKLogger) : ActualKLogger by actualKLogger {
    constructor(name: String) : this(KotlinLogging.logger(name))

    override fun debug(message: () -> Any?) = debug(null as Throwable?, KnooqMarker, message)

// Might want to override this to give the ability to turn on/off logs for knOOQ
// override fun isLoggingEnabledFor(level: Level, marker: Marker?) = level.isLoggingEnabled() && ?
}
