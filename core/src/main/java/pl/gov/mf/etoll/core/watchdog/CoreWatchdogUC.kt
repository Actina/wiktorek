package pl.gov.mf.etoll.core.watchdog

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import pl.gov.mf.etoll.core.criticalmessages.CriticalMessagesObserver
import pl.gov.mf.etoll.core.watchdog.ridefinish.RideFinishCallbacks
import pl.gov.mf.etoll.core.watchdog.status.CoreWatchdogStatusUpdateController

sealed class CoreWatchdogUC {

    class AppGoesToBackgroundUseCaseWatchdog(private val ds: CoreWatchdog) :
        CoreWatchdogUC() {
        fun execute(
            activity: AppCompatActivity,
            overlayServiceStarter: Intent
        ) {
            ds.onAppGoesToBackground(activity, overlayServiceStarter)
        }
    }

    class AppGoesToForegroundUseCase(private val ds: CoreWatchdog) :
        CoreWatchdogUC() {
        fun execute(
            activity: AppCompatActivity,
            criticalMessagesObserver: CriticalMessagesObserver?,
            rideFinishCallbacks: RideFinishCallbacks,
            stopForegroundServiceIntent: Intent
        ) {
            ds.onAppGoesToForeground(
                activity,
                criticalMessagesObserver,
                rideFinishCallbacks,
                stopForegroundServiceIntent
            )
        }
    }

    class ObserveStatusUpdatesUseCase(private val ds: CoreWatchdogStatusUpdateController) :
        CoreWatchdogUC() {
        fun execute(): Observable<CoreWatchdogData> =
            ds.observeUpdates().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    class ChangeCoreObservationModeUseCase(private val ds: CoreWatchdog) : CoreWatchdogUC() {
        fun executeForDefault() = ds.onCheckConditionsRequested()
    }

}