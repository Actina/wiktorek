package pl.gov.mf.etoll.core.watchdog.notification

interface CoreDuringRideNotificationController {
    fun setNotificationShouldBeVisible()
    fun setNotificationShouldBeGone()
    fun intervalCheck()
}