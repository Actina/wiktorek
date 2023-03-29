package pl.gov.mf.etoll.front

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import pl.gov.mf.etoll.demo.DemoFragmentComponent
import pl.gov.mf.etoll.demo.DemoFragmentModule
import pl.gov.mf.etoll.front.about.AboutFragmentComponent
import pl.gov.mf.etoll.front.about.AboutFragmentModule
import pl.gov.mf.etoll.front.bottomNavigation.ds.BottomNavigationDataSource
import pl.gov.mf.etoll.front.bottomNavigation.ds.BottomNavigationRepository
import pl.gov.mf.etoll.front.bottomNavigation.ds.BottomNavigationUC
import pl.gov.mf.etoll.front.configmonitoringdevice.ConfigMonitoringDeviceFragmentComponent
import pl.gov.mf.etoll.front.configmonitoringdevice.ConfigMonitoringDeviceFragmentModule
import pl.gov.mf.etoll.front.configridetypeselection.ConfigRideTypeSelectionFragmentComponent
import pl.gov.mf.etoll.front.configridetypeselection.ConfigRideTypeSelectionFragmentModule
import pl.gov.mf.etoll.front.configsentridesselection.SentRidesSelectionFragmentComponent
import pl.gov.mf.etoll.front.configsentridesselection.SentRidesSelectionFragmentModule
import pl.gov.mf.etoll.front.configsentridesselection.details.SentRideDetailsFragmentComponent
import pl.gov.mf.etoll.front.configsentridesselection.details.SentRideDetailsFragmentModule
import pl.gov.mf.etoll.front.configtrailercategory.ConfigTrailerCategoryFragmentComponent
import pl.gov.mf.etoll.front.configtrailercategory.ConfigTrailerCategoryFragmentModule
import pl.gov.mf.etoll.front.configvehicleselection.ConfigVehicleSelectionFragmentComponent
import pl.gov.mf.etoll.front.configvehicleselection.ConfigVehicleSelectionFragmentModule
import pl.gov.mf.etoll.front.critical.CriticalErrorFragment
import pl.gov.mf.etoll.front.dashboard.DashboardFragmentComponent
import pl.gov.mf.etoll.front.dashboard.DashboardFragmentModule
import pl.gov.mf.etoll.front.driverwarning.DriverWarningFragmentComponent
import pl.gov.mf.etoll.front.driverwarning.DriverWarningFragmentModule
import pl.gov.mf.etoll.front.errors.gpsissues.GpsIssuesFragmentComponent
import pl.gov.mf.etoll.front.errors.gpsissues.GpsIssuesFragmentModule
import pl.gov.mf.etoll.front.errors.instanceissues.InstanceIssuesFragmentComponent
import pl.gov.mf.etoll.front.errors.instanceissues.InstanceIssuesFragmentModule
import pl.gov.mf.etoll.front.errors.timeissues.TimeIssuesFragmentComponent
import pl.gov.mf.etoll.front.errors.timeissues.TimeIssuesFragmentModule
import pl.gov.mf.etoll.front.help.HelpFragmentComponent
import pl.gov.mf.etoll.front.help.HelpFragmentModule
import pl.gov.mf.etoll.front.notificationdetails.NotificationDetailsFragmentComponent
import pl.gov.mf.etoll.front.notificationdetails.NotificationDetailsFragmentModule
import pl.gov.mf.etoll.front.notificationhistory.NotificationHistoryFragmentComponent
import pl.gov.mf.etoll.front.notificationhistory.NotificationHistoryFragmentModule
import pl.gov.mf.etoll.front.onboarding.OnboardingFragmentComponent
import pl.gov.mf.etoll.front.onboarding.OnboardingFragmentModule
import pl.gov.mf.etoll.front.registered.RegisteredFragmentComponent
import pl.gov.mf.etoll.front.registered.RegisteredFragmentModule
import pl.gov.mf.etoll.front.regulationsv2.RegulationsFragmentV2Component
import pl.gov.mf.etoll.front.regulationsv2.RegulationsFragmentV2Module
import pl.gov.mf.etoll.front.ridedata.RideDataFragmentComponent
import pl.gov.mf.etoll.front.ridedata.RideDataFragmentModule
import pl.gov.mf.etoll.front.ridedetails.RideDetailsFragmentComponent
import pl.gov.mf.etoll.front.ridedetails.RideDetailsFragmentModule
import pl.gov.mf.etoll.front.ridedetails.sentselection.RideDetailsSentSelectionFragmentComponent
import pl.gov.mf.etoll.front.ridedetails.sentselection.RideDetailsSentSelectionFragmentModule
import pl.gov.mf.etoll.front.ridedetailsmap.RideDetailsMapFragmentComponent
import pl.gov.mf.etoll.front.ridedetailsmap.RideDetailsMapFragmentModule
import pl.gov.mf.etoll.front.rideshistory.RideHistoryFragmentComponent
import pl.gov.mf.etoll.front.rideshistory.RideHistoryFragmentModule
import pl.gov.mf.etoll.front.rideshistory.details.RideHistoryDetailsFragmentComponent
import pl.gov.mf.etoll.front.rideshistory.details.RideHistoryDetailsFragmentModule
import pl.gov.mf.etoll.front.ridesummary.RideSummaryFragmentComponent
import pl.gov.mf.etoll.front.ridesummary.RideSummaryFragmentModule
import pl.gov.mf.etoll.front.security.config.password.ConfigPasswordFragmentComponent
import pl.gov.mf.etoll.front.security.config.password.ConfigPasswordFragmentModule
import pl.gov.mf.etoll.front.security.config.pin.ConfigPinFragmentComponent
import pl.gov.mf.etoll.front.security.config.pin.ConfigPinFragmentModule
import pl.gov.mf.etoll.front.security.confirmwithpassword.SecurityConfirmWithPasswordFragmentComponent
import pl.gov.mf.etoll.front.security.confirmwithpassword.SecurityConfirmWithPasswordFragmentModule
import pl.gov.mf.etoll.front.security.resettounlock.SecurityResetPinCodeToUnlockFragmentComponent
import pl.gov.mf.etoll.front.security.resettounlock.SecurityResetPinCodeToUnlockFragmentModule
import pl.gov.mf.etoll.front.security.settings.SecuritySettingsFragmentComponent
import pl.gov.mf.etoll.front.security.settings.SecuritySettingsFragmentModule
import pl.gov.mf.etoll.front.security.unlock.SecurityUnlockWithPinCodeFragmentComponent
import pl.gov.mf.etoll.front.security.unlock.SecurityUnlockWithPinCodeFragmentModule
import pl.gov.mf.etoll.front.settings.appsettings.AppSettingsFragmentComponent
import pl.gov.mf.etoll.front.settings.appsettings.AppSettingsFragmentModule
import pl.gov.mf.etoll.front.settings.darkmode.DarkModeSettingsFragmentComponent
import pl.gov.mf.etoll.front.settings.darkmode.DarkModeSettingsFragmentModule
import pl.gov.mf.etoll.front.settings.languagesettingsv2.LanguageSettingsFragmentV2Component
import pl.gov.mf.etoll.front.settings.languagesettingsv2.LanguageSettingsFragmentV2Module
import pl.gov.mf.etoll.front.splash.StartupActivitySplashFragmentComponent
import pl.gov.mf.etoll.front.splash.StartupActivitySplashFragmentModule
import pl.gov.mf.etoll.front.tecs.amountSelection.TecsAmountSelectionFragmentComponent
import pl.gov.mf.etoll.front.tecs.amountSelection.TecsAmountSelectionFragmentModule
import pl.gov.mf.etoll.front.tecs.result.TecsTransactionResultFragmentComponent
import pl.gov.mf.etoll.front.tecs.result.TecsTransactionResultFragmentModule
import pl.gov.mf.etoll.front.tecs.transaction.TecsTransactionFragmentComponent
import pl.gov.mf.etoll.front.tecs.transaction.TecsTransactionFragmentModule
import pl.gov.mf.etoll.front.topBars.TopBarsUC
import pl.gov.mf.etoll.initialization.LoadableSystemsLoader
import pl.gov.mf.etoll.initialization.LoadableSystemsLoaderUC.LoadSystemUseCase
import javax.inject.Scope

