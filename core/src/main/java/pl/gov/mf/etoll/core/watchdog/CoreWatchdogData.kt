package pl.gov.mf.etoll.core.watchdog

import org.joda.time.DateTime

data class CoreWatchdogData(
    val lastStatusUpdate: DateTime?
)