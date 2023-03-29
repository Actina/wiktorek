package pl.gov.mf.etoll.core.watchdog.wakelock

interface WakelockController {
    fun applyLock()
    fun releaseLock()
}