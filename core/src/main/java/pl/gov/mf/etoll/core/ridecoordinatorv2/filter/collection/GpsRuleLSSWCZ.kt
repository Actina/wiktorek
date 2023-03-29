package pl.gov.mf.etoll.core.ridecoordinatorv2.filter.collection

import pl.gov.mf.etoll.core.ridecoordinatorv2.filter.RideDataFilter
import pl.gov.mf.etoll.networking.api.model.EventStreamLocation
import javax.inject.Inject

class GpsRuleLSSWCZ @Inject constructor() : RideDataFilter {
    override fun isDataValid(data: EventStreamLocation): Boolean =
        54.9 - data.latitude - 0.3 * data.longitude <= 0
}