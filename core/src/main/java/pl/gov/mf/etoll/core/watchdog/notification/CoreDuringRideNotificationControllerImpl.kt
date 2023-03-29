package pl.gov.mf.etoll.core.watchdog.notification

import pl.gov.mf.etoll.core.notifications.NotificationManager
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class CoreDuringRideNotificationControllerImpl @Inject constructor(private val notificationManager: NotificationManager) :
    CoreDuringRideNotificationController {

    private val notificationShouldBeVisible = AtomicBoolean(false)

    override fun setNotificationShouldBeVisible() {
        notificationShouldBeVisible.set(true)
    }

    override fun setNotificationShouldBeGone() {
        notificationShouldBeVisible.set(false)
    }

    override fun intervalCheck() {
        if (notificationShouldBeVisible.get() && !notificationManager.isAnyDuringRideNotificationVisible()) {
            notificationManager.createDuringRideNotification()
        }
    }
}