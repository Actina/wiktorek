package pl.gov.mf.etoll.core

import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.location.LocationManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import pl.gov.mf.etoll.app.di.ApplicationScope
import pl.gov.mf.etoll.core.CoreComposedUC.GetCoreStatusUseCase
import pl.gov.mf.etoll.core.NetworkManagerUC.UpdateStatusUseCase
import pl.gov.mf.etoll.core.androidsettings.AndroidSettingsManager
import pl.gov.mf.etoll.core.androidsettings.AndroidSettingsManagerImpl
import pl.gov.mf.etoll.core.androidsettings.AndroidSettingsManagerUC
import pl.gov.mf.etoll.core.biometric.BiometricAuthManager
import pl.gov.mf.etoll.core.biometric.BiometricAuthManagerImpl
import pl.gov.mf.etoll.core.biometric.BiometricAuthManagerUC
import pl.gov.mf.etoll.core.connection.InternetConnectionStateController
import pl.gov.mf.etoll.core.connection.InternetConnectionStateControllerImpl
import pl.gov.mf.etoll.core.criticalmessages.CriticalMessagesChecker
import pl.gov.mf.etoll.core.criticalmessages.CriticalMessagesCheckerV2Impl
import pl.gov.mf.etoll.core.crmmessages.CrmMessagesManager
import pl.gov.mf.etoll.core.crmmessages.CrmMessagesManagerImpl
import pl.gov.mf.etoll.core.devicecompatibility.DeviceCompatibilityUC.*
import pl.gov.mf.etoll.core.devicecompatibility.DeviceInfoProvider
import pl.gov.mf.etoll.core.devicecompatibility.DeviceInfoProviderImpl
import pl.gov.mf.etoll.core.foregroundservice.ForegroundServiceUC.StartForegroundServiceUseCase
import pl.gov.mf.etoll.core.foregroundservice.ForegroundServiceUC.StopForegroundServiceUseCase
import pl.gov.mf.etoll.core.inactivity.InactivityController
import pl.gov.mf.etoll.core.inactivity.InactivityControllerImpl
import pl.gov.mf.etoll.core.locationTracking.LocationTrackingUC
import pl.gov.mf.etoll.core.notifications.NotificationManager
import pl.gov.mf.etoll.core.notifications.NotificationManagerImpl
import pl.gov.mf.etoll.core.notifications.NotificationManagerUC.CreateNotificationsChannelsUseCase
import pl.gov.mf.etoll.core.overlay.controller.OverlayController
import pl.gov.mf.etoll.core.overlay.controller.OverlayControllerImpl
import pl.gov.mf.etoll.core.permissions.GpsPermissionsUC
import pl.gov.mf.etoll.core.ridecoordinatorv2.events.EventHelper
import pl.gov.mf.etoll.core.ridecoordinatorv2.events.EventHelperImpl
import pl.gov.mf.etoll.core.ridecoordinatorv2.filter.DebugGpsChecker
import pl.gov.mf.etoll.core.ridecoordinatorv2.filter.RideDataFilter
import pl.gov.mf.etoll.core.ridecoordinatorv2.filter.RideDataFilterDecorator
import pl.gov.mf.etoll.core.ridecoordinatorv2.location.LocationServiceProvider
import pl.gov.mf.etoll.core.ridecoordinatorv2.location.LocationServicesProviderAgpsImpl
import pl.gov.mf.etoll.core.ridecoordinatorv2.location.LocationServicesProviderGpsImplementation
import pl.gov.mf.etoll.core.ridecoordinatorv2.location.LocationServicesProviderSelector
import pl.gov.mf.etoll.core.ridecoordinatorv2.location.issues.LocationIssuesDetector
import pl.gov.mf.etoll.core.ridecoordinatorv2.location.issues.LocationIssuesDetectorImpl
import pl.gov.mf.etoll.core.ridecoordinatorv2.location.startloc.StartLocationContainer
import pl.gov.mf.etoll.core.ridecoordinatorv2.location.startloc.StartLocationContainerImpl
import pl.gov.mf.etoll.core.ridecoordinatorv2.merger.EventStreamLocationMerger
import pl.gov.mf.etoll.core.ridecoordinatorv2.merger.EventStreamLocationMergerImpl
import pl.gov.mf.etoll.core.ridecoordinatorv2.mobile.MobileDataProviderImpl
import pl.gov.mf.etoll.core.ridecoordinatorv2.mobile.MobileDataprovider
import pl.gov.mf.etoll.core.ridecoordinatorv2.normalizer.RideDataNormalizer
import pl.gov.mf.etoll.core.ridecoordinatorv2.normalizer.RideDataNormalizerDecorator
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorV3
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorV3Impl
import pl.gov.mf.etoll.core.ridecoordinatorv2.sender.RideCoordinatorV3Sender
import pl.gov.mf.etoll.core.ridecoordinatorv2.sender.RideCoordinatorV3SenderImpl
import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.SentAlgorithm
import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.SentAdvancedAlgorithm
import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.processors.LocationProcessorsList
import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.processors.RemoveEmptyLocationsProcessor
import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.processors.SentGeoAlgorithmProcessor
import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.processors.TimeFlowControlProcessor
import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.selector.SentAlgorithmSelector
import pl.gov.mf.etoll.core.ridehistory.collector.RideHistoryCollector
import pl.gov.mf.etoll.core.ridehistory.collector.RideHistoryCollectorImpl
import pl.gov.mf.etoll.core.security.SecurityConfigUC
import pl.gov.mf.etoll.core.security.unlock.PinUnlockUC
import pl.gov.mf.etoll.core.security.unlock.ResetToUnlockUC
import pl.gov.mf.etoll.core.security.unlock.UnlockManager
import pl.gov.mf.etoll.core.security.unlock.UnlockManagerImpl
import pl.gov.mf.etoll.core.sound.SoundNotificationsControllerImpl
import pl.gov.mf.etoll.core.uiinterfaces.NotificationHistoryControllerImpl
import pl.gov.mf.etoll.core.vehiclesdisplaymanagement.RecentVehiclesProvider
import pl.gov.mf.etoll.core.vehiclesdisplaymanagement.RecentVehiclesProviderImpl
import pl.gov.mf.etoll.core.vehiclesdisplaymanagement.VehiclesDisplayManagementUC
import pl.gov.mf.etoll.core.watchdog.CoreWatchdog
import pl.gov.mf.etoll.core.watchdog.CoreWatchdogImplV2
import pl.gov.mf.etoll.core.watchdog.CoreWatchdogUC
import pl.gov.mf.etoll.core.watchdog.CoreWatchdogUC.AppGoesToBackgroundUseCaseWatchdog
import pl.gov.mf.etoll.core.watchdog.CoreWatchdogUC.AppGoesToForegroundUseCase
import pl.gov.mf.etoll.core.watchdog.awake.CoreAwakePushController
import pl.gov.mf.etoll.core.watchdog.awake.CoreAwakePushControllerImpl
import pl.gov.mf.etoll.core.watchdog.counter.TimeShiftDetectorCounter
import pl.gov.mf.etoll.core.watchdog.counter.TimeShiftDetectorCounterImpl
import pl.gov.mf.etoll.core.watchdog.fakegps.FakeGpsCollector
import pl.gov.mf.etoll.core.watchdog.fakegps.FakeGpsCollectorImpl
import pl.gov.mf.etoll.core.watchdog.logsender.CoreLogSender
import pl.gov.mf.etoll.core.watchdog.logsender.CoreLogSenderImpl
import pl.gov.mf.etoll.core.watchdog.minversion.WatchdogMinVersionController
import pl.gov.mf.etoll.core.watchdog.minversion.WatchdogMinVersionControllerImpl
import pl.gov.mf.etoll.core.watchdog.netlock.CoreNetlockChecker
import pl.gov.mf.etoll.core.watchdog.netlock.CoreNetlockCheckerImpl
import pl.gov.mf.etoll.core.watchdog.notification.CoreDuringRideNotificationController
import pl.gov.mf.etoll.core.watchdog.notification.CoreDuringRideNotificationControllerImpl
import pl.gov.mf.etoll.core.watchdog.ridefinish.RideFinishController
import pl.gov.mf.etoll.core.watchdog.ridefinish.RideFinishControllerImpl
import pl.gov.mf.etoll.core.watchdog.sending.CoreWatchdogSendingQueueController
import pl.gov.mf.etoll.core.watchdog.sending.CoreWatchdogSendingQueueControllerImpl
import pl.gov.mf.etoll.core.watchdog.status.CoreWatchdogStatusUpdateController
import pl.gov.mf.etoll.core.watchdog.status.CoreWatchdogStatusUpdateControllerImpl
import pl.gov.mf.etoll.core.watchdog.timeissues.TimeIssuesController
import pl.gov.mf.etoll.core.watchdog.timeissues.TimeIssuesControllerImpl
import pl.gov.mf.etoll.core.watchdog.wakelock.WakelockController
import pl.gov.mf.etoll.core.watchdog.wakelock.WakelockControllerImpl
import pl.gov.mf.etoll.interfaces.NotificationHistoryController
import pl.gov.mf.etoll.interfaces.SoundNotificationController
import pl.gov.mf.etoll.logging.LogUseCase
import pl.gov.mf.etoll.storage.settings.SettingsUC
import pl.gov.mf.etoll.storage.settings.SettingsUC.ReadSettingsUseCase
import pl.gov.mf.mobile.networking.api.interceptors.MinimumVersionTriggersCallbacks
import pl.gov.mf.mobile.networking.api.interceptors.ResponseCheckInterceptor
import pl.gov.mf.mobile.networking.api.interceptors.TimeIssuesTriggersCallbacks

