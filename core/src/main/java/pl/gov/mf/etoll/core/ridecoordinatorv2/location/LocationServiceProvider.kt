package pl.gov.mf.etoll.core.ridecoordinatorv2.location

import pl.gov.mf.mobile.utils.LocationWrapper

interface LocationServiceProvider {
    fun start()
    fun stop()
    fun getLastKnownLocation(forTolled: Boolean): LocationWrapper?
    fun consumeLastKnownLocation(forTolled: Boolean, location: LocationWrapper?)
    fun isInOnlyGpsMode(): Boolean
    fun getLastLocationReceiveTimestamp(): Long
    fun getLastKnownSavedLocation(): LocationWrapper?
    fun getStartLocation(): Pair<Double, Double>?
    fun setFlagNextLocationWillBeStartLocation()
}