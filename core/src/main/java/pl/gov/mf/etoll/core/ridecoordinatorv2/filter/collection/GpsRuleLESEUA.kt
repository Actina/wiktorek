package pl.gov.mf.etoll.core.ridecoordinatorv2.filter.collection

import pl.gov.mf.etoll.core.ridecoordinatorv2.filter.RideDataFilter
import pl.gov.mf.etoll.networking.api.model.EventStreamLocation
import javax.inject.Inject

class GpsRuleLESEUA @Inject constructor() : RideDataFilter {
    override fun isDataValid(data: EventStreamLocation): Boolean =
        1.25 * data.longitude + 20.375 - data.latitude <= 0.0
}