@Module
class CoreModule {

    @Provides
    @ApplicationScope
    fun providesOverlayController(impl: OverlayControllerImpl): OverlayController = impl

    @Provides
    @ApplicationScope
    fun providesNotificationManager(impl: NotificationManagerImpl): NotificationManager = impl

    @Provides
    @ApplicationScope
    fun providesRideDataFilter(impl: RideDataFilterDecorator): RideDataFilter = impl

    @Provides
    @ApplicationScope
    fun providesRideDataNormalizer(impl: RideDataNormalizerDecorator): RideDataNormalizer = impl

    @Provides
    fun providesCreateNotificationChannelsUseCase(ds: NotificationManager): CreateNotificationsChannelsUseCase =
        CreateNotificationsChannelsUseCase(ds)

    @Provides
    fun providesSystemNotificationManager(applicationContext: Context): android.app.NotificationManager =
        applicationContext.getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager

    @Provides
    fun providesStartForegroundServiceUseCase(
        overlayController: OverlayController,
    ): StartForegroundServiceUseCase =
        StartForegroundServiceUseCase(overlayController)

    @Provides
    fun providesStopForegroundServiceUseCase(
        overlayController: OverlayController,
    ): StopForegroundServiceUseCase =
        StopForegroundServiceUseCase(overlayController)

