package pl.gov.mf.etoll.core.ridecoordinatorv2.filter.collection

import pl.gov.mf.etoll.core.ridecoordinatorv2.filter.RideDataFilter
import pl.gov.mf.etoll.networking.api.model.EventStreamLocation
import javax.inject.Inject

class GpsRuleSNERU @Inject constructor() : RideDataFilter {
    override fun isDataValid(data: EventStreamLocation): Boolean =
        !(data.longitude > 19 && data.latitude > 54.5)
}