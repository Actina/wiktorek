package pl.gov.mf.etoll.core.watchdog.minversion

import pl.gov.mf.mobile.networking.api.interceptors.MinimumVersionTriggersCallbacks

interface WatchdogMinVersionController : MinimumVersionTriggersCallbacks {
    fun intervalCheck()
    fun setCallbacks(callbacks: WatchdogMinVersionControllerCallbacks, tag: String)
    fun removeCallbacks(tag: String)
}

interface WatchdogMinVersionControllerCallbacks {
    fun onVersionTooOldDetected()
}