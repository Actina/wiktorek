package pl.gov.mf.etoll.app.di

import dagger.Component
import pl.gov.mf.etoll.app.NkspoApplicationImpl
import pl.gov.mf.etoll.commons.CommonsModule
import pl.gov.mf.etoll.core.CoreModule
import pl.gov.mf.etoll.core.app.di.ApplicationComponent
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorV3
import pl.gov.mf.etoll.front.MainActivityComponent
import pl.gov.mf.etoll.front.MainActivityModule
import pl.gov.mf.etoll.front.watchdog.DemoWatchdog
import pl.gov.mf.etoll.initialization.LoadableSystemsLoader
import pl.gov.mf.etoll.networking.NetworkingModule
import pl.gov.mf.etoll.overlay.OverlayService
import pl.gov.mf.etoll.security.SecurityModule
import pl.gov.mf.etoll.storage.StorageModule
import pl.gov.mf.etoll.ui.components.UIComponentsModule

@ApplicationScope
@Component(
    modules = [ApplicationModule::class, CommonsModule::class, SecurityModule::class,
        StorageModule::class, NetworkingModule::class, CoreModule::class, UIComponentsModule::class]
)
interface ApplicationComponentImpl : ApplicationComponent {

    fun plus(module: MainActivityModule): MainActivityComponent

    fun inject(target: NkspoApplicationImpl)

    fun demoWatchdog(): DemoWatchdog

    fun rideCoordinator(): RideCoordinatorV3

    fun loader(): LoadableSystemsLoader

    fun inject(overlayService: OverlayService)
}