    @Provides
    @ApplicationScope
    fun providesCriticalMessagesChecker(impl: CriticalMessagesCheckerV2Impl): CriticalMessagesChecker =
        impl

    @Provides
    fun providesDeviceInfoProvider(
        impl: DeviceInfoProviderImpl,
    ): DeviceInfoProvider = impl

    @Provides
    fun providesCheckBatteryOptimisationUseCase(impl: DeviceInfoProvider): CheckBatteryOptimisationUseCase =
        CheckBatteryOptimisationUseCase(impl)

    @Provides
    fun providesGetManufacturerBatteryOptimizationIntent(impl: DeviceInfoProvider): GetManufacturerBatteryOptimizationIntent =
        GetManufacturerBatteryOptimizationIntent(impl)

    @Provides
    fun providesCheckGpsAgpsOrNoneUseCase(impl: DeviceInfoProvider): CheckGpsTypeUseCase =
        CheckGpsTypeUseCase(impl)

    @Provides
    fun providesCheckInternetConnectionUseCase(impl: DeviceInfoProvider): CheckInternetConnectionUseCase =
        CheckInternetConnectionUseCase(impl)

    @Provides
    fun providesCheckAutoTimeEnabledUseCase(impl: DeviceInfoProvider): CheckAutoTimeEnabledUseCase =
        CheckAutoTimeEnabledUseCase(impl)

