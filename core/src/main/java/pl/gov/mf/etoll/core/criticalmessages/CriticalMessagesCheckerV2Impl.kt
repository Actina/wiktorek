package pl.gov.mf.etoll.core.criticalmessages

import io.reactivex.disposables.CompositeDisposable
import pl.gov.mf.etoll.core.notifications.NotificationManager
import pl.gov.mf.etoll.core.ridehistory.collector.RideHistoryCollector
import pl.gov.mf.etoll.interfaces.*
import pl.gov.mf.mobile.utils.addSafe
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class CriticalMessagesCheckerV2Impl @Inject constructor(
    private var notificationManager: NotificationManager,
    private val dataSenderStateInterfaceController: DataSenderStateInterfaceController,
    private val gpsStateInterfaceController: GpsStateInterfaceController,
    private val batteryStateInterfaceController: BatteryStateInterfaceController,
    private val rideHistoryCollector: RideHistoryCollector
) : CriticalMessagesChecker, GpsStateInterface, BatteryStateInterface, DataSenderStateInterface {

    companion object {
        private const val CALLBACKS_TAG = "MessagesObserverV2"
        private const val NOTIFICATION_ID_BATTERY = 0
        private const val NOTIFICATION_ID_DATA_COLLECTOR = 1
        private const val NOTIFICATION_ID_DATA_SENDER = 2
    }

    private var criticalMessagesObserver: CriticalMessagesObserver? = null
    private var battery = CriticalMessageContainer(WarningsBasicLevels.GREEN)
    private var dataSender = CriticalMessageContainer(WarningsBasicLevels.GREEN)
    private var dataCollector = CriticalMessageContainer(WarningsBasicLevels.GREEN)
    private var validationIsOn = AtomicBoolean(false)

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun onAppGoesToBackground() {
        criticalMessagesObserver = null
    }

    override fun onAppGoesToForeground(criticalMessagesObserver: CriticalMessagesObserver) {
        this.criticalMessagesObserver = criticalMessagesObserver
        // clear all notifications
        notificationManager.clearNotifications()
    }

    override fun intervalCheck() {
        if (!validationIsOn.get()) return
        // we assume this function is called once per second (!!)
        criticalMessagesObserver?.let { observer ->
            if (!battery.updateLock.get()) {
                if (!battery.lastStatusWasShown.get()) {
                    battery.lastStatusWasShown.set(
                        observer.showCriticalMessageDialog(
                            BatteryState(
                                battery.lastStatus
                            )
                        )
                    )
                }
                battery.lastShownStatusAsNotification = battery.lastStatus
            }
            if (!dataCollector.updateLock.get()) {
                if (!dataCollector.lastStatusWasShown.get()) {
                    dataCollector.lastStatusWasShown.set(
                        observer.showCriticalMessageDialog(
                            GpsState(
                                dataCollector.lastStatus
                            )
                        )
                    )
                }
                dataCollector.lastShownStatusAsNotification = dataCollector.lastStatus
            }
            if (!dataSender.updateLock.get()) {
                if (!dataSender.lastStatusWasShown.get()) {
                    dataSender.lastStatusWasShown.set(
                        observer.showCriticalMessageDialog(
                            DataConnectionState(
                                dataSender.lastStatus
                            )
                        )
                    )
                }
                dataSender.lastShownStatusAsNotification = dataSender.lastStatus
            }
        } ?: run {
            if (!battery.updateLock.get()) {
                if (battery.lastShownStatusAsNotification.priority < battery.lastStatus.priority && battery.lastStatus.priority >= WarningsBasicLevels.PRIORITY_GOOD) {
                    // show
                    battery.lastShownStatusAsNotification = battery.lastStatus
                    if (!battery.lastStatusWasShown.get()) {
                        getNotificationDataForMessage(BatteryState(battery.lastStatus))?.let {
                            notificationManager.createCriticalMessageNotification(
                                NOTIFICATION_ID_BATTERY, it
                            )
                        }
                    }
                } else if (battery.lastStatus == WarningsBasicLevels.GREEN) {
                    // remove notification
                    notificationManager.clearCriticalNotification(NOTIFICATION_ID_BATTERY)
                }
                battery.lastShownStatusAsNotification = battery.lastStatus

            }
            if (!dataCollector.updateLock.get()) {
                if (dataCollector.lastShownStatusAsNotification.priority < dataCollector.lastStatus.priority) {
                    // show
                    dataCollector.lastShownStatusAsNotification = dataCollector.lastStatus
                    if (!dataCollector.lastStatusWasShown.get()) {
                        getNotificationDataForMessage(GpsState(dataCollector.lastStatus))?.let {
                            notificationManager.createCriticalMessageNotification(
                                NOTIFICATION_ID_DATA_COLLECTOR,
                                it
                            )
                        }
                    }
                } else if (dataCollector.lastStatus == WarningsBasicLevels.GREEN) {
                    // remove notification
                    notificationManager.clearCriticalNotification(NOTIFICATION_ID_DATA_COLLECTOR)
                }
            }
            if (!dataSender.updateLock.get()) {
                if (dataSender.lastShownStatusAsNotification.priority < dataSender.lastStatus.priority) {
                    // show
                    dataSender.lastShownStatusAsNotification = dataSender.lastStatus
                    if (!dataSender.lastStatusWasShown.get()) {
                        getNotificationDataForMessage(DataConnectionState(dataSender.lastStatus))?.let {
                            notificationManager.createCriticalMessageNotification(
                                NOTIFICATION_ID_DATA_SENDER, it
                            )
                        }
                    }
                } else if (dataSender.lastStatus == WarningsBasicLevels.GREEN) {
                    // remove notification
                    notificationManager.clearCriticalNotification(NOTIFICATION_ID_DATA_SENDER)
                }
            }
        }
    }

    override fun turnOffValidation() {
        validationIsOn.set(false)
        // stop interfaces
        gpsStateInterfaceController.stop()
        batteryStateInterfaceController.stop()
        dataSenderStateInterfaceController.stop()
        // remove all callbacks
        gpsStateInterfaceController.removeCallback(CALLBACKS_TAG)
        batteryStateInterfaceController.removeCallback(CALLBACKS_TAG)
        dataSenderStateInterfaceController.removeCallback(CALLBACKS_TAG)
        // reset values
        resetValues()
    }

    override fun turnOnValidation() {
        // set defaults
        resetValues()
        // set callbacks
        dataSenderStateInterfaceController.setCallback(this, CALLBACKS_TAG)
        batteryStateInterfaceController.setCallback(this, CALLBACKS_TAG)
        gpsStateInterfaceController.setCallback(this, CALLBACKS_TAG)
        // start interfaces
        gpsStateInterfaceController.start()
        batteryStateInterfaceController.start()
        dataSenderStateInterfaceController.start()
        validationIsOn.set(true)
    }

    override fun onGpsChanged(newLevel: WarningsBasicLevels) {
        // lock is just for UI updates
        dataCollector.updateLock.set(true)
        if (newLevel.priority > dataCollector.lastStatus.priority) {
            dataCollector.lastStatusWasShown.set(newLevel.priority <= WarningsBasicLevels.PRIORITY_GOOD)
        } else if (newLevel.priority <= WarningsBasicLevels.PRIORITY_GOOD) {
            // we do not show good or unknown state
            dataCollector.lastStatusWasShown.set(true)
        }
        collectGpsEventForRideHistory(newLevel)
        dataCollector.lastStatus = newLevel
        dataCollector.updateLock.set(false)
    }

    override fun onBatteryChanged(newLevel: WarningsBasicLevels) {
        // lock is just for UI updates
        battery.updateLock.set(true)
        if (newLevel.priority > battery.lastStatus.priority) {
            battery.lastStatusWasShown.set(newLevel.priority <= WarningsBasicLevels.PRIORITY_GOOD)
        } else if (newLevel.priority <= WarningsBasicLevels.PRIORITY_GOOD) {
            // we do not show good or unknown state
            battery.lastStatusWasShown.set(true)
        }
        collectBatteryEventForRideHistory(newLevel)
        battery.lastStatus = newLevel
        battery.updateLock.set(false)
    }

    override fun onDataSenderStateChanged(newLevel: WarningsBasicLevels) {
        // lock is just for UI updates
        dataSender.updateLock.set(true)
        if (newLevel.priority > dataSender.lastStatus.priority) {
            dataSender.lastStatusWasShown.set(newLevel.priority <= WarningsBasicLevels.PRIORITY_GOOD)
        } else if (newLevel.priority <= WarningsBasicLevels.PRIORITY_GOOD) {
            // we do not show good or unknown state
            dataSender.lastStatusWasShown.set(true)
        }
        collectDataEventForRideHistory(newLevel)
        dataSender.lastStatus = newLevel
        dataSender.updateLock.set(false)
    }

    private fun collectGpsEventForRideHistory(newLevel: WarningsBasicLevels) {
        if (!dataCollector.lastStatus.isRed() && newLevel.isRed()) {
            compositeDisposable.addSafe(
                rideHistoryCollector.onGpsSignalLost()
                    .subscribe()
            )
        } else if (!newLevel.isRed() && dataCollector.lastStatus.isRed() && !newLevel.isUnknown()) {
            compositeDisposable.addSafe(
                rideHistoryCollector.onGpsSignalRetrieve()
                    .subscribe()
            )
        }
    }

    private fun collectBatteryEventForRideHistory(newLevel: WarningsBasicLevels) {
        if (!battery.lastStatus.isRed() && newLevel.isRed()) {
            compositeDisposable.addSafe(
                rideHistoryCollector.onBatteryLevelMoveFromFairToRed()
                    .subscribe()
            )
        } else if (!newLevel.isRed() && battery.lastStatus.isRed() && !newLevel.isUnknown()) {
            compositeDisposable.addSafe(
                rideHistoryCollector.onBatteryLevelMoveFromRedToFair()
                    .subscribe()
            )
        }
    }

    private fun collectDataEventForRideHistory(newLevel: WarningsBasicLevels) {
        if (!dataSender.lastStatus.isRed() && newLevel.isRed()) {
            compositeDisposable.addSafe(
                rideHistoryCollector.onInternetStrengthMoveFromFairToCritical()
                    .subscribe()
            )
        } else if (!newLevel.isRed() && dataSender.lastStatus.isRed() && !newLevel.isUnknown()) {
            compositeDisposable.addSafe(
                rideHistoryCollector.onInternetStrengthMoveFromCriticalToFair()
                    .subscribe()
            )
        }
    }

    private fun resetValues() {
        dataSender.reset()
        dataCollector.reset()
        battery.reset()
    }

    private fun getNotificationDataForMessage(
        criticalMessageType: CriticalMessageState
    ): String? {
        when (criticalMessageType) {
            is BatteryState -> {
                if (criticalMessageType.batterySignal == WarningsBasicLevels.YELLOW) {
                    return "critical_messages_medium_battery"
                } else if (criticalMessageType.batterySignal == WarningsBasicLevels.RED) {
                    return "critical_messages_low_battery"
                }
            }
            is GpsState -> {
                if (criticalMessageType.gpsSignal == WarningsBasicLevels.YELLOW) {
                    return "critical_messages_medium_gps"
                } else if (criticalMessageType.gpsSignal == WarningsBasicLevels.RED) {
                    return "critical_messages_low_gps"
                }
            }
            is DataConnectionState -> {
                if (criticalMessageType.dataConnectionSignal == WarningsBasicLevels.YELLOW) {
                    return "critical_messages_medium_data_connection"
                } else if (criticalMessageType.dataConnectionSignal == WarningsBasicLevels.RED) {
                    return "critical_messages_low_data_connection"
                }
            }
        }
        return null
    }
}

data class CriticalMessageContainer(
    var lastStatus: WarningsBasicLevels = WarningsBasicLevels.GREEN,
    val lastStatusWasShown: AtomicBoolean = AtomicBoolean(true),
    val updateLock: AtomicBoolean = AtomicBoolean(false),
    var lastShownStatusAsNotification: WarningsBasicLevels = WarningsBasicLevels.GREEN
) {
    fun reset() {
        lastStatus = WarningsBasicLevels.GREEN
        lastStatusWasShown.set(true)
        updateLock.set(false)
        lastShownStatusAsNotification = WarningsBasicLevels.GREEN
    }
}