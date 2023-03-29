package pl.gov.mf.etoll.core.watchdog.timeissues

import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorV3
import pl.gov.mf.mobile.utils.CallbacksContainer
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

class TimeIssuesControllerImpl @Inject constructor(private val rideCoordinator: RideCoordinatorV3) :
    TimeIssuesController {

    private val callbacksContainer: CallbacksContainer<TimeIssuesCallbacks> = CallbacksContainer()
    private val issueDetected = AtomicBoolean(false)
    private val timeSinceLastShowing =
        AtomicLong(TimeIssuesController.MIN_TIME_IN_SECS_BETWEEN_MESSAGES)

    override fun onTimeIssueDetected(forceShow: Boolean) {
        // this one is used now mainly with forceShow communicate, all rest is done by interceptor callbacks methods
        issueDetected.set(true)
        if (forceShow)
            timeSinceLastShowing.set(TimeIssuesController.MIN_TIME_IN_SECS_BETWEEN_MESSAGES)
    }

    override fun onTimeIssueScreenShown() {
        timeSinceLastShowing.set(0)
        issueDetected.set(false)
    }

    override fun setCallbacks(callbacks: TimeIssuesCallbacks, tag: String) {
        callbacksContainer.set(callbacks, tag)
    }

    override fun removeCallbacks(tag: String) {
        callbacksContainer.set(null, tag)
    }

    override fun intervalCheck() {
        val timeSinceLastMessage = timeSinceLastShowing.incrementAndGet()
        if (issueDetected.get() && (timeSinceLastMessage >= TimeIssuesController.MIN_TIME_IN_SECS_BETWEEN_MESSAGES || rideCoordinator.getConfiguration()?.duringRide == true)) {
            val duringRide = rideCoordinator.getConfiguration()?.duringRide == true
            callbacksContainer.get().forEach { it.value.showTimeIssueScreen() }
            // now stop ride
            if (duringRide) {
                // finish ride
                rideCoordinator.stopRide().subscribe()
            }
        }
    }

    override fun onTimeIssueIntercepted() {
        issueDetected.set(true)
    }

    override fun onNoTimeIssueIntercepted() {
        issueDetected.set(false)
    }
}