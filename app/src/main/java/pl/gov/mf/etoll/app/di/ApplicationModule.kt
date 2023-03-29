package pl.gov.mf.etoll.app.di

import android.content.ContentResolver
import android.content.Context
import dagger.Module
import dagger.Provides
import pl.gov.mf.etoll.BuildConfig
import pl.gov.mf.etoll.app.CommonGetBusinessNumberUseCase
import pl.gov.mf.etoll.app.NkspoDefaultSettings
import pl.gov.mf.etoll.app.timeshift.TimeShiftAdapter
import pl.gov.mf.etoll.app.timeshift.TimeShiftAdapterImpl
import pl.gov.mf.etoll.appmode.AppModeManager
import pl.gov.mf.etoll.core.CoreComposedUC
import pl.gov.mf.etoll.core.messaging.FcmManager
import pl.gov.mf.etoll.core.messaging.FcmManagerImpl
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorV3
import pl.gov.mf.etoll.core.ridehistory.RideHistoryDeleteObsoleteActivities
import pl.gov.mf.etoll.core.uiinterfaces.BatteryStateInterfaceControllerImpl
import pl.gov.mf.etoll.core.uiinterfaces.DataSenderStateInterfaceControllerImpl
import pl.gov.mf.etoll.core.uiinterfaces.GpsStateInterfaceControllerImpl
import pl.gov.mf.etoll.core.watchdog.CoreWatchdog
import pl.gov.mf.etoll.core.watchdog.minversion.BuildVersionProvider
import pl.gov.mf.etoll.demo.watchdog.DemoWatchdogImpl
import pl.gov.mf.etoll.front.config.RideConfigurationCoordinator
import pl.gov.mf.etoll.front.config.RideConfigurationCoordinatorImpl
import pl.gov.mf.etoll.front.regulationsv2.RegulationsProvider
import pl.gov.mf.etoll.front.regulationsv2.RegulationsProviderImpl
import pl.gov.mf.etoll.front.watchdog.DemoWatchdog
import pl.gov.mf.etoll.initialization.LoadableSystemsLoader
import pl.gov.mf.etoll.interfaces.BatteryStateInterfaceController
import pl.gov.mf.etoll.interfaces.DataSenderStateInterfaceController
import pl.gov.mf.etoll.interfaces.GpsStateInterfaceController
import pl.gov.mf.etoll.maps.MapsRendererConfigurator
import pl.gov.mf.etoll.maps.MapsRendererConfiguratorImpl
import pl.gov.mf.etoll.overlay.OverlayServiceContract
import pl.gov.mf.etoll.overlay.OverlayServicePresenter
import pl.gov.mf.etoll.starter.AppStarter
import pl.gov.mf.etoll.starter.AppStarterImpl
import pl.gov.mf.etoll.storage.settings.SettingsManager
import pl.gov.mf.etoll.storage.settings.defaults.SettingsDefaultsProvider
import pl.gov.mf.etoll.translations.AppLanguageManager
import pl.gov.mf.etoll.translations.AppLanguageManagerUC
import pl.gov.mf.etoll.utils.BuildVersionProviderImpl
import pl.gov.mf.mobile.networking.api.interceptors.BasicHeadersProvider
import pl.gov.mf.mobile.networking.api.interceptors.v2.ResponseCheckInterceptorV2
import pl.gov.mf.mobile.storage.database.providers.realm.RealmDatabaseProvider

@Module
class ApplicationModule(private val applicationContext: Context) {

    companion object {
        private val USER_AGENT_HEADER =
            "SPOE_KAS/${BuildConfig.VERSION_NAME} (Android ${android.os.Build.VERSION.SDK_INT})"
    }

    @Provides
    fun providesServicePresenter(impl: OverlayServicePresenter): OverlayServiceContract.Presenter =
        impl

    @Provides
    @ApplicationScope
    fun providesContext() = applicationContext

    @Provides
    fun providesResources() = applicationContext.resources

    @Provides
    fun provideStarter(impl: AppStarterImpl): AppStarter = impl

    @Provides
    @ApplicationScope
    fun providesMapsRendererConfigurator(impl: MapsRendererConfiguratorImpl): MapsRendererConfigurator =
        impl

    @Provides
    fun providesContentResolver(): ContentResolver = applicationContext.contentResolver

    @ApplicationScope
    @Provides
    fun providesDefaultSettings(settings: NkspoDefaultSettings): SettingsDefaultsProvider = settings

    @Provides
    fun providesBasicHeadersProvider(getCurrentLanguageUseCase: AppLanguageManagerUC.GetCurrentLanguageUseCase): BasicHeadersProvider =
        object : BasicHeadersProvider {
            override fun getAppVersion(): String = USER_AGENT_HEADER

            override fun getAppLanguage(): String =
                getCurrentLanguageUseCase.execute().apiLanguageName
        }

    @Provides
    fun providesResponseCheckInterceptor(
    ): ResponseCheckInterceptorV2 =
        ResponseCheckInterceptorV2(BuildConfig.VERSION_CODE)

    @ApplicationScope
    @Provides
    fun providesDemoWatchdog(impl: DemoWatchdogImpl): DemoWatchdog = impl

    @ApplicationScope
    @Provides
    fun providesFirebaseManager(impl: FcmManagerImpl): FcmManager = impl

    @ApplicationScope
    @Provides
    fun providesLoaderChain(
        settingsManager: SettingsManager,
        appLanguageManager: AppLanguageManager,
        databaseManager: RealmDatabaseProvider,
        coreWatchdogLoader: CoreWatchdog,
        fcmManager: FcmManager,
        rideCoordinator: RideCoordinatorV3,
        rideHistoryDeleteObsoleteActivities: RideHistoryDeleteObsoleteActivities,
        appModeManager: AppModeManager,
    ): LoadableSystemsLoader = LoadableSystemsLoader().chain(settingsManager)
        .chain(appLanguageManager)
        .chain(appModeManager)
        .chain(databaseManager)
        .chain(rideCoordinator)
        .chain(coreWatchdogLoader)
        .chain(fcmManager)
        .chain(rideHistoryDeleteObsoleteActivities)

    @ApplicationScope
    @Provides
    fun providesBatteryStateInterfaceController(impl: BatteryStateInterfaceControllerImpl): BatteryStateInterfaceController =
        impl

    @ApplicationScope
    @Provides
    fun providesGpsStateInterfaceController(impl: GpsStateInterfaceControllerImpl): GpsStateInterfaceController =
        impl

    @ApplicationScope
    @Provides
    fun providesDataSenderStateInterfaceController(impl: DataSenderStateInterfaceControllerImpl): DataSenderStateInterfaceController =
        impl

    @ApplicationScope
    @Provides
    fun providesBusinessNumberUC(impl: CoreComposedUC.GetBusinessNumberUseCase): CommonGetBusinessNumberUseCase =
        impl

    // it should be here, as activity could be restarted and this helper values are not saved in view model
    @ApplicationScope
    @Provides
    fun providesConfigCoordinator(impl: RideConfigurationCoordinatorImpl): RideConfigurationCoordinator =
        impl

    @ApplicationScope
    @Provides
    fun providesBuildVersion(impl: BuildVersionProviderImpl): BuildVersionProvider = impl

    @ApplicationScope
    @Provides
    fun providesTimeShiftAdapter(impl: TimeShiftAdapterImpl): TimeShiftAdapter = impl


    @Provides
    @ApplicationScope
    fun providesRegulationsProvider(impl: RegulationsProviderImpl): RegulationsProvider = impl
}