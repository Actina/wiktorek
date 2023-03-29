package pl.gov.mf.etoll.core.watchdog.netlock

interface CoreNetlockChecker {
    fun intervalCheck()
    fun markStateAsShowed()
    fun setCallbacks(callback: CoreNetlockCheckerCallbacks, tag: String)
    fun removeCallbacks(tag: String)
    fun markStateAsNotShowed()
}

interface CoreNetlockCheckerCallbacks {
    fun onShowNetworkLock()
}