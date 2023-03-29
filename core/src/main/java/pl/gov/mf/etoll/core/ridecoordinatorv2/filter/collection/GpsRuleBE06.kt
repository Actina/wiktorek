package pl.gov.mf.etoll.core.ridecoordinatorv2.filter.collection

import pl.gov.mf.etoll.core.ridecoordinatorv2.filter.RideDataFilter
import pl.gov.mf.etoll.networking.api.model.EventStreamLocation
import javax.inject.Inject

class GpsRuleBE06 @Inject constructor() : RideDataFilter {
    override fun isDataValid(data: EventStreamLocation): Boolean = data.longitude <= 24.15
}