package pl.gov.mf.etoll.starter

import pl.gov.mf.etoll.core.notifications.NotificationManagerUC.CreateNotificationsChannelsUseCase
import pl.gov.mf.etoll.core.ridehistory.collector.RideHistoryCollector
import pl.gov.mf.etoll.maps.MapsRendererConfigurator
import javax.inject.Inject

class AppStarterImpl @Inject constructor(
    private val rideHistoryCollector: RideHistoryCollector,
    private val createNotificationsChannelsUseCase: CreateNotificationsChannelsUseCase,
    private val rendererConfigurator: MapsRendererConfigurator,
) : AppStarter {
    override fun start() {
        createNotificationsChannelsUseCase.execute()
        rideHistoryCollector.start()
        rendererConfigurator.initializeRenderer()
    }
}

interface AppStarter {
    fun start()
}