    @Provides
    fun providesCheckWifiServiceAvailableUseCase(impl: DeviceInfoProvider): CheckWifiServiceAvailableUseCase =
        CheckWifiServiceAvailableUseCase(impl)

    @Provides
    fun providesCheckGpsLocationServiceAvailableUseCase(impl: DeviceInfoProvider): CheckGpsLocationServiceAvailable =
        CheckGpsLocationServiceAvailable(impl)

    @Provides
    fun providesGetSerialNumberForRideUseCase(
        readSettingsUseCase: ReadSettingsUseCase,
    ): CoreComposedUC.GetDeviceIdUseCase =
        CoreComposedUC.GetDeviceIdUseCase(readSettingsUseCase)

    @Provides
    fun providesGetCoreStatusUseCase(
        updateStatusUseCase: UpdateStatusUseCase,
        readSettingsUseCase: ReadSettingsUseCase,
        timeIssuesDetectedUseCase: CoreComposedUC.TimeIssuesDetectedUseCase,
    ): GetCoreStatusUseCase =
        GetCoreStatusUseCase(updateStatusUseCase, readSettingsUseCase, timeIssuesDetectedUseCase)

    @Provides
    fun providesLocationManager(context: Context): LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    @Provides
    fun providesCheckGpsStateUseCase(
        ds: DeviceInfoProvider,
        locationManager: LocationManager,
    ): CheckGpsStateUseCase =
        CheckGpsStateUseCase(ds, locationManager)

    @Provides
    fun providesGetSystemInfoUseCase(ds: DeviceInfoProvider): GetSystemInfoUseCase =
        GetSystemInfoUseCase(ds)

    @ApplicationScope
    @Provides
    fun providesBackgroundSupportWatchdog(ds: CoreWatchdogImplV2): CoreWatchdog =
        ds

    @Provides
    fun providesAppGoesBackgroundUseCase(ds: CoreWatchdog): AppGoesToBackgroundUseCaseWatchdog =
        AppGoesToBackgroundUseCaseWatchdog(ds)

    @Provides
    fun providesAppGoesForegroundUseCase(ds: CoreWatchdog): AppGoesToForegroundUseCase =
        AppGoesToForegroundUseCase(ds)

    @Provides
    fun providesObserveStatusUpdatesUseCase(ds: CoreWatchdogStatusUpdateController): CoreWatchdogUC.ObserveStatusUpdatesUseCase =
        CoreWatchdogUC.ObserveStatusUpdatesUseCase(ds)

    @Provides
    fun providesChangeCoreObservationModeUseCase(ds: CoreWatchdog): CoreWatchdogUC.ChangeCoreObservationModeUseCase =
        CoreWatchdogUC.ChangeCoreObservationModeUseCase(ds)

    @ApplicationScope
    @Provides
    fun providesVehiclesToDisplayProvider(
        readSettingsUseCase: ReadSettingsUseCase,
        writeSettingsUseCase: SettingsUC.WriteSettingsUseCase,
        getCoreStatusUseCase: GetCoreStatusUseCase,
    ): RecentVehiclesProvider =
        RecentVehiclesProviderImpl(
            readSettingsUseCase,
            writeSettingsUseCase,
            getCoreStatusUseCase
        )

