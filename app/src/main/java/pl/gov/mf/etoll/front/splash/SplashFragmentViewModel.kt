package pl.gov.mf.etoll.front.splash

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.gov.mf.etoll.base.BaseDatabindingViewModel
import pl.gov.mf.etoll.core.CoreComposedUC
import pl.gov.mf.etoll.core.devicecompatibility.DeviceCompatibilityUC
import pl.gov.mf.etoll.core.devicecompatibility.types.BatteryOptimisationInfo
import pl.gov.mf.etoll.core.permissions.GpsPermissionsUC
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorUC
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorV3
import pl.gov.mf.etoll.core.watchdog.CoreWatchdog
import pl.gov.mf.etoll.front.MainActivity
import pl.gov.mf.etoll.front.regulationsv2.RegulationsProvider
import pl.gov.mf.etoll.front.splash.SplashFragmentViewModel.SplashMode.SPLASH_ALL_LOADED
import pl.gov.mf.etoll.front.splash.SplashFragmentViewModel.SplashMode.SPLASH_ASK_OPTIMIZATION
import pl.gov.mf.etoll.front.splash.SplashFragmentViewModel.SplashMode.SPLASH_ASK_PERMISSIONS
import pl.gov.mf.etoll.front.splash.SplashFragmentViewModel.SplashMode.SPLASH_CODE_ERROR
import pl.gov.mf.etoll.front.splash.SplashFragmentViewModel.SplashMode.SPLASH_DEVICE_COMPATIBILITY
import pl.gov.mf.etoll.front.splash.SplashFragmentViewModel.SplashMode.SPLASH_DEVICE_COMPATIBILITY_ERROR
import pl.gov.mf.etoll.front.splash.SplashFragmentViewModel.SplashMode.SPLASH_IDLE
import pl.gov.mf.etoll.front.splash.SplashFragmentViewModel.SplashMode.SPLASH_LOADING
import pl.gov.mf.etoll.front.splash.SplashFragmentViewModel.SplashMode.SPLASH_LOADING_ERROR
import pl.gov.mf.etoll.front.splash.SplashFragmentViewModel.SplashMode.SPLASH_SECURITY
import pl.gov.mf.etoll.front.splash.SplashFragmentViewModel.SplashMode.SPLASH_SECURITY_ERROR
import pl.gov.mf.etoll.initialization.LoadableSystemsLoaderUC.LoadSystemUseCase
import pl.gov.mf.etoll.networking.NetworkingUC
import pl.gov.mf.etoll.security.checker.SecuritySanityCheckerUseCase.ValidateSecuritySanityUseCase
import pl.gov.mf.etoll.storage.database.ridecache.RideCacheDatabase
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC
import pl.gov.mf.etoll.translations.AppLanguageManagerUC
import pl.gov.mf.mobile.utils.addSafe
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SplashFragmentViewModel : BaseDatabindingViewModel() {
    companion object {
        private const val SPLASH_IDLE_TIMER = 1000L
    }

    @Inject
    lateinit var checkGpsPermissionsUseCase: GpsPermissionsUC.CheckGpsPermissionsUseCase

    @Inject
    lateinit var checkBatteryOptimisationUseCase: DeviceCompatibilityUC.CheckBatteryOptimisationUseCase

    @Inject
    lateinit var validateSecuritySanityUseCase: ValidateSecuritySanityUseCase

    @Inject
    lateinit var checkWifiServiceAvailable: DeviceCompatibilityUC.CheckWifiServiceAvailableUseCase

    @Inject
    lateinit var readSettingsUseCase: SettingsUC.ReadSettingsUseCase

    @Inject
    lateinit var writeSettingsUseCase: SettingsUC.WriteSettingsUseCase

    @Inject
    lateinit var getCoreStatusUseCase: CoreComposedUC.GetCoreStatusUseCase

    @Inject
    lateinit var loadSystemsUseCase: LoadSystemUseCase

    @Inject
    lateinit var languageWasDetectedOrSetUseCase: AppLanguageManagerUC.LanguageWasDetectedOrSetUseCase

    @Inject
    lateinit var rideCoordinatorV3: RideCoordinatorV3

    @Inject
    lateinit var coreWatchdog: CoreWatchdog

    @Inject
    lateinit var regulationsProvider: RegulationsProvider

    @Inject
    lateinit var cleanBusinessNumberUseCase: NetworkingUC.CleanBusinessNumberUseCase

    @Inject
    lateinit var rideCacheDatabase: RideCacheDatabase

    @Inject
    lateinit var checkIfRideIsInProgressUseCase: RideCoordinatorUC.CheckIfRideIsInProgressUseCase

    var splashState: MutableLiveData<SplashMode> = MutableLiveData(SPLASH_IDLE)

    private var permissionErrorCounter = 0

    fun loadSystems() {
        compositeDisposable.addSafe(
            loadSystemsUseCase.execute()
                .subscribe({ loadingCompleted ->
                    if (loadingCompleted) {
                        splashState.postValue(SPLASH_SECURITY)
                    }
                }, {
                    splashState.postValue(SPLASH_LOADING_ERROR)
                })
        )
    }

    override fun onResume() {
        super.onResume()
        if (splashState.value!! == SPLASH_IDLE) {
            compositeDisposable.addSafe(Observable.interval(
                SPLASH_IDLE_TIMER,
                TimeUnit.MILLISECONDS
            )
                .timeInterval()
                .take(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    splashState.postValue(SPLASH_LOADING)
                })
        }
    }

    fun checkSecurity() {
        compositeDisposable.addSafe(
            validateSecuritySanityUseCase.execute()
                .subscribe({
                    splashState.postValue(
                        if (it.isNotEmpty()) SPLASH_SECURITY_ERROR else SPLASH_DEVICE_COMPATIBILITY
                    )
                }, {
                    splashState.postValue(SPLASH_SECURITY_ERROR)
                })
        )
    }

    fun checkCompatibility() {
        splashState.postValue(
            if (checkWifiServiceAvailable.execute()) {
                decideOnNextStepsBeforeStartingApp()
            } else SPLASH_DEVICE_COMPATIBILITY_ERROR
        )
    }

    private var awaitingOptimisationDecision = false
    private var awaitingGpsDecision = false
    private var allLoaded = false

    private fun decideOnNextStepsBeforeStartingApp(): SplashMode =
        if (rideCoordinatorV3.getConfiguration()?.duringRide == true) {
            // first of all, check if we have serial number - if not, stop ride
            if (readSettingsUseCase.executeForString(Settings.DEVICE_ID).isNullOrEmpty()) {
                onUserRequestedRideStop(cleanDatabase = true)
                SPLASH_IDLE
            }
            // we're during ride, check battery optimization
            when {
                awaitingOptimisationDecision || awaitingGpsDecision -> SPLASH_IDLE
                checkBatteryOptimisationUseCase.execute() == BatteryOptimisationInfo.OPTIMISATION_ENABLED -> {
                    awaitingOptimisationDecision = true
                    SPLASH_ASK_OPTIMIZATION
                }

                !checkGpsPermissionsUseCase.execute() && rideCoordinatorV3.getConfiguration()?.monitoringDeviceConfiguration?.monitoringByApp == true -> {
                    // or now check permissions
                    awaitingGpsDecision = true
                    SPLASH_ASK_PERMISSIONS
                }

                else -> {
                    // security for one-time-call as this one is called more times due to other conditions(all correct)
                    if (!allLoaded)
                        rideCoordinatorV3.checkAndResumeRideAfterAppRestart()
                    allLoaded = true
                    SPLASH_ALL_LOADED
                }
            }
        } else {
            if (readSettingsUseCase.executeForString(Settings.DEVICE_ID).isNullOrEmpty()) {
                onStopStep2(cleanDatabase = true)
                SPLASH_IDLE
            } else
                SPLASH_ALL_LOADED
        }

    fun retry(mode: SplashMode) {
        when (mode) {
            SPLASH_LOADING_ERROR -> SPLASH_LOADING
            // this is special kind of error - it means that one of observables called exception
            // TODO: log this exception to analytics
            SPLASH_LOADING -> SPLASH_CODE_ERROR
            else -> null
        }?.run {
            splashState.postValue(this)
        }
    }

    override fun shouldCheckSecuritySanityOnThisView(): Boolean = false

    fun resetNavigation() {
        splashState.postValue(SPLASH_IDLE)
    }

    fun onGpsPermissionGranted() {
        awaitingGpsDecision = false
        decideOnNextStepsBeforeStartingApp()
    }

    fun onBatteryOptimizationDisabled() {
        awaitingOptimisationDecision = false
        decideOnNextStepsBeforeStartingApp()
    }

    fun onUserRequestedRideStop(cleanDatabase: Boolean = false) {
        awaitingOptimisationDecision = false
        awaitingGpsDecision = false
        compositeDisposable.addSafe(rideCoordinatorV3.stopRide(true).subscribe({
            onStopStep2(cleanDatabase)
        }, {
            onStopStep2(cleanDatabase)
        }))
    }

    fun notifyActivityStarted(activity: MainActivity) {
        coreWatchdog.onAppGoesToForeground(
            activity,
            activity,
            activity,
            activity.getOverlayIntent()
        )
    }

    fun getNavigationTarget(retValue: (NavigationTarget) -> Unit) {
        // migrate sent flag
        onIO {
            if (!readSettingsUseCase.executeForBooleanV2(Settings.SENT_FLAG_OVERRIDDEN_V122)) {
                writeSettingsUseCase.executeV2(Settings.SUPPORT_FOR_SENT_ENABLED, true)
                writeSettingsUseCase.executeV2(Settings.SENT_FLAG_OVERRIDDEN_V122, true)
            }
            if (checkIfRideIsInProgressUseCase.execute() == true) {
                onMain {
                    retValue(NavigationTarget.DASHBOARD)
                }
            } else {
                val overrideSelectedLanguage =
                    readSettingsUseCase.executeForBooleanV2(Settings.SELECTED_LANGUAGE_WEAK_SAVE)
                if (isLanguageDetectedCorrectly() && !overrideSelectedLanguage) {
                    val mode = regulationsProvider.getCurrentRegulationsMode()
                    val newRegulations = (mode != RegulationsProvider.Mode.ALL_ACCEPTED)
                    if (newRegulations) {
                        onMain {
                            retValue(NavigationTarget.REGULATIONS)
                        }
                    } else {
                        val bnScreenShown =
                            readSettingsUseCase.executeForBoolean(Settings.ACTIVATION_TRANS_SUCCEED)
                        if (!bnScreenShown) {
                            onMain {
                                retValue(NavigationTarget.BUSINESS_NUMBER)
                            }
                        } else {
                            val skipDriverWarning =
                                readSettingsUseCase.executeForBooleanV2(Settings.SKIP_DRIVER_WARNING)
                            onMain {
                                if (skipDriverWarning)
                                    retValue(NavigationTarget.DASHBOARD)
                                else
                                    retValue(NavigationTarget.DRIVER_WARNING)
                            }
                        }
                    }
                } else {
                    onMain {
                        retValue(NavigationTarget.LANGUAGE)
                    }
                }
            }
        }
    }


    private fun isLanguageDetectedCorrectly(): Boolean = languageWasDetectedOrSetUseCase.execute()

    private fun onStopStep2(cleanDatabase: Boolean) {
        if (cleanDatabase) {
            compositeDisposable.addSafe(cleanBusinessNumberUseCase.execute().subscribe {
                viewModelScope.launch(Dispatchers.IO) {
                    regulationsProvider.cleanAllAcceptedRegulations()
                    viewModelScope.launch(Dispatchers.Main) {
                        // clean ride database
                        compositeDisposable.addSafe(
                            rideCacheDatabase.clearDatabase().subscribe({
                                splashState.postValue(SPLASH_ALL_LOADED)
                            }, {// we don't care on returned value
                                splashState.postValue(SPLASH_ALL_LOADED)
                            })
                        )
                    }
                }
            })
        } else {
            // we don't care on returned value
            splashState.postValue(SPLASH_ALL_LOADED)
        }
    }

    enum class NavigationTarget {
        LANGUAGE, REGULATIONS, DRIVER_WARNING, DASHBOARD, BUSINESS_NUMBER
    }

    enum class SplashMode {
        SPLASH_IDLE,
        SPLASH_LOADING,
        SPLASH_LOADING_ERROR,
        SPLASH_DEVICE_COMPATIBILITY,
        SPLASH_DEVICE_COMPATIBILITY_ERROR,
        SPLASH_SECURITY,
        SPLASH_SECURITY_ERROR,
        SPLASH_ALL_LOADED,
        SPLASH_CODE_ERROR,
        SPLASH_ASK_PERMISSIONS,
        SPLASH_ASK_OPTIMIZATION
    }
}
