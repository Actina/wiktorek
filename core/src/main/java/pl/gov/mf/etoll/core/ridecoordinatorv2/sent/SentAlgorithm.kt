package pl.gov.mf.etoll.core.ridecoordinatorv2.sent

import pl.gov.mf.mobile.utils.LocationWrapper

interface SentAlgorithm {
    /**
     * Inform algorithm about next second that passed with last known location
     * @return true if new location event should be propagated
     */
    fun onNextSecond(location: LocationWrapper?): Boolean

    /**
     * Reset algorithm - it should be called whenever new ride is started or old one is resumed
     */
    fun resetAlgorithm()
}