    @Provides
    fun providesAddRecentVehiclesUseCase(recentVehiclesProvider: RecentVehiclesProvider): VehiclesDisplayManagementUC.AddRecentVehiclesUseCase =
        VehiclesDisplayManagementUC.AddRecentVehiclesUseCase(recentVehiclesProvider)

    @Provides
    fun providesGetVehiclesDividedIntoRecentAndOtherUseCase(recentVehiclesProvider: RecentVehiclesProvider):
            VehiclesDisplayManagementUC.GetVehiclesDividedIntoRecentAndOtherUseCase =
        VehiclesDisplayManagementUC.GetVehiclesDividedIntoRecentAndOtherUseCase(
            recentVehiclesProvider
        )

    @ApplicationScope
    @Provides
    fun providesRideCoordinator(impl: RideCoordinatorV3Impl): RideCoordinatorV3 = impl

    @ApplicationScope
    @Provides
    fun providesEventHelper(impl: EventHelperImpl): EventHelper = impl

    @ApplicationScope
    @Provides
    fun providesLocationService(impl: LocationServicesProviderSelector): LocationServiceProvider =
        impl

    @ApplicationScope
    @Provides
    fun providesAgpsImpl(
        context: Context,
        fusedLocationProviderClient: FusedLocationProviderClient,
        fakeGpsCollector: FakeGpsCollector,
        logUseCase: LogUseCase,
        debugGpsChecker: DebugGpsChecker,
        startLocationContainer: StartLocationContainer,
        locationIssuesDetector: LocationIssuesDetector,
    )
            : LocationServicesProviderAgpsImpl = LocationServicesProviderAgpsImpl(
        context,
        fusedLocationProviderClient,
        fakeGpsCollector,
        logUseCase,
        debugGpsChecker,
        startLocationContainer,
        locationIssuesDetector
    )

    @ApplicationScope
    @Provides
    fun providesGpsImpl(
        context: Context,
        fakeGpsCollector: FakeGpsCollector,
        logUseCase: LogUseCase,
        debugGpsChecker: DebugGpsChecker,
        startLocationContainer: StartLocationContainer,
        locationIssuesDetector: LocationIssuesDetector,
    ): LocationServicesProviderGpsImplementation = LocationServicesProviderGpsImplementation(
        context,
        logUseCase,
        fakeGpsCollector,
        debugGpsChecker,
        startLocationContainer,
        locationIssuesDetector
    )

    @ApplicationScope
    @Provides
    fun providesStartLocationContainer(impl: StartLocationContainerImpl): StartLocationContainer =
        impl

    @ApplicationScope
    @Provides
    fun providesLocationIssuesDetector(impl: LocationIssuesDetectorImpl): LocationIssuesDetector =
        impl

    @ApplicationScope
    @Provides
    fun providesMobileService(impl: MobileDataProviderImpl): MobileDataprovider = impl

    @Provides
    fun providesEventStreamLocationMerger(impl: EventStreamLocationMergerImpl): EventStreamLocationMerger =
        impl

    @ApplicationScope
    @Provides
    fun providesFusedLocationProviderClient(context: Context): FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @ApplicationScope
    @Provides
    fun providesStartFusedLocationTrackingUseCase(
        locationServiceProvider: LocationServiceProvider,
    ) = LocationTrackingUC.StartLocationTrackingUseCase(locationServiceProvider)

    @ApplicationScope
    @Provides
    fun providesStopFusedLocationTrackingUseCase(
        locationServiceProvider: LocationServiceProvider,
    ) = LocationTrackingUC.StopLocationTrackingUseCase(locationServiceProvider)

    @ApplicationScope
    @Provides
    fun providesGetLastFusedLocationFromLocationServiceUseCase(
        locationServiceProvider: LocationServiceProvider,
    ) = LocationTrackingUC.GetLastLocationUseCase(locationServiceProvider)

