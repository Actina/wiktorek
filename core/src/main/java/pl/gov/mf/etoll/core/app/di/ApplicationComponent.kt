package pl.gov.mf.etoll.core.app.di

import pl.gov.mf.etoll.commons.CommonsComponent
import pl.gov.mf.etoll.core.CoreComponent
import pl.gov.mf.etoll.core.notifications.NotificationManager
import pl.gov.mf.etoll.core.notifications.NotificationManagerUC.CreateNotificationsChannelsUseCase
import pl.gov.mf.etoll.core.overlay.controller.OverlayController
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorUC
import pl.gov.mf.etoll.core.watchdog.CoreWatchdog
import pl.gov.mf.etoll.networking.NetworkingComponent
import pl.gov.mf.etoll.security.SecurityComponent
import pl.gov.mf.etoll.storage.StorageComponent

interface ApplicationComponent : CoreComponent,
    CommonsComponent,
    StorageComponent,
    SecurityComponent,
    NetworkingComponent {

    fun useCaseCreateNotificationChannels(): CreateNotificationsChannelsUseCase

    fun coreWatchdog(): CoreWatchdog

    fun notificationManager(): NotificationManager

    fun overlayController(): OverlayController

    fun useCaseGetRideCoordinatorConfiguration(): RideCoordinatorUC.GetRideCoordinatorConfigurationUseCase
}