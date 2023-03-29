package pl.gov.mf.etoll.front.dashboard

import android.content.Context
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import org.joda.time.DateTime
import pl.gov.mf.etoll.base.BaseDatabindingViewModel
import pl.gov.mf.etoll.commons.formatMillisToReadableUiTime
import pl.gov.mf.etoll.core.CoreComposedUC
import pl.gov.mf.etoll.core.NetworkManagerUC
import pl.gov.mf.etoll.core.connection.InternetConnectionStateController
import pl.gov.mf.etoll.core.devicecompatibility.DeviceCompatibilityUC
import pl.gov.mf.etoll.core.model.*
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorConfiguration
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorUC
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorV3
import pl.gov.mf.etoll.front.config.RideConfigurationCoordinator
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC
import pl.gov.mf.etoll.utils.formatAccountBalanceText
import pl.gov.mf.mobile.networking.api.interceptors.WrongSystemTimeException
import pl.gov.mf.mobile.ui.components.adapters.TranslationsAdapter
import pl.gov.mf.mobile.utils.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class DashboardViewModel : BaseDatabindingViewModel() {

    companion object {
        private const val UI_UPDATE_INTERVAL_MS = 1000L
        private const val BUTTON_NOT_ACTIVE_AFTER_STARTING_RIDE_FOR_S = 5
        private const val INTERNET_STATE_CHECK_IN_SECS = 5
    }

    private val _headerMode: MutableLiveData<String> =
        MutableLiveData("dashboard_empty_title")
    val headerMode: LiveData<String>
        get() = _headerMode

    private val _startingRideConfiguration: MutableLiveData<Boolean> = MutableLiveData(false)
    val startingRideConfiguration: LiveData<Boolean>
        get() = _startingRideConfiguration

    private val _navigationTarget: MutableLiveData<NavigationTarget> =
        MutableLiveData(NavigationTarget.NONE)
    val navigationTarget: LiveData<NavigationTarget>
        get() = _navigationTarget

    private val _dashboardConfiguration: MutableLiveData<DashboardConfiguration> = MutableLiveData()
    val dashboardConfiguration: LiveData<DashboardConfiguration>
        get() = _dashboardConfiguration

    private val _timerValue: MutableLiveData<String> = MutableLiveData("00:00:00")
    val timerValue: LiveData<String>
        get() = _timerValue

    private val _noInternetErrorShouldBeShown: MutableLiveData<Int> = MutableLiveData(View.GONE)
    val noInternetErrorShouldBeShown: LiveData<Int>
        get() = _noInternetErrorShouldBeShown

    private val _rideButtonShouldBeEnabled: MutableLiveData<Boolean> = MutableLiveData(false)
    val rideButtonShouldBeEnabled: LiveData<Boolean>
        get() = _rideButtonShouldBeEnabled

    private val _dataSyncInProgress: MutableLiveData<Boolean> =
        MutableLiveData()
    val dataSyncInProgress: LiveData<Boolean> = _dataSyncInProgress

    private val _overlayFunctionInfoShouldBeShown: MutableLiveData<Boolean> = MutableLiveData(false)
    val overlayFunctionInfoShouldBeShown: LiveData<Boolean> = _overlayFunctionInfoShouldBeShown

    @Inject
    lateinit var getSentList: NetworkManagerUC.CheckSentListUseCase

    @Inject
    lateinit var updateStatusUseCase: NetworkManagerUC.UpdateStatusUseCase

    @Inject
    lateinit var readSettingsUseCase: SettingsUC.ReadSettingsUseCase

    @Inject
    lateinit var startRideUseCase: RideCoordinatorUC.StartRideUseCase

    @Inject
    lateinit var stopRideUseCase: RideCoordinatorUC.StopRideUseCase

    @Inject
    lateinit var getCoreStatusUseCase: CoreComposedUC.GetCoreStatusUseCase

    @Inject
    lateinit var writeSettingsUseCase: SettingsUC.WriteSettingsUseCase

    @Inject
    lateinit var checkGpsStateUseCase: DeviceCompatibilityUC.CheckGpsStateUseCase

    @Inject
    lateinit var rideCoordinatorV3: RideCoordinatorV3 // TODO: clean to UC

    @Inject
    lateinit var rideConfigurationCoordinator: RideConfigurationCoordinator

    @Inject
    lateinit var internetConnectionStateController: InternetConnectionStateController

    @Inject
    lateinit var timeIssuesDetectedUseCase: CoreComposedUC.TimeIssuesDetectedUseCase

    @Inject
    lateinit var showInstanceIssueUseCase: CoreComposedUC.ShowInstanceIssueUseCase

    @Inject
    lateinit var checkSyncStatusUseCase: CoreComposedUC.IsDataSyncInProgressUseCase

    @Inject
    lateinit var checkGpsLocationServiceAvailable: DeviceCompatibilityUC.CheckGpsLocationServiceAvailable

    private var timerDisposable: Disposable? = null
    private val syncCounter = AtomicInteger(0)

    val shouldShowNoGpsDialog: Boolean
        get() = !checkGpsLocationServiceAvailable.execute() && isGpsRequiredForRide()

    override fun onCreate() {
        super.onCreate()
        _overlayFunctionInfoShouldBeShown.value =
            !readSettingsUseCase.executeForBoolean(Settings.OVERLAY_INFO_SHOWN)
    }

    override fun onResume() {
        super.onResume()
        // reset start ride button if required
        _startingRideConfiguration.postValue(false)
        // update config
        updateConfiguration()
        compositeDisposable.addSafe(
            internetConnectionStateController.observeConnectionState().subscribe { connected ->
                _noInternetErrorShouldBeShown.postValue(if (!connected) View.VISIBLE else View.GONE)
            })
        compositeDisposable.addSafe(checkSyncStatusUseCase.execute().subscribe { syncing ->
            if (_dataSyncInProgress.value == null || syncing != _dataSyncInProgress.value) {
                _dataSyncInProgress.value = syncing
            }
        })
    }


    override fun onPause() {
        super.onPause()
        timerDisposable?.dispose()
        timerDisposable = null
    }

    fun markOverlayInfoAsShown() {
        _overlayFunctionInfoShouldBeShown.value = false
        compositeDisposable.addSafe(
            writeSettingsUseCase.execute(Settings.OVERLAY_INFO_SHOWN, true).subscribe()
        )
    }

    private val lastRideCoordinatorHashCode = AtomicInteger(-1)
    private val lastStatusHashCode = AtomicInteger(-1)
    private val lastConfigurationHashCode = AtomicInteger(-1)

    private fun updateConfiguration() {
        val rideConfiguration = rideCoordinatorV3.getConfiguration()
        if (rideConfiguration?.isConfigured == true) {
            if (rideConfiguration.duringSent) {
                if (rideConfiguration.duringTolled)
                // mixed
                    _headerMode.postValue("dashboard_configured_title_mixed")
                else
                // sent
                    _headerMode.postValue("dashboard_configured_title_sent")
            } else {
                // tolled
                _headerMode.postValue("dashboard_configured_title_tolled")
            }
            // configured ride - UI
            timerDisposable =
                Observable.interval(0, UI_UPDATE_INTERVAL_MS, TimeUnit.MILLISECONDS).subscribe({
                    // update timer
                    val startTime =
                        readSettingsUseCase.executeForString(Settings.RIDE_START_TIMESTAMP).toLong()
                    val rideCoordinatorStatus = rideCoordinatorV3.getConfiguration()
                    val timerNewValue =
                        if (startTime <= 0L || rideCoordinatorStatus?.duringRide != true) "00:00:00" else {
                            val output =
                                (System.currentTimeMillis() - startTime).formatMillisToReadableUiTime()
                            if (output == null) {
                                timeIssuesDetectedUseCase.execute(true)
                                "--.--.--"
                            } else output
                        }
                    _timerValue.postValue(timerNewValue)

                    // update button
                    // we should allow to stop ride if we generated at least one correct event OR we were without events for 5s
                    val eventOrTimeConditionPassed: Boolean =
                        if (rideCoordinatorStatus?.generatedAtLeastOneEventInSessionForTolled == true || rideCoordinatorStatus?.generatedAtLeastOneEventInSessionForSent == true) true
                        else (startTime > 0 && DateTime(startTime).isBefore(
                            DateTime.now().minusSeconds(BUTTON_NOT_ACTIVE_AFTER_STARTING_RIDE_FOR_S)
                        ))
                    _rideButtonShouldBeEnabled.postValue(
                        (rideCoordinatorStatus?.duringRide ?: false && eventOrTimeConditionPassed) || (!(rideCoordinatorStatus?.duringRide
                            ?: false))
                    )

                    val status = getCoreStatusUseCase.execute()
                    val configuration = rideCoordinatorV3.getConfiguration()
                    // this should be called on demand, when new status came or configuration changed
                    if (rideCoordinatorStatus.hashCode() != lastRideCoordinatorHashCode.get() ||
                        status.hashCode() != lastStatusHashCode.get() ||
                        configuration.hashCode() != lastConfigurationHashCode.get()
                    ) {
                        lastRideCoordinatorHashCode.set(
                            rideCoordinatorStatus.hashCode()
                        )
                        lastStatusHashCode.set(status.hashCode())
                        lastConfigurationHashCode.set(configuration.hashCode())
                        Log.d("UI_UPDATE", "UI update requested")
                        updateUIByConfiguration()
                    }
                }, {
                    // issue
                    updateConfiguration()
                })
        } else {
            // not configured ride
            _headerMode.postValue("dashboard_empty_title")
            timerDisposable?.dispose()
            _dashboardConfiguration.postValue(
                DashboardConfiguration(
                    alsoIncludesTolledRide = false,
                    accountBalance = 0.0,
                    rideInProgress = false,
                    monitoringDeviceName = "",
                    vehiclePlateNumber = "",
                    configuredModeVisibility = View.GONE,
                    noConfigurationModeVisibility = View.VISIBLE,
                    coreAccountPriorityType = CoreAccountPriorityType.UNKNOWN
                )
            )
        }
    }

    private fun updateUIByConfiguration() {
        val configuration = rideCoordinatorV3.getConfiguration()
        val status = getCoreStatusUseCase.execute()
//        val startTime = readSettingsUseCase.executeForString(Settings.RIDE_START_TIMESTAMP).toLong()
        // fallback stop requirement - in case of dropping configuration
        if (configuration?.isConfigured != true) {
            updateConfiguration()
            timerDisposable.disposeSafe()
            timerDisposable = null
            return
        }
        // important, this value will determine if we have selected or configured tolled (where configuration requires selection)
        val tolledConfigured = configuration.duringTolled
        val accountType = configuration.tolledConfiguration?.vehicle?.accountInfo?.type
        val accountInitialized =
            configuration.tolledConfiguration?.vehicle?.accountInfo?.balance?.isInitialized ?: false
        val accountBalance: Double =
            if (accountType == null) 0.0 else getBalance(status, configuration)

        val rideInProgress = configuration.duringRide
        val monitoringDeviceName =
            if (configuration.monitoringDeviceConfiguration?.monitoringByApp == true
            ) "ride_status_monitoring_device_phone" else "ride_status_monitoring_device_zsl"
        val vehiclePlateNumber =
            if (configuration.tolledConfiguration?.vehicle?.licensePlate != null) configuration.tolledConfiguration?.vehicle?.licensePlate
                ?: "" else ""

        val configuredVisiblity = if (configuration.isConfigured) View.VISIBLE else View.GONE
        val notConfiguredVisiblity =
            if (!configuration.isConfigured) View.VISIBLE else View.GONE
        val priorityType =
            CoreAccountPriorityType.fromString(configuration.tolledConfiguration?.vehicle?.accountInfo?.balance?.priority)
        if (configuration.sentConfiguration?.sentSelectionWasPossibleAndDone != true && configuration.sentConfiguration?.sentListWasDownloaded == true) {
            // check current navigation target
            _navigationTarget.value?.let { target ->
                if (target == NavigationTarget.NONE) {
                    // warning, check if we should show sent continuation or finish ride screen
                    if (configuration.sentConfiguration?.availableSentList?.data?.isNullOrEmpty() == true) {
                        if (status?.sentActivated != true)
                        // security path for data malformation - should never happen in real life
                            _navigationTarget.postValue(NavigationTarget.RIDE_CANT_BE_CONTINUED)
                        else {
                            // data will be auto-updated as it's reference, not deep copy
                            configuration.sentConfiguration?.sentListWasDownloaded = true
                            // show dialog with information for user
                            notifySentConfigurationWasDone()
                            _navigationTarget.postValue(NavigationTarget.CONTINUE_SENT_CONFIGURATION_NO_SENT)
                        }
                    } else
                        _navigationTarget.postValue(NavigationTarget.CONTINUE_SENT_CONFIGURATION)
                }
            }
        }
        _dashboardConfiguration.postValue(
            DashboardConfiguration(
                tolledConfigured,
                accountBalance,
                rideInProgress,
                monitoringDeviceName,
                vehiclePlateNumber,
                configuredVisiblity,
                notConfiguredVisiblity,
                priorityType,
                accountInitialized,
                accountType
            )
        )

    }

    private fun getBalance(
        status: CoreStatus?,
        configuration: RideCoordinatorConfiguration,
    ): Double {
        return if (status != null && !vehicleIdInArray(
                status.vehicles,
                configuration.tolledConfiguration?.vehicleId
            )
        ) {
            // vehicle is no longer in status, so we should end ride
            _navigationTarget.postValue(NavigationTarget.RIDE_CANT_BE_CONTINUED)
            0.0
        } else
            if (configuration.tolledConfiguration?.vehicle == null || CoreAccountTypes.fromLiteral(
                    configuration.tolledConfiguration?.vehicle?.accountInfo?.type
                ) != CoreAccountTypes.PREPAID
            ) {
                0.0
            } else {
                val found =
                    status?.vehicles?.find { it.id == configuration.tolledConfiguration?.vehicleId ?: -1 }
                if (found == null) {
                    // fallback, should never happen
                    configuration.tolledConfiguration?.vehicle?.accountInfo?.balance?.value ?: 0.0
                } else
                    found.accountInfo.balance?.value ?: 0.0
            }
    }

    private fun vehicleIdInArray(vehicles: Array<CoreVehicle>, vehicleId: Long?): Boolean {
        if (vehicleId == null) return false
        for (vehicle in vehicles)
            if (vehicle.id == vehicleId)
                return true
        return false
    }


    fun tryToUpdateStatusAndSent() {
        if (readSettingsUseCase.executeForBoolean(Settings.NETWORK_COMMUNICATION_LOCKED)) {
            showInstanceIssueUseCase.execute()
            return
        }

        _startingRideConfiguration.postValue(true)
        // first of all - update sent list if required
        if (readSettingsUseCase.executeForBoolean(Settings.SUPPORT_FOR_SENT_ENABLED)) {
            // try to download sent list
            compositeDisposable.addSafe(getSentList.execute().subscribe({ sentList ->
                tryToUpdateStatus(true, sentList)
            }, {
                if (it is WrongSystemTimeException) {
                    timeIssuesDetectedUseCase.execute(true)
                    return@subscribe
                }
                tryToUpdateStatus(true, null)
            }))
        } else {
            // no sent support, so just update status and proceed
            tryToUpdateStatus()
        }
    }

    private fun tryToUpdateStatus(sentIsSupported: Boolean = false, sentList: CoreSentMap? = null) {
        // update status and tryToStartConfiguration
        compositeDisposable.addSafe(updateStatusUseCase.execute().subscribe({ status ->
            tryToStartConfiguration(status, sentList, sentIsSupported)
        }, {
            if (it is WrongSystemTimeException) {
                // special case
                _navigationTarget.postValue(NavigationTarget.RESET_LOADER)
                timeIssuesDetectedUseCase.execute(true)
                return@subscribe
            }
            val statusString = readSettingsUseCase.executeForString(Settings.STATUS)
            var status: CoreStatus
            try {
                status = statusString.toObject()
            } catch (_: Exception) {
                // sting is null or model changed - stop configuration and show generic error
                _startingRideConfiguration.postValue(false)
                // show dialog with info that we can't start any ride - no conditions matched
                _navigationTarget.postValue(NavigationTarget.NO_CONFIGURATION_POSSIBLE)
                return@subscribe
            }
            tryToStartConfiguration(status, sentList, sentIsSupported)
        }))
    }

    private fun tryToStartConfiguration(
        status: CoreStatus,
        sentList: CoreSentMap?,
        sentIsSupported: Boolean,
    ) {
        val sentIsPossible =
            sentIsSupported && (status.sentActivated || sentList?.data?.size ?: 0 > 0)
        val tolledIsPossible = status.crmActivated
        if (sentIsPossible || tolledIsPossible) {
            _startingRideConfiguration.postValue(false)
            // we can start only if sent is possible or tolled is possible
            rideConfigurationCoordinator.resetCoordinator()
            rideConfigurationCoordinator.onConfigurationStarted(
                sentIsPossible,
                tolledIsPossible,
                sentList == null, // null means no sent list was downloaded
                sentList,
                status
            )
            _navigationTarget.postValue(NavigationTarget.CONFIGURATION_START)
        } else {
            _startingRideConfiguration.postValue(false)
            // show dialog with info that we can't start any ride - no conditions matched
            _navigationTarget.postValue(NavigationTarget.NO_CONFIGURATION_POSSIBLE)
        }
    }

    fun resetNavigationTarget() {
        _navigationTarget.postValue(NavigationTarget.NONE)
    }


    fun onRideDetailsClicked() {
        _navigationTarget.postValue(NavigationTarget.RIDE_DETAILS)
    }

    /**
     * "Cancel" configuration, we should drop, save changes and refresh view
     */
    fun dropConfiguration() {
        rideConfigurationCoordinator.resetCoordinator()
        rideCoordinatorV3.clearConfiguration()
    }

    /**
     * Start ride
     */
    fun startRide(context: Context) {
        // check if gps is enabled and available + if fly mode is enabled
        if (AirplaneModeUtils.isAirplaneModeOn(
                context
            ) || rideCoordinatorV3.getConfiguration()?.monitoringDeviceConfiguration?.monitoringByApp == true && !checkGpsStateUseCase.executeAlternativeCode()
        ) {
            // show error
            _navigationTarget.postValue(NavigationTarget.RIDE_CANT_BE_STARTED_GPS)
        } else {
            startRideUseCase.execute()
        }
    }

    /**
     * Stop ride
     */
    fun stopRide() {
        compositeDisposable.addSafe(stopRideUseCase.execute().subscribe { finished ->
        })
    }

    fun onAccountTopUpClick() {
        rideCoordinatorV3.getConfiguration()?.let { configuration ->
            if (configuration.duringTolled && CoreAccountTypes.fromLiteral(
                    configuration.tolledConfiguration?.vehicle?.accountInfo?.type
                ) == CoreAccountTypes.PREPAID
            ) {
                if (dashboardConfiguration.value != null)
                // navigate to account top up fragment
                    _navigationTarget.postValue(NavigationTarget.ACCOUNT_TOP_UP)
                else {
                    _navigationTarget.postValue(NavigationTarget.ACCOUNT_TOP_UP_NOT_INITIALIZED)
                }
            }
        }
    }

    fun prepareConfigurationForResumeOnSent() {
        rideConfigurationCoordinator.setDataBasedOnExistingConfiguration(
            RideConfigurationCoordinator.RideConfigurationDestination.SENT_LIST
        )
    }

    fun isTolled(): Boolean = rideCoordinatorV3.getConfiguration()?.duringTolled ?: false
    fun isSent(): Boolean = rideCoordinatorV3.getConfiguration()?.duringSent ?: false

    private fun notifySentConfigurationWasDone() {
        compositeDisposable.addSafe(
            Completable.create {
                // no update needed as we update ref
                rideCoordinatorV3.onConfigurationUpdated(
                    rideCoordinatorV3.getConfiguration()!!.deepCopy().apply {
                        sentConfiguration?.availableSentList = CoreSentMap(emptyMap())
                        sentConfiguration?.sentListWasDownloaded = true
                        sentConfiguration?.sentSelectionWasPossibleAndDone = true
                    })
            }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe()
        )
    }

    fun isGpsRequiredForRide(): Boolean =
        rideCoordinatorV3.getConfiguration()?.monitoringDeviceConfiguration?.monitoringByApp == true

    fun resetSyncState() {
        _dataSyncInProgress.postValue(false)
    }
}


