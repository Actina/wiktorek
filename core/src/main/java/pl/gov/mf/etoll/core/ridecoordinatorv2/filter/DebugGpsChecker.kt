package pl.gov.mf.etoll.core.ridecoordinatorv2.filter

import pl.gov.mf.etoll.networking.api.model.EventStreamLocation
import pl.gov.mf.mobile.utils.LocationWrapper
import javax.inject.Inject

class DebugGpsChecker @Inject constructor(private val decorator: RideDataFilterDecorator) {
    fun isLocationValid(location: LocationWrapper): Boolean =
        decorator.isDataValid(location.toEventStreamSimpleLocation())
}

private fun LocationWrapper.toEventStreamSimpleLocation(): EventStreamLocation = EventStreamLocation(
    "",
    0,
    "",
    "",
    latitude,
    longitude,
    altitude,
    time,
    speed.toDouble(),
    accuracy.toDouble(),
    bearing.toDouble(),
    "",
    "",
    "",
    "",
    "",
    true,
    true,
    true,
    100,
    null
)
