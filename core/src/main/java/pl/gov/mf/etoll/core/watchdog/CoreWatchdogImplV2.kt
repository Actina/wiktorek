package pl.gov.mf.etoll.core.watchdog

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.RemoteMessage
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import pl.gov.mf.etoll.core.BuildConfig
import pl.gov.mf.etoll.core.CoreComposedUC
import pl.gov.mf.etoll.core.connection.InternetConnectionStateController
import pl.gov.mf.etoll.core.criticalmessages.CriticalMessagesChecker
import pl.gov.mf.etoll.core.criticalmessages.CriticalMessagesObserver
import pl.gov.mf.etoll.core.crmmessages.CrmMessagesManager
import pl.gov.mf.etoll.core.devicecompatibility.DeviceCompatibilityUC
import pl.gov.mf.etoll.core.devicecompatibility.types.BatteryOptimisationInfo
import pl.gov.mf.etoll.core.foregroundservice.ForegroundServiceUC
import pl.gov.mf.etoll.core.notifications.NotificationManager
import pl.gov.mf.etoll.core.ridecoordinatorv2.location.issues.LocationIssuesDetector
import pl.gov.mf.etoll.core.ridecoordinatorv2.location.issues.LocationIssuesDetectorCallback
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorV3
import pl.gov.mf.etoll.core.ridecoordinatorv2.sender.RideCoordinatorV3Sender
import pl.gov.mf.etoll.core.ridehistory.collector.RideHistoryCollector
import pl.gov.mf.etoll.core.watchdog.awake.CoreAwakePushController
import pl.gov.mf.etoll.core.watchdog.counter.TimeShiftDetectorCounter
import pl.gov.mf.etoll.core.watchdog.fakegps.FakeGpsCollector
import pl.gov.mf.etoll.core.watchdog.logsender.CoreLogSender
import pl.gov.mf.etoll.core.watchdog.minversion.WatchdogMinVersionController
import pl.gov.mf.etoll.core.watchdog.minversion.WatchdogMinVersionControllerCallbacks
import pl.gov.mf.etoll.core.watchdog.netlock.CoreNetlockChecker
import pl.gov.mf.etoll.core.watchdog.netlock.CoreNetlockCheckerCallbacks
import pl.gov.mf.etoll.core.watchdog.notification.CoreDuringRideNotificationController
import pl.gov.mf.etoll.core.watchdog.ridefinish.RideFinishCallbacks
import pl.gov.mf.etoll.core.watchdog.ridefinish.RideFinishController
import pl.gov.mf.etoll.core.watchdog.sending.CoreWatchdogSendingQueueController
import pl.gov.mf.etoll.core.watchdog.status.CoreWatchdogStatusUpdateController
import pl.gov.mf.etoll.core.watchdog.timeissues.TimeIssuesCallbacks
import pl.gov.mf.etoll.core.watchdog.timeissues.TimeIssuesController
import pl.gov.mf.etoll.core.watchdog.wakelock.WakelockController
import pl.gov.mf.etoll.initialization.LoadableSystemsLoader
import pl.gov.mf.etoll.logging.LogUseCase
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC
import pl.gov.mf.mobile.utils.AirplaneModeUtils
import pl.gov.mf.mobile.utils.addSafe
import pl.gov.mf.mobile.utils.disposeSafe
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class CoreWatchdogImplV2 @Inject constructor(
    private val startForegroundServiceUseCase: ForegroundServiceUC.StartForegroundServiceUseCase,
    private val stopForegroundServiceUseCase: ForegroundServiceUC.StopForegroundServiceUseCase,
    private val writeSettingsUseCase: SettingsUC.WriteSettingsUseCase,
    private val criticalMessagesChecker: CriticalMessagesChecker,
    private val rideCoordinator: RideCoordinatorV3,
    private val rideCoordinatorSender: RideCoordinatorV3Sender,
    private val notificationManager: NotificationManager,
    private val readSettingsUseCase: SettingsUC.ReadSettingsUseCase,
    private val context: Context,
    private val checkGpsStateUseCase: DeviceCompatibilityUC.CheckGpsStateUseCase,
    private val sendingQueueController: CoreWatchdogSendingQueueController,
    private val statusUpdateController: CoreWatchdogStatusUpdateController,
    private val awakePushController: CoreAwakePushController,
    private val checkBatteryOptimisationUseCase: DeviceCompatibilityUC.CheckBatteryOptimisationUseCase,
    private val fakeGpsCollector: FakeGpsCollector,
    private val duringRideNotificationController: CoreDuringRideNotificationController,
    private val wakelockController: WakelockController,
    private val internetConnectionStateController: InternetConnectionStateController,
    private val crmMessagesManager: CrmMessagesManager,
    private val rideFinishController: RideFinishController,
    private val coreLogSender: CoreLogSender,
    private val logUseCase: LogUseCase,
    private val rideHistoryCollector: RideHistoryCollector,
    private val locationIssuesDetector: LocationIssuesDetector,
    private val timeIssuesController: TimeIssuesController,
    private val coreNetlockChecker: CoreNetlockChecker,
    private val timeShiftDetectorCounter: TimeShiftDetectorCounter,
    private val watchdogMinVersionController: WatchdogMinVersionController,
    private val observeDataSyncInProgressUseCase: CoreComposedUC.IsDataSyncInProgressUseCase,
) : CoreWatchdog, CoreWatchdogStatusUpdateController.AppInForegroundInfoProvider,
    LocationIssuesDetectorCallback, TimeIssuesCallbacks, CoreNetlockCheckerCallbacks,
    WatchdogMinVersionControllerCallbacks {

    private lateinit var owner: LoadableSystemsLoader
    private val systemIsLoaded = AtomicBoolean(false)
    private val duringRide = AtomicBoolean(false)
    private var currentActivity: WeakReference<Activity>? = null
    private var criticalMessagesObserver: CriticalMessagesObserver? = null
    private val compositeDisposable = CompositeDisposable()
    private var statusUpdatesDisposable: Disposable? = null
    private val watchdogCyclesCounter = AtomicInteger(0)
    private val rideWasResumed = AtomicBoolean(false)
    private var ridefinishCallbacks: RideFinishCallbacks? = null
    private val appInForeground = AtomicBoolean(false)
    private var stopForegroundServiceIntent: Intent? = null

    override fun onAppGoesToBackground(
        activity: Activity,
        overlayServiceStarter: Intent,
    ) {
        if (!appInForeground.getAndSet(false) || activity.isChangingConfigurations) {
            return
        }

        // update refs
        currentActivity?.clear()
        currentActivity = null

        criticalMessagesObserver = null

        if (!systemIsLoaded.get()) return
        logUseCase.log(LogUseCase.APP, "Going to background")
        // store info about foreground mode - it's used only for statistics (event param)
        compositeDisposable.addSafe(
            writeSettingsUseCase.execute(Settings.APP_IN_FOREGROUND, false).subscribe()
        )

        onCheckConditionsRequested()

        // schedule sending queue
        sendingQueueController.onAppGoesToBackground()

        criticalMessagesChecker.onAppGoesToBackground()

        rideFinishController.onAppGoesToBackground()

        // notification service
        if (rideCoordinator.getConfiguration()?.duringRide == true) {
            startForegroundServiceUseCase.execute(activity, overlayServiceStarter)
            duringRideNotificationController.setNotificationShouldBeVisible()
        }

        timeIssuesController.removeCallbacks(LOGCAT_TAG)
        coreNetlockChecker.removeCallbacks(LOGCAT_TAG)
        watchdogMinVersionController.removeCallbacks(LOGCAT_TAG)
        // check if we should create notification about unsent data
        // we do not manage this variable
        observeDataSyncInProgressUseCase.execute().first(false).subscribe({ syncInProgress ->
            if (syncInProgress && rideCoordinator.getConfiguration()?.duringRide == false) {
                notificationManager.createNotificationForUnsentData()
            }
        }, {
            // do nothing on error
        })
    }

    override fun onAppGoesToForeground(
        activity: Activity,
        criticalMessagesObserver: CriticalMessagesObserver?,
        rideFinishCallbacks: RideFinishCallbacks?,
        stopForegroundServiceIntent: Intent,
    ) {
        internalOnAppGoesToForeground(
            activity,
            criticalMessagesObserver,
            rideFinishCallbacks,
            false,
            stopForegroundServiceIntent
        )
    }

    private fun internalOnAppGoesToForeground(
        activity: Activity,
        criticalMessagesObserver: CriticalMessagesObserver?,
        rideFinishCallbacks: RideFinishCallbacks?,
        force: Boolean,
        stopForegroundServiceIntent: Intent,
    ) {
        if ((appInForeground.getAndSet(true) && !force) || (activity.isChangingConfigurations)) {
            return
        }

        // update refs
        currentActivity = WeakReference(activity)
        this.criticalMessagesObserver = criticalMessagesObserver
        this.ridefinishCallbacks = rideFinishCallbacks
        this.stopForegroundServiceIntent = stopForegroundServiceIntent
        if (!systemIsLoaded.get())
            return
        logUseCase.log(LogUseCase.APP, "Going to foreground")
        // store info about foreground mode - it's used only for statistics (event param)
        compositeDisposable.addSafe(
            writeSettingsUseCase.execute(Settings.APP_IN_FOREGROUND, true).subscribe()
        )
        // check if we should force-update status
        statusUpdateController.checkIfUpdateShouldBeDoneNow(activity.intent)

        // schedule sending queue
        sendingQueueController.onAppGoesToForeground()

        criticalMessagesObserver?.let { observer ->
            criticalMessagesChecker.onAppGoesToForeground(
                observer
            )
        }

        rideFinishCallbacks?.let { callbacks ->
            rideFinishController.onAppGoesToForeground(callbacks)
        }

        onCheckConditionsRequested()

        // notification service
        stopForegroundServiceUseCase.execute(activity, stopForegroundServiceIntent)
        duringRideNotificationController.setNotificationShouldBeGone()

        // if we're loaded and returned from background when not during ride, we should update status - this is for CRM messages
        if (systemIsLoaded.get() && rideCoordinator.getConfiguration()?.duringRide != true) {
            // update system
            statusUpdateController.updateNow(this)
        }

        timeIssuesController.setCallbacks(this, LOGCAT_TAG)
        coreNetlockChecker.setCallbacks(this, LOGCAT_TAG)
        watchdogMinVersionController.setCallbacks(this, LOGCAT_TAG)
    }

    override fun onAwakePushReceived(msg: RemoteMessage) {
        if (!systemIsLoaded.get())
            owner.loadSequentially()
        awakePushController.onAwakePushReceived(
            msg,
            systemIsLoaded.get(),
            appIsInForeground()
        )
        logUseCase.log(LogUseCase.APP, "Got awake push! $msg")
    }

    override fun onCheckConditionsRequested() {
        // check if we have business number, and if no, do nothing
        val businessNumberIsPresent =
            readSettingsUseCase.executeForString(Settings.BUSINESS_NUMBER).isNotEmpty()
        if (!businessNumberIsPresent) {
            stopUpdates()
            return
        }
        // if we're not during ride and we're in background, stop updates, otherwise start them
        val rideConfiguration = rideCoordinator.getConfiguration()
        if (((rideConfiguration != null && !rideConfiguration.duringRide)
                    || rideConfiguration == null)
            && !(currentActivity != null && currentActivity?.get() != null)
        ) {
            stopUpdates()
            return
        }
        // if all previous conditions passed, start updates
        startUpdates()
    }

    override fun setOwner(loadableSystemsLoader: LoadableSystemsLoader) {
        owner = loadableSystemsLoader
    }

    override fun load(): Single<Boolean> = Single.create { emitter ->
        if (systemIsLoaded.getAndSet(true)) {
            emitter.onSuccess(true)
            return@create
        }
        currentActivity?.get()?.let {
            internalOnAppGoesToForeground(
                it,
                criticalMessagesObserver,
                ridefinishCallbacks,
                true,
                stopForegroundServiceIntent!!
            )
        }

        // load status controller
        statusUpdateController.initialize()

        // resume support
        rideCoordinator.getConfiguration()?.let {
            if (it.duringRide) {
                // we should resume ride
                rideWasResumed.set(true)
            }
        }

        compositeDisposable.addSafe(rideCoordinator.observeConfiguration().subscribe {
            if (it.duringRide != duringRide.getAndSet(it.duringRide)) {
                if (it.duringRide)
                    onRideStarted()
                else
                    onRideEnded()
            }
            onCheckConditionsRequested()
        })
        start()
        emitter.onSuccess(true)
    }

    private fun start() {
        compositeDisposable.addSafe(fakeGpsCollector.observeChanges().subscribe { detected ->
            if (detected) {
                logUseCase.log(LogUseCase.APP, "Fake gps detected!(critical!)")
                if (readSettingsUseCase.executeForBoolean(Settings.ACTIVATION_TRANS_SUCCEED)) {
                    rideCoordinator.getConfiguration()?.let { config ->
                        if (config.duringRide) {
                            rideCoordinator.stopRide().subscribe()
                            rideHistoryCollector.onFakeGpsDetected().subscribe()
                        }
                        // one-time check for background notification for fake gps
                        if (!appIsInForeground()) {
                            notificationManager.createNotificationForFakeGps()
                        }
                    }
                }
            }
        })
    }

    private fun stopUpdates() {
        // do nothing if not needed
        if (statusUpdatesDisposable == null || statusUpdatesDisposable!!.isDisposed) {
            statusUpdatesDisposable = null
            return
        }
        logUseCase.log(LogUseCase.APP, "Stopping updates")
        Log.d(LOGCAT_TAG, "Updates stopped")
        // stop updates
        statusUpdatesDisposable?.disposeSafe()
        statusUpdatesDisposable = null
    }

    private var lastBatteryOptimizationWasEnabled = AtomicBoolean(false)

    private fun startUpdates() {
        // we do not start updates again
        if (statusUpdatesDisposable != null && !statusUpdatesDisposable!!.isDisposed)
            return
        logUseCase.log(LogUseCase.APP, "Starting updates")
        Log.d(LOGCAT_TAG, "Updates started")
        // start "main loop" and wire up systems
        statusUpdatesDisposable = Observable.interval(
            ONE_SECOND,
            ONE_SECOND,
            TimeUnit.SECONDS
        ).filter {
            // TODO: refactor, change to observable and move to start
            // check if airplane mode is on or we disabled location services
            if (checkIfAirplaneModeShouldBeProhibited() && AirplaneModeUtils.isAirplaneModeOn(
                    context
                )
            ) {
                logUseCase.log(LogUseCase.APP, "Airplane mode on(critical!)")
                // critical error
                // we're always during ride when those checks fire, so let's stop it
                rideCoordinator.stopRide().subscribe()
                // and notify user about what did just happened
                if (criticalMessagesObserver != null) {
                    criticalMessagesObserver?.blockAppAirplaneModeIsOn()
                } else {
                    notificationManager.createNotificationForAirplaneModeIsOn()
                }
            } else if (checkIfGpsPermissionsAreRequired() && !checkGpsStateUseCase.executeAlternativeCode()) {
                logUseCase.log(LogUseCase.APP, "GPS turned off(critical!)")
                // critical error
                // we're always during ride when those checks fire, so let's stop it
                rideCoordinator.stopRide().subscribe()
                // and notify user about what did just happened
                if (criticalMessagesObserver != null) {
                    criticalMessagesObserver?.blockAppNoGpsAccessible()
                } else {
                    notificationManager.createNotificationForGpsNotAccessible()
                }
            }

            // and now foreground dialog for fake gps
            if (fakeGpsCollector.shouldFakeGpsDialogBeShown() && appIsInForeground()) {
                criticalMessagesObserver?.let { criticalMessagesObserver ->
                    fakeGpsCollector.setDialogWasShown(
                        criticalMessagesObserver.showFakeGpsDetectedDialog()
                    )
                }
            }

            // TODO: refactor, change to observable and move to start
            if (checkBatteryOptimisationUseCase.execute() == BatteryOptimisationInfo.OPTIMISATION_ENABLED) {
                // check if we finished registration
                if (readSettingsUseCase.executeForBoolean(Settings.ACTIVATION_TRANS_SUCCEED)) {
                    logUseCase.log(LogUseCase.APP, "Battery optimizations are enabled!")
                    if (BuildConfig.FLAVOR.uppercase().contains("DEV")) {
                        if (!lastBatteryOptimizationWasEnabled.get()) {
                            notificationManager.createNotificationForBatteryOptimizationEnabled()
                            lastBatteryOptimizationWasEnabled.set(true)
                        }
                    }
                }
            } else {
                lastBatteryOptimizationWasEnabled.set(false)
            }
            watchdogCyclesCounter.incrementAndGet()
            criticalMessagesChecker.intervalCheck()
            duringRideNotificationController.intervalCheck()
            internetConnectionStateController.intervalCheck()
            crmMessagesManager.intervalCheck()
            rideFinishController.intervalCheck()
            coreLogSender.intervalCheck()
            locationIssuesDetector.intervalCheck(
                this,
                appInForeground.get(),
                rideCoordinator.getConfiguration()
            )
            timeIssuesController.intervalCheck()
            coreNetlockChecker.intervalCheck()
            watchdogMinVersionController.intervalCheck()
            timeShiftDetectorCounter.onNextSecond()
            // experimental flow-control - not once per second, but at max retry speed of once per 3s
            if (rideCoordinator.getConfiguration()?.duringRide != true && it.toInt() % 3 == 0) {
                rideCoordinatorSender.checkAndSend(forceTolled = true, forceSent = true)
            }

            if (watchdogCyclesCounter.get() >= STATUS_UPDATE_INTERVAL || statusUpdateController.shouldForceUpdate()
            ) {
                watchdogCyclesCounter.set(0)
                return@filter true
            }
            return@filter false
        }.subscribe({
            statusUpdateController.updateNow(this)
        }, {

        })
    }

    private fun checkIfGpsPermissionsAreRequired(): Boolean {
        val config = rideCoordinator.getConfiguration() ?: return false
        if (config.duringRide && config.monitoringDeviceConfiguration?.monitoringByApp == true) return true
        return false
    }

    private fun checkIfAirplaneModeShouldBeProhibited(): Boolean {
        val config = rideCoordinator.getConfiguration() ?: return false
        return config.duringRide
    }

    private fun onRideStarted() {
        logUseCase.log(LogUseCase.APP, "start/resume ride(onRideStarted)")
        if (rideCoordinator.getConfiguration()?.duringRide != true) return
        rideCoordinatorSender.resetTimers()
        criticalMessagesChecker.turnOnValidation()
        if (!rideWasResumed.get()) {
            timeShiftDetectorCounter.resetCounter()
            compositeDisposable.addSafe(
                writeSettingsUseCase.execute(
                    Settings.RIDE_START_TIMESTAMP,
                    "${System.currentTimeMillis()}"
                ).subscribe {
                    onCheckConditionsRequested()
                    sendingQueueController.onCheckConditionsRequested()
                })
        } else {
            onCheckConditionsRequested()
            sendingQueueController.onCheckConditionsRequested()
        }
        wakelockController.applyLock()
    }

    private fun onRideEnded() {
        logUseCase.log(LogUseCase.APP, "stop ride(onRideEnded)")
        if (rideCoordinator.getConfiguration()?.duringRide == true) return
        criticalMessagesChecker.turnOffValidation()
        // unscheduled sending queue if it's empty
        onCheckConditionsRequested()
        rideCoordinatorSender.checkAndSend(forceTolled = true, forceSent = true)
        rideWasResumed.set(false)
        stopForegroundServiceUseCase.execute(context, stopForegroundServiceIntent!!)
        wakelockController.releaseLock()
    }

    companion object {
        private const val ONE_SECOND: Long = 1
        private const val LOGCAT_TAG = "WatchdogV2"
        private val STATUS_UPDATE_INTERVAL: Long = if (BuildConfig.DEBUG) 30 else 3 * 60
    }

    override fun appIsInForeground(): Boolean =
        currentActivity != null && currentActivity!!.get() != null

    override fun onIssueDetected(issues: List<Int>) {
        // check if we're in foreground or background
        if (appIsInForeground()) {
            criticalMessagesObserver?.showGpsIssuesDialog(issues)

        } else {
            // show notification if not showing
            notificationManager.createNotificationForGpsIssuesDetected()
        }
    }

    override fun showTimeIssueScreen() {
        val duringRide = rideCoordinator.getConfiguration()?.duringRide == true
        if (appIsInForeground()) {
            criticalMessagesObserver?.showTimeIssues(duringRide)
        } else if (duringRide) {
            // show notification as we're in background but during ride
            notificationManager.createNotificationForTimeIssuesDetected()
        }
    }

    override fun onShowNetworkLock() {
        // stop ride
        if (rideCoordinator.getConfiguration()?.duringRide == true) {
            rideCoordinator.stopRide().subscribe()
        }
        // anyway, clear configuration
        rideCoordinator.clearConfiguration()
        // show 410 error (network lock) - this will be showed just once
        if (appIsInForeground()) {
            if (criticalMessagesObserver?.showInstanceIssuesError() == true)
                coreNetlockChecker.markStateAsShowed()
        }
    }

    override fun onVersionTooOldDetected() {
        if (appIsInForeground() && rideCoordinator.getConfiguration()?.duringRide == false) {
            criticalMessagesObserver?.blockAppVersionTooOld()
        }
    }

}