@MainActivityScope
@Subcomponent(modules = [MainActivityModule::class])
interface MainActivityComponent : BaseComponent<MainActivityViewModel> {
    fun inject(target: MainActivity)

    fun inject(target: CriticalErrorFragment)

    fun changeBottomNavigationStateUseCase(): BottomNavigationUC.RequestBottomNavigationStateUseCase
    fun setStatusBarColorUseCase(): TopBarsUC.SetTopBarsColorUseCase

    fun plus(module: StartupActivitySplashFragmentModule): StartupActivitySplashFragmentComponent
    fun plus(module: DriverWarningFragmentModule): DriverWarningFragmentComponent
    fun plus(module: OnboardingFragmentModule): OnboardingFragmentComponent
    fun plus(module: ConfigPinFragmentModule): ConfigPinFragmentComponent
    fun plus(module: ConfigPasswordFragmentModule): ConfigPasswordFragmentComponent
    fun plus(module: ConfigRideTypeSelectionFragmentModule): ConfigRideTypeSelectionFragmentComponent
    fun plus(module: ConfigMonitoringDeviceFragmentModule): ConfigMonitoringDeviceFragmentComponent
    fun plus(module: ConfigTrailerCategoryFragmentModule): ConfigTrailerCategoryFragmentComponent
    fun plus(module: ConfigVehicleSelectionFragmentModule): ConfigVehicleSelectionFragmentComponent
    fun plus(module: RideSummaryFragmentModule): RideSummaryFragmentComponent
    fun plus(module: RideDetailsFragmentModule): RideDetailsFragmentComponent
    fun plus(module: HelpFragmentModule): HelpFragmentComponent
    fun plus(module: AppSettingsFragmentModule): AppSettingsFragmentComponent
    fun plus(module: DarkModeSettingsFragmentModule): DarkModeSettingsFragmentComponent
    fun plus(module: AboutFragmentModule): AboutFragmentComponent
    fun plus(module: DemoFragmentModule): DemoFragmentComponent
    fun plus(module: DashboardFragmentModule): DashboardFragmentComponent
    fun plus(module: RideDataFragmentModule): RideDataFragmentComponent
    fun plus(module: RideDetailsMapFragmentModule): RideDetailsMapFragmentComponent
    fun plus(module: SentRidesSelectionFragmentModule): SentRidesSelectionFragmentComponent
    fun plus(module: SentRideDetailsFragmentModule): SentRideDetailsFragmentComponent
    fun plus(module: RideDetailsSentSelectionFragmentModule): RideDetailsSentSelectionFragmentComponent
    fun plus(module: RideHistoryFragmentModule): RideHistoryFragmentComponent
    fun plus(module: TecsAmountSelectionFragmentModule): TecsAmountSelectionFragmentComponent
    fun plus(module: TecsTransactionFragmentModule): TecsTransactionFragmentComponent
    fun plus(module: TecsTransactionResultFragmentModule): TecsTransactionResultFragmentComponent
    fun plus(module: SecuritySettingsFragmentModule): SecuritySettingsFragmentComponent
    fun plus(module: SecurityConfirmWithPasswordFragmentModule): SecurityConfirmWithPasswordFragmentComponent
    fun plus(module: SecurityUnlockWithPinCodeFragmentModule): SecurityUnlockWithPinCodeFragmentComponent
    fun plus(module: SecurityResetPinCodeToUnlockFragmentModule): SecurityResetPinCodeToUnlockFragmentComponent
    fun plus(notificationHistoryFragmentModule: NotificationHistoryFragmentModule): NotificationHistoryFragmentComponent
    fun plus(notificationDetailsFragmentModule: NotificationDetailsFragmentModule): NotificationDetailsFragmentComponent
    fun plus(module: RideHistoryDetailsFragmentModule): RideHistoryDetailsFragmentComponent
    fun plus(module: GpsIssuesFragmentModule): GpsIssuesFragmentComponent
    fun plus(module: TimeIssuesFragmentModule): TimeIssuesFragmentComponent
    fun plus(instanceIssuesFragmentModule: InstanceIssuesFragmentModule): InstanceIssuesFragmentComponent
    fun plus(regulationsFragmentV2Module: RegulationsFragmentV2Module): RegulationsFragmentV2Component
    fun plus(languageSettingsFragmentV2Module: LanguageSettingsFragmentV2Module): LanguageSettingsFragmentV2Component
    fun plus(registeredFragmentModule: RegisteredFragmentModule): RegisteredFragmentComponent
//    fun plus(module: CriticalErrorFragmentModule): CriticalErrorFragmentComponent
}

