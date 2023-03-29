package pl.gov.mf.etoll.starter

import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
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
        // make sure rx undeliverable exception is caught
        RxJavaPlugins.setErrorHandler { e ->
            if (e is UndeliverableException) {
                // we do not log it now, it's related to our get status policy
            } else {
                // Forward all others to current thread's uncaught exception handler
                Thread.currentThread().also { thread ->
                    thread.uncaughtExceptionHandler?.uncaughtException(thread, e)
                }
            }
        }
    }
}

interface AppStarter {
    fun start()
}