package pl.gov.mf.etoll.core.ridecoordinatorv2.location.issues

import android.content.Context
import android.util.Log
import pl.gov.mf.etoll.core.BuildConfig
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorConfiguration
import pl.gov.mf.mobile.utils.BatteryUtils
import pl.gov.mf.mobile.utils.LocationWrapper
import pl.gov.mf.mobile.utils.OverheatUtils
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class LocationIssuesDetectorImpl @Inject constructor(private val appContext: Context) :
    LocationIssuesDetector {

    companion object {
        private val WARNING_AFTER_SECONDS =
            if (BuildConfig.DEBUG) 65 else 180 // 180s without gps -> warning | CHANGE TO 1s FOR TESTING PURPOSES
        private val MIN_TIME_BETWEEN_WARNINGS =
            if (BuildConfig.DEBUG) 300 else 600 // 600s after showing last warning and still no gps -> warning
    }

    private val secondsWithoutGpsData = AtomicInteger(0)
    private val secondsAfterLastMessage = AtomicInteger(0)
    private var running = false

    override fun start() {
        Log.d("LocIssues", "Starting")
        secondsWithoutGpsData.set(0)
        secondsAfterLastMessage.set(MIN_TIME_BETWEEN_WARNINGS)
        running = true
    }

    override fun stop() {
        Log.d("LocIssues", "Stopping")
        running = false
        secondsWithoutGpsData.set(0)
        secondsAfterLastMessage.set(0)
    }

    override fun intervalCheck(
        callback: LocationIssuesDetectorCallback,
        appInForeground: Boolean,
        configuration: RideCoordinatorConfiguration?,
    ) {
        if (!running || configuration == null || !configuration.duringRide || configuration.monitoringDeviceConfiguration?.monitoringByApp != true) return
        val secondsWithoutGps = secondsWithoutGpsData.incrementAndGet()
        if (secondsAfterLastMessage.incrementAndGet() >= MIN_TIME_BETWEEN_WARNINGS) {
            // show warning
            if (secondsWithoutGps >= WARNING_AFTER_SECONDS) {
                Log.d("LocIssues", "Scheduling issues dialog")
                val output = mutableListOf<Int>()
                // 1st - check if app is in foreground or background
                // for background - we could count time in background in overall
//                if (!appInForeground)
//                    output.add(LocationIssuesDetector.ISSUE_BACKGROUND)
                // 2nd - check battery state, loading state
                val batteryLevel = BatteryUtils.getCurrentBatteryState(context = appContext)
                    ?: BatteryUtils.BATTERY_UNKNOWN
                if (batteryLevel <= 15) {
                    if (BatteryUtils.isCharging(context = appContext))
                        output.add(LocationIssuesDetector.ISSUE_CRITICAL_BATTERY_LOADING)
                    else
                        output.add(LocationIssuesDetector.ISSUE_CRITICAL_BATTERY)
                } else
                    if (batteryLevel < 51) {
                        output.add(LocationIssuesDetector.ISSUE_LOW_BATTERY)
                    }
                // 3rd - check overheat info - min api 29 (Q)
                if (OverheatUtils.getCurrentLevel(context = appContext) != OverheatUtils.OverheatStatus.COLD)
                    output.add(LocationIssuesDetector.ISSUE_OVERHEAT)
                // for testing purposes:
//                output.add(LocationIssuesDetector.ISSUE_LOW_BATTERY)
//                output.add(LocationIssuesDetector.ISSUE_CRITICAL_BATTERY)
//                output.add(LocationIssuesDetector.ISSUE_CRITICAL_BATTERY_LOADING)
//                output.add(LocationIssuesDetector.ISSUE_OVERHEAT)
//                output.add(LocationIssuesDetector.ISSUE_BACKGROUND)
                Log.d("LocIssues", "Issues to show: $output")
                callback.onIssueDetected(output)
            }
        }
    }

    override fun onIssueDealt() {
        secondsWithoutGpsData.set(0)
        secondsAfterLastMessage.set(0)
    }

    override fun onNextLocation(location: LocationWrapper?) {
        secondsWithoutGpsData.set(0)
    }
}