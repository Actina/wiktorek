package pl.gov.mf.etoll.app.timeshift

import android.util.Log
import org.joda.time.DateTime
import pl.gov.mf.etoll.core.ridecoordinatorv2.events.EventHelper
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorV3
import pl.gov.mf.etoll.core.watchdog.counter.TimeShiftDetectorCounter
import javax.inject.Inject

class TimeShiftAdapterImpl @Inject constructor(
    private val timeShiftDetectorCounter: TimeShiftDetectorCounter,
    private val rideCoordinatorV3: RideCoordinatorV3,
    private val eventHelper: EventHelper
) : TimeShiftAdapter {
    override fun onAppStartedAndLoaded() {
        // generate event
        // we want event just for first run of app
        if (timeShiftDetectorCounter.isLocked() && rideCoordinatorV3.getConfiguration()?.duringRide == true) {
            Log.d("NKSPO_APP", "Resume activated, generating event! ${DateTime.now()}")
            val time = timeShiftDetectorCounter.getTimestampForLastEvent()
            // edge case for application update while during ride
            if (time > 0)
                eventHelper.generateResumeApplicationEvent(time, true)
            // and now reset counter
            timeShiftDetectorCounter.resetCounter()
            // and unlock it
            timeShiftDetectorCounter.unlockCounter()
        } else {
            Log.d("NKSPO_APP", "No resume available, normal start! ${DateTime.now()}")
        }
    }
}