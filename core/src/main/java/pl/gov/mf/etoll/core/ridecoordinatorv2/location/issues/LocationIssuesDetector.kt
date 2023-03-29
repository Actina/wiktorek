package pl.gov.mf.etoll.core.ridecoordinatorv2.location.issues

import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorConfiguration
import pl.gov.mf.mobile.utils.LocationWrapper

interface LocationIssuesDetector {
    fun start()
    fun stop()
    fun intervalCheck(
        callback: LocationIssuesDetectorCallback,
        appInForeground: Boolean,
        configuration: RideCoordinatorConfiguration?,
    )

    fun onNextLocation(location: LocationWrapper?)
    fun onIssueDealt()

    companion object {
        const val ISSUE_LOW_BATTERY = 1
        const val ISSUE_CRITICAL_BATTERY = 2
        const val ISSUE_CRITICAL_BATTERY_LOADING = 3
        const val ISSUE_OVERHEAT = 4
        const val ISSUE_BACKGROUND = 5

    }
}

interface LocationIssuesDetectorCallback {
    fun onIssueDetected(issues: List<Int>)
}