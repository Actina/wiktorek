package pl.gov.mf.etoll.core.watchdog.status

import android.content.Intent
import io.reactivex.Observable
import pl.gov.mf.etoll.core.watchdog.CoreWatchdogData

interface CoreWatchdogStatusUpdateController {
    fun initialize()

    fun observeUpdates(): Observable<CoreWatchdogData>

    fun onUpdateStatusRemoteRequest()
    fun updateNow(appInForeground: AppInForegroundInfoProvider)
    fun shouldForceUpdate(): Boolean
    fun checkIfUpdateShouldBeDoneNow(intent: Intent?)

    interface AppInForegroundInfoProvider {
        fun appIsInForeground(): Boolean
    }
}