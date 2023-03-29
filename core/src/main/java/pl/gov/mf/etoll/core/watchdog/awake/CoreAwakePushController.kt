package pl.gov.mf.etoll.core.watchdog.awake

import com.google.firebase.messaging.RemoteMessage

interface CoreAwakePushController {
    fun onAwakePushReceived(msg: RemoteMessage, systemsLoaded: Boolean, appInForeground: Boolean)
}