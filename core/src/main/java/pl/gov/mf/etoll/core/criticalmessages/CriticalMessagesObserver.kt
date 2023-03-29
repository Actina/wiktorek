package pl.gov.mf.etoll.core.criticalmessages

import pl.gov.mf.etoll.interfaces.CriticalMessageState

interface CriticalMessagesObserver {
    fun showCriticalMessageDialog(criticalMessageType: CriticalMessageState): Boolean
    fun blockAppNoGpsPermissions()
    fun blockAppNoGpsAccessible()
    fun showFakeGpsDetectedDialog(): Boolean
    fun blockAppAirplaneModeIsOn()
    fun showGpsIssuesDialog(issues: List<Int>): Boolean
    fun showTimeIssues(duringRide: Boolean)
    fun showInstanceIssuesError(): Boolean
    fun blockAppVersionTooOld()
}