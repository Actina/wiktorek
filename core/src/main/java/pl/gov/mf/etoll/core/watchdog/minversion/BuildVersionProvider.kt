package pl.gov.mf.etoll.core.watchdog.minversion

interface BuildVersionProvider {
    fun getCurrentBuildVersion(): Int
}