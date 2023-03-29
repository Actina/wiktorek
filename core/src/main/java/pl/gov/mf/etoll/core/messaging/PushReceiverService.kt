package pl.gov.mf.etoll.core.messaging

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.reactivex.Observable
import pl.gov.mf.etoll.core.app.NkspoApplication
import pl.gov.mf.etoll.storage.settings.Settings
import java.util.concurrent.TimeUnit

class PushReceiverService : FirebaseMessagingService() {

    override fun onMessageReceived(msg: RemoteMessage) {
        super.onMessageReceived(msg)
        // we do not care about message details, we just send info to watchdog
        (applicationContext as NkspoApplication).getApplicationComponent().coreWatchdog()
            .onAwakePushReceived(msg)
    }

    @SuppressLint("CheckResult")
    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.i("WATCHDOG_AWAKE", "PUSH ID: $p0")
        Observable.interval(10, 1, TimeUnit.SECONDS).take(1).subscribe {
            (applicationContext as NkspoApplication).getApplicationComponent()
                .useCaseWriteSettings()
                .execute(Settings.FIREBASE_ID, p0).subscribe()
        }
    }
}