    @ApplicationScope
    @Provides
    fun providesGetStartingLocationUseCase(
        locationServiceProvider: LocationServiceProvider,
    ) = LocationTrackingUC.GetStartingLocationUseCase(locationServiceProvider)

    @ApplicationScope
    @Provides
    fun providesSender(impl: RideCoordinatorV3SenderImpl): RideCoordinatorV3Sender = impl

    @ApplicationScope
    @Provides
    fun providesSentAlgorithm(
        selector: SentAlgorithmSelector,
    ): SentAlgorithm = selector

    @ApplicationScope
    @Provides
    fun providesSoundController(impl: SoundNotificationsControllerImpl): SoundNotificationController =
        impl

    @ApplicationScope
    @Provides
    fun providesRideHistoryCollector(impl: RideHistoryCollectorImpl): RideHistoryCollector = impl

    @ApplicationScope
    @Provides
    fun providesAndroidSettingsManager(impl: AndroidSettingsManagerImpl): AndroidSettingsManager =
        impl

    @ApplicationScope
    @Provides
    fun provideOpenSecuritySettings(
        androidSettingsManager: AndroidSettingsManager,
    ): AndroidSettingsManagerUC.OpenSecuritySettingsUseCase =
        AndroidSettingsManagerUC.OpenSecuritySettingsUseCase(androidSettingsManager)

    @ApplicationScope
    @Provides
    fun provideBiometricAuthManager(impl: BiometricAuthManagerImpl): BiometricAuthManager = impl

    @ApplicationScope
    @Provides
    fun providePerformBiometricAuthentication(
        biometricAuthManager: BiometricAuthManager,
    ): BiometricAuthManagerUC.PerformBiometricAuthentication =
        BiometricAuthManagerUC.PerformBiometricAuthentication(biometricAuthManager)

    @Provides
    fun provideSavePinUseCase(writeSettingsUseCase: SettingsUC.WriteSettingsUseCase) =
        SecurityConfigUC.SavePinUseCase(writeSettingsUseCase)

    @Provides
    fun provideSavePasswordUseCase(writeSettingsUseCase: SettingsUC.WriteSettingsUseCase) =
        SecurityConfigUC.SavePasswordUseCase(writeSettingsUseCase)

    @Provides
    fun provideSaveBiometricAuthOptionUseCase(writeSettingsUseCase: SettingsUC.WriteSettingsUseCase) =
        SecurityConfigUC.SaveBiometricAuthOptionUseCase(writeSettingsUseCase)

    @Provides
    fun provideGetBiometricAuthOptionUseCase(readSettingsUseCase: ReadSettingsUseCase) =
        SecurityConfigUC.GetBiometricAuthOptionUseCase(readSettingsUseCase)

    @Provides
    fun provideIsSecurityConfigValid(readSettingsUseCase: ReadSettingsUseCase) =
        SecurityConfigUC.IsSecurityConfigValidUseCase(readSettingsUseCase)

    @Provides
    fun provideCheckPasswordUseCase(readSettingsUseCase: ReadSettingsUseCase) =
        SecurityConfigUC.CheckPasswordUseCase(readSettingsUseCase)

    @Provides
    fun provideClearSecurityConfigUseCase(unlockManager: UnlockManager) =
        SecurityConfigUC.ClearSecurityConfigUseCase(unlockManager)

    @Provides
    fun provideCheckPinUseCase(readSettingsUseCase: ReadSettingsUseCase) =
        SecurityConfigUC.CheckPinUseCase(readSettingsUseCase)

    @Provides
    fun provideInvalidPinEnteredUseCase(unlockManager: UnlockManager) =
        PinUnlockUC.InvalidPinEnteredUseCase(unlockManager)

    @Provides
    fun provideClearPinInvalidAttemptsNumberUC(writeSettingsUseCase: SettingsUC.WriteSettingsUseCase) =
        PinUnlockUC.ClearPinInvalidAttemptsNumberUC(writeSettingsUseCase)

