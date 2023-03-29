package pl.gov.mf.etoll.core.watchdog

import android.app.Activity
import android.content.Intent
import com.google.firebase.messaging.RemoteMessage
import pl.gov.mf.etoll.core.criticalmessages.CriticalMessagesObserver
import pl.gov.mf.etoll.core.watchdog.ridefinish.RideFinishCallbacks
import pl.gov.mf.etoll.initialization.LoadableSystem

interface CoreWatchdog : LoadableSystem {

    fun onAppGoesToBackground(
        activity: Activity,
        overlayServiceStarter: Intent
    )

    fun onAppGoesToForeground(
        activity: Activity,
        criticalMessagesObserver: CriticalMessagesObserver?,
        rideFinishCallbacks: RideFinishCallbacks?,
        stopForegroundServiceIntent: Intent
    )

    fun onAwakePushReceived(msg: RemoteMessage)
    fun onCheckConditionsRequested()
}
