package pl.gov.mf.etoll.core.ridecoordinatorv2.location.startloc

import pl.gov.mf.mobile.utils.LocationWrapper

interface StartLocationContainer {
    fun setLocation(location: LocationWrapper?)
    fun getLocation(): Pair<Double, Double>?
}