@Module
class MainActivityModule(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle
) {

    @Provides
    @MainActivityScope
    fun providesViewModel(): MainActivityViewModel =
        ViewModelProvider(viewModelStoreOwner).get(MainActivityViewModel::class.java)
            .apply {
                // FYI: No need to care about removing observer: https://github.com/googlecodelabs/android-lifecycles/issues/5
                lifecycle.addObserver(this)
            }

    @MainActivityScope
    @Provides
    fun providesLoadSystemUseCase(ds: LoadableSystemsLoader): LoadSystemUseCase =
        LoadSystemUseCase(ds)


    @MainActivityScope
    @Provides
    fun providesBottomNavigationDS(impl: BottomNavigationRepository): BottomNavigationDataSource =
        impl

    @Provides
    fun providesRequestBottomNavigationChanges(ds: BottomNavigationDataSource): BottomNavigationUC.RequestBottomNavigationStateUseCase =
        BottomNavigationUC.RequestBottomNavigationStateUseCase(ds)

    @Provides
    fun providesObserveBottomNavigationChanges(ds: BottomNavigationDataSource): BottomNavigationUC.ObserveBottomNavigationChangesUseCase =
        BottomNavigationUC.ObserveBottomNavigationChangesUseCase(ds)

    @Provides
    fun providesSetTopBarsColorsUseCase(): TopBarsUC = TopBarsUC.SetTopBarsColorUseCase()

}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class MainActivityScope
