package pl.gov.mf.etoll.core.watchdog.sending

import android.util.Log
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorV3
import pl.gov.mf.etoll.core.ridecoordinatorv2.sender.RideCoordinatorV3Sender
import pl.gov.mf.etoll.storage.database.ridecache.RideCacheDatabase
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC
import pl.gov.mf.mobile.utils.addSafe
import pl.gov.mf.mobile.utils.disposeSafe
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CoreWatchdogSendingQueueControllerImpl @Inject constructor(
    private val rideCacheDatabase: RideCacheDatabase,
    private val rideCoordinatorV3Sender: RideCoordinatorV3Sender,
    private val rideCoordinatorV3: RideCoordinatorV3,
    private val writeSettingsUseCase: SettingsUC.WriteSettingsUseCase

) : CoreWatchdogSendingQueueController {

    private var sendingTimerDisposable: Disposable? = null
    private val compositeDisposable = CompositeDisposable()

    /**
     * App goes to foreground, we should start sending queue
     */
    override fun onAppGoesToForeground() {
        if (sendingTimerDisposable != null && !sendingTimerDisposable!!.isDisposed) return
        // start sending queue
        startSendingQueue()
    }

    /**
     * App goes to background, if we're not during ride, we should stop sending queue
     */
    override fun onAppGoesToBackground() {
        if (rideCoordinatorV3.getConfiguration() != null && rideCoordinatorV3.getConfiguration()!!.duringRide) {
            // do nothing, we're during ride
        } else {
            // stop sending queue
            stopSendingQueue()
        }
    }

    /**
     * This function is called whenever we start ride or we receive push to update app status
     */
    override fun onCheckConditionsRequested() {
        if (sendingTimerDisposable != null && !sendingTimerDisposable!!.isDisposed) return
        if (rideCoordinatorV3.getConfiguration() == null) return
        Log.d(LOGCAT_TAG, "Check conditions")
        val config = rideCoordinatorV3.getConfiguration()!!
        if (config.duringRide) {
            // start sending queue
            startSendingQueue()
        } else {
            // check if there is anything to send and if yes - try to send it
            checkAndRequestFlushIfNeeded()
        }
    }

    override fun checkIfThereAreItemsWaitingForSending() = rideCacheDatabase.count().map { it > 0 }

    private fun stopSendingQueue() {
        Log.d(LOGCAT_TAG, "Stopping")
        sendingTimerDisposable.disposeSafe()
        sendingTimerDisposable = null
        // and try to flush all pending events
        checkAndRequestFlushIfNeeded()
    }

    private fun startSendingQueue() {
        Log.d(LOGCAT_TAG, "Starting")
        sendingTimerDisposable = Observable.interval(
            CoreWatchdogSendingQueueController.SENDING_QUEUE_INTERVAL_IN_SECOND,
            CoreWatchdogSendingQueueController.SENDING_QUEUE_INTERVAL_IN_SECOND,
            TimeUnit.SECONDS
        ).subscribe {
            Log.d(LOGCAT_TAG, "Ping")
            checkAndRequestFlushIfNeeded()
        }
    }

    private fun checkAndRequestFlushIfNeeded() {
        compositeDisposable.addSafe(rideCacheDatabase.count().subscribe({ count ->
            if (count > 0) {
                val forced = !rideCoordinatorV3.getConfiguration()!!.duringRide
                Log.d(LOGCAT_TAG, "Flush forced=$forced")
                rideCoordinatorV3Sender.checkAndSend(forced, forced)
            } else {
                // this line is making "gps" counter to work as "time since latest non-send event"
                compositeDisposable.add(
                    writeSettingsUseCase.execute(
                        Settings.LAST_CORRECT_DATA_SENDING_TIMESTAMP,
                        System.currentTimeMillis().toString()
                    ).subscribe()
                )
            }
        }, {
            Log.e(LOGCAT_TAG, "$it")
        }))
    }

    companion object {
        private const val LOGCAT_TAG = "SendingQueue"
    }
}