    @Provides
    fun provideLockPinInputUC(writeSettingsUseCase: SettingsUC.WriteSettingsUseCase) =
        PinUnlockUC.LockPinInputUC(writeSettingsUseCase)

    @Provides
    fun provideCheckPinInputLockStatusUC(unlockManager: UnlockManager) =
        PinUnlockUC.CheckPinInputLockStatusUC(unlockManager)

    @Provides
    fun provideCheckAttemptsNumberLeftUC(unlockManager: UnlockManager) =
        PinUnlockUC.CheckAttemptsNumberLeftUC(unlockManager)

    @Provides
    fun providesPasswordCheckAttemptsNumberLeftUC(unlockManager: UnlockManager) =
        ResetToUnlockUC.CheckAttemptsNumberLeftUC(unlockManager)

    @Provides
    fun providesInvalidPasswordEnteredUseCase(unlockManager: UnlockManager) =
        ResetToUnlockUC.InvalidPasswordEnteredUseCase(unlockManager)

    @Provides
    fun providesResetSecurityConfigUseCase(unlockManager: UnlockManager) =
        ResetToUnlockUC.ResetSecurityConfigUseCase(unlockManager)

    @Provides
    fun providesClearPasswordInvalidAttemptsNumberUC(writeSettingsUseCase: SettingsUC.WriteSettingsUseCase) =
        ResetToUnlockUC.ClearPasswordInvalidAttemptsNumberUC(writeSettingsUseCase)

    @Provides
    fun providesLockPasswordInputUC(writeSettingsUseCase: SettingsUC.WriteSettingsUseCase) =
        ResetToUnlockUC.LockPasswordInputUC(writeSettingsUseCase)

    @Provides
    fun providesCheckPasswordInputLockStatusUC(unlockManager: UnlockManager) =
        ResetToUnlockUC.CheckPasswordInputLockStatusUC(unlockManager)

    @Provides
    fun providesMarkAppAsLocked(writeSettingsUseCase: SettingsUC.WriteSettingsUseCase) =
        PinUnlockUC.MarkAppAsLockedUseCase(writeSettingsUseCase)

    @Provides
    fun providesUnlockAppUseCase(unlockManager: UnlockManager) =
        PinUnlockUC.UnlockAppUseCase(unlockManager)

    @Provides
    fun providesUnlockManager(impl: UnlockManagerImpl): UnlockManager = impl

    @ApplicationScope
    @Provides
    fun providesNotificationHistoryController(impl: NotificationHistoryControllerImpl): NotificationHistoryController =
        impl

    @Provides
    fun providesCheckGpsPermissionsUseCase(context: Context) =
        GpsPermissionsUC.CheckGpsPermissionsUseCase(context)

    @Provides
    fun providesAskForGpsPermissionsUseCase(writeSettingsUseCase: SettingsUC.WriteSettingsUseCase) =
        GpsPermissionsUC.AskForGpsPermissionsUseCase(writeSettingsUseCase)

    @Provides
    fun providesCanAskForGpsPermissionsUseCase(readSettingsUseCase: ReadSettingsUseCase) =
        GpsPermissionsUC.CanAskForGpsPermissionsUseCase(readSettingsUseCase)

    @Provides
    fun providesOpenAppDetailsSettingsUseCase(androidSettingsManager: AndroidSettingsManager) =
        AndroidSettingsManagerUC.OpenAppDetailsSettingsUseCase(androidSettingsManager)

    @ApplicationScope
    @Provides
    fun providesCRMMessagesManager(impl: CrmMessagesManagerImpl): CrmMessagesManager = impl

    @ApplicationScope
    @Provides
    fun providesSendingQueueController(impl: CoreWatchdogSendingQueueControllerImpl): CoreWatchdogSendingQueueController =
        impl

