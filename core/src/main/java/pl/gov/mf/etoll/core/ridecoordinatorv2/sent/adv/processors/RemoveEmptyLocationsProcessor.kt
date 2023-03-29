package pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.processors

import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.data.LocationData
import javax.inject.Inject

/**
 * This processor removes all locations with empty latitude or longitude
 */
class RemoveEmptyLocationsProcessor @Inject constructor() : LocationDataProcessor {

    override fun newProbeSet() {
        // do nothing, as we're not preserving any state
    }

    override fun processData(input: LocationData): LocationData? {
        if (input.lat < 0.0001 && input.lat > -0.0001) return null
        if (input.lon < 0.0001 && input.lon > -0.0001) return null
        return input
    }
}