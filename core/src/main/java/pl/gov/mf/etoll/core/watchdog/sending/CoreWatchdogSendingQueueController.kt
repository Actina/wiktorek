package pl.gov.mf.etoll.core.watchdog.sending

import io.reactivex.Single

interface CoreWatchdogSendingQueueController {
    fun onAppGoesToForeground()
    fun onAppGoesToBackground()
    fun onCheckConditionsRequested()

    companion object {
        const val SENDING_QUEUE_INTERVAL_IN_SECOND: Long = 10
    }

    fun checkIfThereAreItemsWaitingForSending(): Single<Boolean>
}