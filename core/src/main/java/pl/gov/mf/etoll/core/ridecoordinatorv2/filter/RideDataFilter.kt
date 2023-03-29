package pl.gov.mf.etoll.core.ridecoordinatorv2.filter

import pl.gov.mf.etoll.networking.api.model.EventStreamLocation

interface RideDataFilter {

    /**
     * Validate if data is passing requirements
     */
    fun isDataValid(data: EventStreamLocation): Boolean

}