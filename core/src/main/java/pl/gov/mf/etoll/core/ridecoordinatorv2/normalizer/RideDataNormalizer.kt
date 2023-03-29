package pl.gov.mf.etoll.core.ridecoordinatorv2.normalizer

import pl.gov.mf.etoll.networking.api.model.EventStreamLocation
import pl.gov.mf.etoll.networking.api.model.EventStreamLocationWithoutLocation

interface RideDataNormalizer {
    /**
     * Format input to pass requirements; Return formatted data
     */
    fun formatData(data: EventStreamLocation): EventStreamLocation
    fun formatData(data: EventStreamLocationWithoutLocation): EventStreamLocationWithoutLocation
}