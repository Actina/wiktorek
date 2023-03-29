package pl.gov.mf.etoll.core.ridecoordinatorv2.sender

interface RideCoordinatorV3Sender {
    fun checkAndSend(forceTolled: Boolean = false, forceSent: Boolean = false)
    fun setCallbacks(callbacks: RideCoordinatorV3SenderCallbacks?, tag: String)
    fun resetTimers()
}

interface RideCoordinatorV3SenderCallbacks {
    fun onPackageSent(size: Int, ids: List<Long>, sent: Boolean)
    fun onPackageSendingError(details: String?)
}