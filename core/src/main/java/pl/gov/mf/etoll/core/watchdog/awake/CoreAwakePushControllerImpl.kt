package pl.gov.mf.etoll.core.watchdog.awake

import com.google.firebase.messaging.RemoteMessage
import pl.gov.mf.etoll.core.watchdog.sending.CoreWatchdogSendingQueueController
import pl.gov.mf.etoll.core.watchdog.status.CoreWatchdogStatusUpdateController
import javax.inject.Inject

class CoreAwakePushControllerImpl @Inject constructor(
    private val sendingQueueController: CoreWatchdogSendingQueueController,
    private val statusUpdateController: CoreWatchdogStatusUpdateController
) : CoreAwakePushController {
    override fun onAwakePushReceived(
        msg: RemoteMessage,
        systemsLoaded: Boolean,
        appInForeground: Boolean
    ) {
        if (systemsLoaded) {
            sendingQueueController.onCheckConditionsRequested()
            statusUpdateController.onUpdateStatusRemoteRequest()
        }
    }

    private fun RemoteMessage.Notification.isValid(): Boolean =
        !title.isNullOrEmpty() && !body.isNullOrEmpty()
}