package pl.gov.mf.etoll.core.watchdog.interfaces

// left for refactoring of critical messages
interface GpsNotAccessibleDelegate {
    fun onGpsBecomeInaccessible()
    fun onGpsBecomeAccessibleAgain()
}