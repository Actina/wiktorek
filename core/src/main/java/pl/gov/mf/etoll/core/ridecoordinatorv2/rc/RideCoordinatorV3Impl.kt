package pl.gov.mf.etoll.core.ridecoordinatorv2.rc

import android.annotation.SuppressLint
import android.util.Log
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import pl.gov.mf.etoll.core.BuildConfig
import pl.gov.mf.etoll.core.model.CoreSent
import pl.gov.mf.etoll.core.ridecoordinatorv2.events.EventHelper
import pl.gov.mf.etoll.core.ridecoordinatorv2.events.SentChangeActionType
import pl.gov.mf.etoll.core.ridecoordinatorv2.location.LocationServiceProvider
import pl.gov.mf.etoll.core.ridecoordinatorv2.location.issues.LocationIssuesDetector
import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.SentAlgorithm
import pl.gov.mf.etoll.core.watchdog.counter.TimeShiftDetectorCounter
import pl.gov.mf.etoll.core.watchdog.counter.TimeShiftDetectorCounterCallback
import pl.gov.mf.etoll.core.watchdog.fakegps.FakeGpsCollector
import pl.gov.mf.etoll.initialization.LoadableSystemsLoader
import pl.gov.mf.etoll.logging.LogUseCase
import pl.gov.mf.etoll.storage.database.ridecache.RideCacheDatabase
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC
import pl.gov.mf.mobile.utils.CallbacksContainer
import pl.gov.mf.mobile.utils.deepCopy
import pl.gov.mf.mobile.utils.disposeSafe
import pl.gov.mf.mobile.utils.toObject
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class RideCoordinatorV3Impl @Inject constructor(
    private val eventHelper: EventHelper,
    private val locationServiceProvider: LocationServiceProvider,
    private val readSettingsUseCase: SettingsUC.ReadSettingsUseCase,
    private val writeSettingsUseCase: SettingsUC.WriteSettingsUseCase,
    private val sentAlgorithm: SentAlgorithm,
    private val rideCacheDatabase: RideCacheDatabase,
    private val logUseCase: LogUseCase,
    private val fakeGpsCollector: FakeGpsCollector,
    private val locationIssuesDetector: LocationIssuesDetector,
    private val timeShiftDetectorCounter: TimeShiftDetectorCounter
) : RideCoordinatorV3, RideCoordinatorTolledController, RideCoordinatorSentController,
    RideCoordinatorDeviceController, RideCoordinatorSanityCheck, RideCoordinatorGpsConfiguration,
    TimeShiftDetectorCounterCallback {

    companion object {
        private val tolledTreshold = RideCoordinatorThresholds(
            gpsYellowMinutes = 1,
            gpsRedMinutes = if (BuildConfig.DEBUG) 2 else 15,
            dataConnectionYellowMinutes = if (BuildConfig.DEBUG) 2 else 5,
            dataConnectionRedMinutes = if (BuildConfig.DEBUG) 3 else 15,
            batteryYellowPercent = 50,
            batteryRedPercent = 20,
            timeForGpsToWarmUp = if (BuildConfig.DEBUG) 10 else 60
        )
        private val sentTreshold = RideCoordinatorThresholds(
            gpsYellowMinutes = 10,
            gpsRedMinutes = 60,
            dataConnectionYellowMinutes = 10,
            dataConnectionRedMinutes = 60,
            batteryYellowPercent = 50,
            batteryRedPercent = 20,
            timeForGpsToWarmUp = if (BuildConfig.DEBUG) 10 else 60
        )
    }

    private lateinit var owner: LoadableSystemsLoader
    private var configurationSource = BehaviorSubject.create<RideCoordinatorConfiguration>()
    private var timerDisposable: Disposable? = null
    private val callbacksContainer: CallbacksContainer<RideCoordinatorV3Callbacks> =
        CallbacksContainer()

    override fun setOwner(loadableSystemsLoader: LoadableSystemsLoader) {
        owner = loadableSystemsLoader
        // TODO: check if we need to preload anything anywhere in any case here
        timeShiftDetectorCounter.setCallback(this)
    }

    override fun startRide() {
        // check conditions
        fakeGpsCollector.changeLastKnownLocationState(false)
        logUseCase.log(LogUseCase.RIDE_COORDINATOR, "Start ride requested!")
        val configuration = configurationSource.value!!
        if (configuration.duringRide)
            return
        // overwrite last sending date to current one
        writeSettingsUseCase.execute(
            Settings.LAST_CORRECT_DATA_SENDING_TIMESTAMP,
            System.currentTimeMillis().toString()
        ).subscribe()
        // change configuration
        configuration.duringRide = true

        locationServiceProvider.setFlagNextLocationWillBeStartLocation()

        notifyRideStarted(configuration)

        configurationSource.onNext(configuration)
        // write configuration to cache
        writeSettingsUseCase.execute(
            Settings.RIDE_COORDINATOR_CONFIGURATION,
            configuration.toJSON()
        )
            .subscribe()
        // set starting event flag
        eventHelper.setStartOfTracking(configuration)
        // enable ride
        enableRide()
        timeShiftDetectorCounter.unlockCounter()
        logUseCase.log(LogUseCase.RIDE_COORDINATOR, "Started ride!")
    }

    private fun notifyRideStarted(configuration: RideCoordinatorConfiguration) {
        callbacksContainer.get().forEach { callback ->
            if (configuration.duringTolled) {
                notifyTolledRideStarted(callback.value, configuration)
            }

            if (configuration.duringSent) {
                callback.value.onRideStart(
                    rideType = RideCoordinatorV3Callbacks.RideType.SENT,
                    vehicle = configuration.tolledConfiguration?.vehicle,
                    monitoring = configuration.monitoringDeviceConfiguration.map(),
                    exceedingWeightDeclared = configuration.tolledConfiguration?.trailerUsedAndCategoryWillBeIncreased
                        ?: false,
                    accountInfo = getAccountInfoFromConfigurations(configuration),
                    selectedSentList = configuration.sentConfiguration?.selectedSentList
                )
            }
        }
    }

    @SuppressLint("CheckResult")
    override fun stopRide(ignoreNotStartedStatus: Boolean): Single<Boolean> =
        Single.create { emitter ->
            timeShiftDetectorCounter.lockCounter()
            logUseCase.log(LogUseCase.RIDE_COORDINATOR, "Stop ride requested!")
            configurationSource.value!!.let { configuration ->
                if (!configuration.duringRide) {
                    logUseCase.log(LogUseCase.RIDE_COORDINATOR, "Stopped ride!(was not started)")
                    emitter.onSuccess(false)
                    return@create
                }
                // quick update - let's setup flag to inform UI about requirement to show ride summary dialog
                writeSettingsUseCase.execute(
                    Settings.RIDE_END_TIMESTAMP,
                    System.currentTimeMillis().toString()
                ).subscribe {
                    writeSettingsUseCase.execute(Settings.RIDE_SUMMARY_SHOULD_BE_SHOWN, true)
                        .subscribe {
                            timerDisposable.disposeSafe()
                            timerDisposable = null
                            // send last event
                            eventHelper.generateStopEvents(configuration)
                            // update status
                            configuration.apply {
                                duringRide = false
                                generatedAtLeastOneEventInSessionForSent = false
                                generatedAtLeastOneEventInSessionForTolled = false
                            }
                            configurationSource.onNext(configuration)
                            // write configuration to cache
                            writeSettingsUseCase.execute(
                                Settings.RIDE_COORDINATOR_CONFIGURATION,
                                configuration.toJSON()
                            )
                                .subscribe()
                            updateGps(forceStop = true)
                            callbacksContainer.get()
                                .forEach {
                                    if (configuration.duringSent) {
                                        var savedSentItemsCount = 0
                                        configuration.sentConfiguration?.selectedSentList
                                            ?.takeIf { sentList -> !sentList.isNullOrEmpty() }
                                            ?.forEach { sentItem ->
                                                notifySentCarriageStopped(
                                                    it.value,
                                                    sentItem,
                                                    configuration
                                                ) {
                                                    savedSentItemsCount++
                                                    if (savedSentItemsCount == configuration.sentConfiguration?.selectedSentList?.size) {
                                                        notifySentRideStopped(
                                                            it.value,
                                                            configuration
                                                        )
                                                        savedSentItemsCount = 0
                                                    }
                                                }
                                            } ?: notifySentRideStopped(it.value, configuration)
                                    }
                                    if (configuration.duringTolled) {
                                        it.value.onTolledRideChange(
                                            false,
                                            configuration.tolledConfiguration!!.vehicleId!!,
                                            configuration.tolledConfiguration!!.trailerUsedAndCategoryWillBeIncreased
                                        )
                                        it.value.onRideStop(
                                            type = RideCoordinatorV3Callbacks.RideType.TOLLED,
                                            vehicle = configuration.tolledConfiguration?.vehicle,
                                            monitoring = configuration.monitoringDeviceConfiguration.map(),
                                            accountInfo = getAccountInfoFromConfigurations(
                                                configuration
                                            ),
                                            exceedingWeightDeclared = configuration.tolledConfiguration?.trailerUsedAndCategoryWillBeIncreased
                                                ?: false,
                                            selectedSentList = configuration.sentConfiguration?.selectedSentList
                                        )
                                    }
                                }
                            logUseCase.log(LogUseCase.RIDE_COORDINATOR, "Stopped ride!")
                            emitter.onSuccess(true)
                        }
                }
            }
        }

    private fun notifyTolledRideStarted(
        callback: RideCoordinatorV3Callbacks,
        configuration: RideCoordinatorConfiguration
    ) {
        callback.onRideStart(
            rideType = RideCoordinatorV3Callbacks.RideType.TOLLED,
            vehicle = configuration.tolledConfiguration?.vehicle,
            monitoring = configuration.monitoringDeviceConfiguration.map(),
            exceedingWeightDeclared = configuration.tolledConfiguration?.trailerUsedAndCategoryWillBeIncreased
                ?: false,
            accountInfo = getAccountInfoFromConfigurations(configuration)
        )
    }

    private fun notifySentCarriageStopped(
        callback: RideCoordinatorV3Callbacks,
        sentItem: CoreSent,
        configuration: RideCoordinatorConfiguration,
        onStoppedSuccessfully: () -> Unit = {}
    ) {
        callback.onSentRideChange(
            action = SentChangeActionType.STOP,
            sentItem = sentItem,
            monitoring = configuration.monitoringDeviceConfiguration.map()
        ) {
            onStoppedSuccessfully()
        }
    }

    private fun notifySentRideStopped(
        callback: RideCoordinatorV3Callbacks,
        configuration: RideCoordinatorConfiguration
    ) {
        callback.onRideStop(
            type = RideCoordinatorV3Callbacks.RideType.SENT,
            monitoring = configuration.monitoringDeviceConfiguration.map(),
            accountInfo = getAccountInfoFromConfigurations(
                configuration
            ),
            exceedingWeightDeclared = configuration.tolledConfiguration?.trailerUsedAndCategoryWillBeIncreased
                ?: false,
            selectedSentList = configuration.sentConfiguration?.selectedSentList
        )
    }

    override fun setCallbacks(callbacks: RideCoordinatorV3Callbacks?, tag: String) {
        callbacksContainer.set(callbacks, tag)
        eventHelper.setCallbacks(callbacks, tag)
    }

    override fun hardStopRideWithoutEvents() {
        val configuration = configurationSource.value!!
        if (!configuration.duringRide)
            return
        timerDisposable.disposeSafe()
        timerDisposable = null
        // update status
        configuration.duringRide = false
        configuration.generatedAtLeastOneEventInSessionForTolled = false
        configuration.generatedAtLeastOneEventInSessionForSent = false
        configurationSource.onNext(configuration)
        // write configuration to cache
        writeSettingsUseCase.execute(
            Settings.RIDE_COORDINATOR_CONFIGURATION,
            configuration.toJSON()
        )
            .subscribe()
        updateGps(forceStop = true)
        callbacksContainer.get()
            .forEach {
                if (configuration.duringTolled)
                    it.value.onTolledRideChange(
                        false,
                        configuration.tolledConfiguration!!.vehicleId,
                        configuration.tolledConfiguration!!.trailerUsedAndCategoryWillBeIncreased
                    )
                if (configuration.duringSent) {
                    configuration.sentConfiguration!!.selectedSentList.forEach { sentItem ->
                        it.value.onSentRideChange(
                            action = SentChangeActionType.STOP,
                            sentItem = sentItem,
                            monitoring = configuration.monitoringDeviceConfiguration.map()
                        )
                    }
                }
            }
    }

    override fun timeTresholdsForNotifications(): RideCoordinatorThresholds {
        val config = getConfiguration() ?: return tolledTreshold
        return if (config.duringTolled && config.duringSent) {
            // mixed
            tolledTreshold
        } else if (config.duringTolled) {
            tolledTreshold
        } else if (config.duringSent) {
            sentTreshold
        } else {
            // default config
            tolledTreshold
        }
    }

    fun tolled(): RideCoordinatorTolledController = this

    fun sent(): RideCoordinatorSentController = this

    fun device(): RideCoordinatorDeviceController = this

    private var isLoaded = AtomicBoolean(false)

    override fun load(): Single<Boolean> = Single.create<Boolean> { emitter ->
        if (isLoaded.getAndSet(true)) {
            emitter.onSuccess(true)
            return@create
        }
        val read = readSettingsUseCase.executeForString(Settings.RIDE_COORDINATOR_CONFIGURATION)
        if (read.isEmpty()) {
            configurationSource.onNext(
                createEmptyConfiguration()
            )
        } else {
            try {
                configurationSource.onNext(read.toObject())
            } catch (ex: Exception) {
                configurationSource.onNext(
                    createEmptyConfiguration()
                )
            }
        }
        emitter.onSuccess(true)
    }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())

    override fun checkAndResumeRideAfterAppRestart() {
        // resume ride if needed
        if (configurationSource.value!!.duringRide) {
            enableRide()
        }
    }

    override fun clearConfiguration() {
        onConfigurationUpdated(createEmptyConfiguration())
    }

    private fun createEmptyConfiguration(): RideCoordinatorConfiguration =
        RideCoordinatorConfiguration(
            duringRide = false,
            generatedAtLeastOneEventInSessionForSent = false,
            generatedAtLeastOneEventInSessionForTolled = false,
            monitoringDeviceConfiguration = null,
            sentConfiguration = null,
            tolledConfiguration = null,
            tolledIsPossibleToBeEnabled = false
        )


    private fun enableRide() {
        sentAlgorithm.resetAlgorithm()
        val configuration = configurationSource.value!!
        callbacksContainer.get().forEach { callback ->
            if (configuration.duringTolled)
                callback.value.onTolledRideChange(
                    configuration.duringTolled,
                    configuration.tolledConfiguration!!.vehicleId,
                    configuration.tolledConfiguration!!.trailerUsedAndCategoryWillBeIncreased
                )
        }
        // manage gps status
        updateGps(forceStart = true)
        // start timer
        timerDisposable.disposeSafe()
        timerDisposable = Observable.interval(0, 1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                onTimerTick()
            }, { error ->
                callbacksContainer.get()
                    .forEach { it.value.onRideCoordinatorError(error.localizedMessage) }
                stopRide().subscribe()
            })
    }

    /**
     * Check if we should use gps, and if yes - enable it
     */
    private fun updateGps(forceStart: Boolean = false, forceStop: Boolean = false) {
        val configuration = configurationSource.value
        if (configuration?.monitoringDeviceConfiguration?.monitoringByApp == true &&
            (forceStart || configuration.duringRide)
        ) {
            Log.d("GPS_STATE", "STARTING GPS")
            // gps should be enabled
            locationServiceProvider.start()
            locationIssuesDetector.start()
        }
        if (configuration?.monitoringDeviceConfiguration?.monitoringByApp == false || forceStop) {
            Log.d("GPS_STATE", "STOPPING GPS")
            // gps should be disabled
            locationServiceProvider.stop()
            locationIssuesDetector.stop()
        }
        Log.d("GPS_STATE", "FINISHED GPS UPDATE")
    }

    private var timerTicksSinceLastUpdate = 0
    private fun onTimerTick() {
        timerTicksSinceLastUpdate++
        val configuration = configurationSource.value!!

        if (configuration.duringTolled && timerTicksSinceLastUpdate >= RideCoordinatorV3.SECONDS_BETWEEN_COLLECTING_TOLLED) {
            if (locationServiceProvider.getLastKnownLocation(forTolled = true) != null) {
                timerTicksSinceLastUpdate = 0
                val willUpdateStatusAfterwards =
                    !configuration.generatedAtLeastOneEventInSessionForTolled
                eventHelper.generateIntervalEventForTolled(configuration)

                // update variable if needed
                if (willUpdateStatusAfterwards) {
                    configurationSource.onNext(configuration)
                    // write configuration to cache
                    writeSettingsUseCase.execute(
                        Settings.RIDE_COORDINATOR_CONFIGURATION,
                        configuration.toJSON()
                    ).subscribe()
                }
            }
        }

        if (configuration.duringSent) {
            val location = locationServiceProvider.getLastKnownLocation(forTolled = false)
            if (location != null && sentAlgorithm.onNextSecond(location)) {
                eventHelper.generateIntervalEventForSent(configuration)
                val willUpdateStatusAfterwards =
                    !configuration.generatedAtLeastOneEventInSessionForSent
                // update variable if needed
                if (willUpdateStatusAfterwards) {
                    configurationSource.onNext(configuration)
                    // write configuration to cache
                    writeSettingsUseCase.execute(
                        Settings.RIDE_COORDINATOR_CONFIGURATION,
                        configuration.toJSON()
                    ).subscribe()
                }
            }
        }
    }

    override fun startTolled(notifyRideStarted: Boolean) {
        val configuration = configurationSource.value!!
        if (configuration.duringRide) {
            eventHelper.generateTolledChangedEvent(configuration)
            callbacksContainer.get()
                .forEach {
                    it.value.onTolledRideChange(
                        true,
                        configuration.tolledConfiguration?.vehicleId,
                        configuration.tolledConfiguration?.trailerUsedAndCategoryWillBeIncreased
                            ?: false
                    )
                    if (notifyRideStarted) {
                        notifyTolledRideStarted(it.value, configuration)
                    }
                }
        }
        configurationSource.onNext(configuration)
        // write configuration to cache
        writeSettingsUseCase.execute(
            Settings.RIDE_COORDINATOR_CONFIGURATION,
            configuration.toJSON()
        ).subscribe()
    }

    override fun stopTolled() {
        val configuration = configurationSource.value!!
        // we do not reset vehicle id
        if (configuration.duringRide) {
            eventHelper.generateTolledChangedEvent(
                configuration
            )
            callbacksContainer.get()
                .forEach {
                    it.value.onTolledRideChange(
                        false,
                        configuration.tolledConfiguration?.vehicleId,
                        configuration.tolledConfiguration?.trailerUsedAndCategoryWillBeIncreased
                            ?: false
                    )
                    it.value.onRideStop(
                        type = RideCoordinatorV3Callbacks.RideType.TOLLED,
                        vehicle = configuration.tolledConfiguration?.vehicle
                            ?: configuration.disabledTolledConfiguration?.vehicle,
                        monitoring = configuration.monitoringDeviceConfiguration.map(),
                        accountInfo = getAccountInfoFromConfigurations(configuration),
                        exceedingWeightDeclared = configuration
                            .run { tolledConfiguration ?: disabledTolledConfiguration }
                            ?.trailerUsedAndCategoryWillBeIncreased ?: false,
                        selectedSentList = configuration.sentConfiguration?.selectedSentList
                    )
                }
        }
    }

    private fun getAccountInfoFromConfigurations(configuration: RideCoordinatorConfiguration) =
        configuration.run { tolledConfiguration?.vehicle ?: disabledTolledConfiguration?.vehicle }
            ?.accountInfo

    override fun startSent(sentName: CoreSent?) {
        val configuration = configurationSource.value!!
        if (configuration.duringRide) {
            eventHelper.generateSentChangedEvent(
                type = SentChangeActionType.START,
                sentId = sentName?.sentNumber,
                rideCoordinatorConfiguration = configuration
            )
            callbacksContainer.get()
                .forEach {
                    it.value.onSentRideChange(
                        action = SentChangeActionType.START,
                        sentItem = sentName,
                        monitoring = configuration.monitoringDeviceConfiguration.map()
                    )
                }
        }
    }

    override fun stopSent(sent: CoreSent?) {
        // TODO: start/stop/cancel do przerobienia
        val configuration = configurationSource.value!!
        if (configuration.duringRide) {
            eventHelper.generateSentChangedEvent(
                type = SentChangeActionType.STOP,
                sentId = sent?.sentNumber,
                rideCoordinatorConfiguration = configuration
            )
            callbacksContainer.get()
                .forEach {
                    it.value.onSentRideChange(
                        action = SentChangeActionType.STOP,
                        sentItem = sent,
                        monitoring = configuration.monitoringDeviceConfiguration.map()
                    )
                }
        }
    }

    override fun cancelSent(sent: CoreSent?) {
        val configuration = configurationSource.value!!
        if (configuration.duringRide) {
            eventHelper.generateSentChangedEvent(
                type = SentChangeActionType.CANCEL,
                sentId = sent?.sentNumber,
                rideCoordinatorConfiguration = configuration
            )
            callbacksContainer.get()
                .forEach {
                    it.value.onSentRideChange(
                        action = SentChangeActionType.CANCEL,
                        sentItem = sent,
                        monitoring = configuration.monitoringDeviceConfiguration.map()
                    )
                }
        }
    }

    override fun changeDeviceToOBU() {
        val configuration = configurationSource.value!!
        updateGps()
        if (configuration.duringRide) {
            eventHelper.generateTrackingDeviceChangedEvent(
                configuration,
                true,
                configuration.duringTolled,
                configuration.duringSent
            )
            callbacksContainer.get()
                .forEach {
                    it.value.onObservationDeviceChanged(
                        newOneIsMobileApp = false,
                        obeId = configuration.monitoringDeviceConfiguration?.monitoringOBEId ?: ""
                    )
                }
        }
    }

    override fun changeDeviceToMobileApp() {
        val configuration = configurationSource.value!!
        updateGps()
        if (configuration.duringRide) {
            eventHelper.generateTrackingDeviceChangedEvent(
                configuration,
                false,
                configuration.duringTolled,
                configuration.duringSent
            )
            callbacksContainer.get()
                .forEach {
                    it.value.onObservationDeviceChanged(
                        newOneIsMobileApp = true,
                        obeId = ""
                    )
                }
        }
    }

    override fun getConfiguration(): RideCoordinatorConfiguration? = configurationSource.value

    override fun observeConfiguration(): Observable<RideCoordinatorConfiguration> =
        configurationSource

    override fun sanity(): RideCoordinatorSanityCheck = this
    fun gpsConfiguration(): RideCoordinatorGpsConfiguration = this

    override fun isReallyRunning(): Boolean {
        return timerDisposable != null && !timerDisposable!!.isDisposed
    }

    override fun useOnlyGps() {
        val configuration = configurationSource.value!!
        if (configuration.duringRide) return
        configurationSource.onNext(configuration)
        writeSettingsUseCase.execute(
            Settings.RIDE_COORDINATOR_CONFIGURATION,
            configuration.toJSON()
        ).subscribe()
    }

    override fun useOnlyAgps() {
        val configuration = configurationSource.value!!
        if (configuration.duringRide) return
        configurationSource.onNext(configuration)
        writeSettingsUseCase.execute(
            Settings.RIDE_COORDINATOR_CONFIGURATION,
            configuration.toJSON()
        ).subscribe()
    }

    override fun debugUseBoth() {
        val configuration = configurationSource.value!!
        if (configuration.duringRide) return
        configurationSource.onNext(configuration)
        writeSettingsUseCase.execute(
            Settings.RIDE_COORDINATOR_CONFIGURATION,
            configuration.toJSON()
        ).subscribe()
    }

    @SuppressLint("CheckResult")
    override fun onConfigurationUpdated(newConfig: RideCoordinatorConfiguration) {
        // check what changed in configuration and depending on it, update own config
        Log.d("RC_UPDATES", "Config has just updated :D")
        val oldConfig = getConfiguration() ?: return

        // overwrite config in memory
        configurationSource.onNext(newConfig)
        // and on hard storage
        writeSettingsUseCase.execute(
            Settings.RIDE_COORDINATOR_CONFIGURATION,
            newConfig.toJSON()
        ).subscribe({ }, {})
        Log.d("RC_UPDATES", newConfig.toJSON())


        if (oldConfig.duringRide) {
            if (!newConfig.duringSent && !newConfig.duringTolled) {
                stopRide().subscribe()
                return
            }

            // monitoring device
            if (oldConfig.monitoringDeviceConfiguration?.monitoringByApp != newConfig.monitoringDeviceConfiguration?.monitoringByApp) {
                // check and send required start events if not done before
                // monitoring changed
                if (newConfig.monitoringDeviceConfiguration?.monitoringByApp == true) {
                    Log.d("RC_UPDATES", "Monitoring device changed to mobile app")
                    rideCacheDatabase.count().subscribe({ count ->
                        if (count == 0) {
                            // reset sending timer
                            writeSettingsUseCase.execute(
                                Settings.LAST_CORRECT_DATA_SENDING_TIMESTAMP,
                                System.currentTimeMillis().toString()
                            ).subscribe({
                                device().changeDeviceToMobileApp()
                            }, {
                                device().changeDeviceToMobileApp()
                            })
                        } else {
                            device().changeDeviceToMobileApp()
                        }
                    }, {
                        device().changeDeviceToMobileApp()
                    })
                } else {
                    Log.d("RC_UPDATES", "Monitoring device changed to OBU")
                    // send starting event
                    if (!newConfig.generatedAtLeastOneEventInSessionForTolled && newConfig.duringTolled) {
                        eventHelper.generateTolledChangedEvent(newConfig)
                        writeSettingsUseCase.execute(
                            Settings.RIDE_COORDINATOR_CONFIGURATION,
                            newConfig.toJSON()
                        )
                    }

                    // update last sending timestamp if no unsent events were in cache
                    device().changeDeviceToOBU()
                }
            }

            // tolled configuration
            if (oldConfig.duringTolled != newConfig.duringTolled) {
                // tolled changed
                if (!newConfig.duringTolled) {
                    // we're dropping tolled ride
                    tolled().stopTolled()
                }
                if (newConfig.duringTolled) {
                    // we're starting tolled ride
                    tolled().startTolled(true)
                }
            } else {
                if (oldConfig.duringTolled) {
                    // check if something changed in trailer
                    // did we had trailer?
                    val hadTrailer =
                        oldConfig.tolledConfiguration?.trailerUsedAndCategoryWillBeIncreased
                            ?: false
                    val haveTrailer =
                        newConfig.tolledConfiguration?.trailerUsedAndCategoryWillBeIncreased
                            ?: false
                    if (hadTrailer != haveTrailer) {
                        if (hadTrailer) {
                            callbacksContainer.get()
                                .forEach {
                                    it.value.onTrailerChange(
                                        newConfig.tolledConfiguration?.trailerUsedAndCategoryWillBeIncreased
                                            ?: false
                                    )
                                }
                        }
                        if (haveTrailer) {
                            callbacksContainer.get()
                                .forEach {
                                    it.value.onTrailerChange(
                                        newConfig.tolledConfiguration?.trailerUsedAndCategoryWillBeIncreased
                                            ?: false
                                    )
                                }
                        }
                        // send new activate vehicle - we added or removed trailer
                        tolled().startTolled(false)
                    }
                }
                // there is nothing else - we're in tolled, so no more comparisons required
            }

            // sent
            if (newConfig.duringSent) {
                // compare sent packages
                val oldSent = oldConfig.sentConfiguration
                val newSent = newConfig.sentConfiguration

                // freshly started sent packages
                // some sents could be activated, find them
                for (item in newSent?.selectedSentList ?: mutableListOf()) {
                    if (oldSent?.selectedSentList?.find { it.sentNumber.contentEquals(item.sentNumber) } == null) {
                        // new sent, need to start it - it's "item"
                        Log.d("RC_UPDATES", "New sent found - ${item.sentNumber}")
                        startSent(item)
                    }
                }

                // freshly finished sent packages
                // some sents could be finished, find them and send events
                for (item in newSent?.finishedSentList ?: mutableListOf()) {
                    if (oldSent?.finishedSentList?.find { it.sentNumber.contentEquals(item.sentNumber) } == null) {
                        // new sent, need to start it - it's "item"
                        Log.d("RC_UPDATES", "Finished sent found - ${item.sentNumber}")
                        stopSent(item)
                    }
                }


                // cancelled sent packages
                for (item in oldSent?.selectedSentList ?: mutableListOf()) {
                    val foundInNewSelected =
                        newSent?.selectedSentList?.find { it.sentNumber.contentEquals(item.sentNumber) }
                    val foundInNewFinished =
                        newSent?.finishedSentList?.find { it.sentNumber.contentEquals(item.sentNumber) }
                    if (foundInNewSelected == null && foundInNewFinished == null) {
                        // item was cancelled
                        Log.d("RC_UPDATES", "Cancelled sent found - ${item.sentNumber}")
                        cancelSent(item)
                    }
                }
            }
        } else {
            // path for configuring not-working RC
            // do nothing, just replace config
        }
    }

    override fun pauseTolledRide() {
        configurationSource.value?.let { configuration ->
            val configurationCopy = configuration.deepCopy()
            configurationCopy.disabledTolledConfiguration = configurationCopy.tolledConfiguration
            configurationCopy.tolledConfiguration = null
            onConfigurationUpdated(configurationCopy)
        }
    }

    override fun resumeDisabledRide(): Boolean {
        val updatePossible = configurationSource.value?.disabledTolledConfiguration != null
        if (!updatePossible) return false
        val configurationCopy = configurationSource.value!!.deepCopy()
        configurationCopy.tolledConfiguration = configurationCopy.disabledTolledConfiguration
        configurationCopy.disabledTolledConfiguration = null
        onConfigurationUpdated(configurationCopy)
        return true
    }

    override fun onTimeShiftDetected(lastKnownTime: Long) {
        // check if we're during ride
        if (getConfiguration()?.duringRide != true) return
        // we detected that app was in sleep for some time
        eventHelper.generateResumeApplicationEvent(lastKnownTime, false)
        timeShiftDetectorCounter.resetCounter()
    }
}

private fun MonitoringDeviceConfiguration?.map(): MonitoringDeviceConfiguration =
    if (this == null) MonitoringDeviceConfiguration()
    else MonitoringDeviceConfiguration(this.monitoringByApp, this.monitoringOBEId)