    @ApplicationScope
    @Provides
    fun providesCoreStatusUpdateController(impl: CoreWatchdogStatusUpdateControllerImpl): CoreWatchdogStatusUpdateController =
        impl

    @ApplicationScope
    @Provides
    fun providesCoreInactivityController(impl: InactivityControllerImpl): InactivityController =
        impl

    @ApplicationScope
    @Provides
    fun providesCoreAwakePushController(impl: CoreAwakePushControllerImpl): CoreAwakePushController =
        impl

    @ApplicationScope
    @Provides
    fun providesFakeGpsDetector(impl: FakeGpsCollectorImpl): FakeGpsCollector = impl

    @ApplicationScope
    @Provides
    fun providesCoreDuringRideNotificationController(impl: CoreDuringRideNotificationControllerImpl): CoreDuringRideNotificationController =
        impl

    @ApplicationScope
    @Provides
    fun providesWakeLockController(impl: WakelockControllerImpl): WakelockController = impl

    @ApplicationScope
    @Provides
    fun providesInternetStateController(impl: InternetConnectionStateControllerImpl): InternetConnectionStateController =
        impl

    @ApplicationScope
    @Provides
    fun providesRideFinishController(impl: RideFinishControllerImpl): RideFinishController = impl

    @ApplicationScope
    @Provides
    fun providesLogSender(impl: CoreLogSenderImpl): CoreLogSender = impl

    @ApplicationScope
    @Provides
    fun providesTimeIssuesController(impl: TimeIssuesControllerImpl): TimeIssuesController = impl


    @Provides
    fun providesUnauthorizedInterceptor(
        callbacks: TimeIssuesTriggersCallbacks,
        minimumVersionTriggersCallbacks: MinimumVersionTriggersCallbacks,
    ): ResponseCheckInterceptor =
        ResponseCheckInterceptor(callbacks, minimumVersionTriggersCallbacks)

    @ApplicationScope
    @Provides
    fun providesTimeIssuesControllerInterceptorCallbacks(impl: TimeIssuesController): TimeIssuesTriggersCallbacks =
        impl

    @Provides
    fun providesTimeIssuesDetectedUC(ds: TimeIssuesController): CoreComposedUC.TimeIssuesDetectedUseCase =
        CoreComposedUC.TimeIssuesDetectedUseCase(ds)

    @ApplicationScope
    @Provides
    fun providesSentAdvancedAlgorithm(processors: LocationProcessorsList): SentAdvancedAlgorithm =
        SentAdvancedAlgorithm(processors)

    @ApplicationScope
    @Provides
    fun providesSentProcessorsList(
        flowControlProcessor: TimeFlowControlProcessor,
        removeEmptyLocationsProcessor: RemoveEmptyLocationsProcessor,
        sentAlgorithmProcessor: SentGeoAlgorithmProcessor,
    ): LocationProcessorsList = LocationProcessorsList(
        listOf(removeEmptyLocationsProcessor, flowControlProcessor, sentAlgorithmProcessor)
    )

    @ApplicationScope
    @Provides
    fun providesShowInstanceIssueUseCase(ds: CoreNetlockChecker): CoreComposedUC.ShowInstanceIssueUseCase =
        CoreComposedUC.ShowInstanceIssueUseCase(ds)

    @ApplicationScope
    @Provides
    fun providesCoreNetlock(impl: CoreNetlockCheckerImpl): CoreNetlockChecker = impl

    @ApplicationScope
    @Provides
    fun providesRideCounter(impl: TimeShiftDetectorCounterImpl): TimeShiftDetectorCounter = impl

    @ApplicationScope
    @Provides
    fun providesWatchdogMinVersionController(impl: WatchdogMinVersionControllerImpl): WatchdogMinVersionController =
        impl

    @ApplicationScope
    @Provides
    fun providesWatchdogMinVersionTrigger(impl: WatchdogMinVersionController): MinimumVersionTriggersCallbacks =
        impl
}