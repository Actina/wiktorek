package pl.gov.mf.etoll.core.watchdog.ridefinish

interface RideFinishController {
    fun intervalCheck()
    fun onAppGoesToBackground()
    fun onAppGoesToForeground(callbacks: RideFinishCallbacks)
}

interface RideFinishCallbacks {
    fun onShowRideFinishedRequested()
}