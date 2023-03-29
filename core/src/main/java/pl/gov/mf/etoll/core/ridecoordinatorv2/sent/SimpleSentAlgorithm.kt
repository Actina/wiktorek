package pl.gov.mf.etoll.core.ridecoordinatorv2.sent

import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorV3
import pl.gov.mf.mobile.utils.LocationWrapper
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

/**
 * Simple implementation that emits new location after dynamic time
 */
class SimpleSentAlgorithm @Inject constructor() : SentAlgorithm {

    private val counter = AtomicInteger(0)

    override fun onNextSecond(location: LocationWrapper?): Boolean {
        if (counter.incrementAndGet() >= RideCoordinatorV3.SECONDS_BETWEEN_COLLECTING_SENT && location != null) {
            counter.set(0)
            return true
        }
        return false
    }

    override fun resetAlgorithm() {
        counter.set(5)
    }
}