enum class NavigationTarget {
    NONE,
    CONFIGURATION_START,
    RIDE_DETAILS,
    NO_CONFIGURATION_POSSIBLE,
    RIDE_CANT_BE_CONTINUED,
    RIDE_CANT_BE_STARTED_GPS,
    CONTINUE_SENT_CONFIGURATION,
    CONTINUE_SENT_CONFIGURATION_NO_SENT,
    ACCOUNT_TOP_UP,
    ACCOUNT_TOP_UP_NOT_INITIALIZED,
    RESET_LOADER
}

data class DashboardConfiguration(
    var alsoIncludesTolledRide: Boolean,
    var accountBalance: Double = 0.0,
    var rideInProgress: Boolean,
    var monitoringDeviceName: String,
    var vehiclePlateNumber: String = "",
    var configuredModeVisibility: Int,
    var noConfigurationModeVisibility: Int,
    var coreAccountPriorityType: CoreAccountPriorityType,
    val accountInitialized: Boolean = false,
    val accountType: String? = null,
) {

    val postPaidContainerVisible
        get() = CoreAccountTypes.fromLiteral(accountType) == CoreAccountTypes.POSTPAID && alsoIncludesTolledRide

    val prepaidContainerVisible: Boolean
        get() = CoreAccountTypes.fromLiteral(accountType) != CoreAccountTypes.POSTPAID && alsoIncludesTolledRide

    val accountBalanceColorName: String
        get() = when {
            CoreAccountTypes.fromLiteral(accountType) == CoreAccountTypes.POSTPAID -> "dashboardBalancePostpaid"
            else -> when (coreAccountPriorityType) {
                CoreAccountPriorityType.ZERO_BALANCE, CoreAccountPriorityType.LOW_BALANCE -> "dashboardBalanceZeroLow"
                else -> "dashboardBalanceOther"
            }
        }

    val accountBalanceValue: TranslationsAdapter.ConditionallyTranslatedText =
        formatAccountBalanceText(accountBalance, accountInitialized, accountType)

    val rideStartButtonVisibility: Int
        get() = if (rideInProgress) View.INVISIBLE else View.VISIBLE

    val rideEndButtonVisibility: Int
        get() = if (!rideInProgress) View.INVISIBLE else View.VISIBLE

    val vehiclePlateNumberAreaVisibility: Int
        get() = if (alsoIncludesTolledRide) View.VISIBLE else View.INVISIBLE
}