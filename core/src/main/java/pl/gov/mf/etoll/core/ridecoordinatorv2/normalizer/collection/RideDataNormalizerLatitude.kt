package pl.gov.mf.etoll.core.ridecoordinatorv2.normalizer.collection

import pl.gov.mf.etoll.core.ridecoordinatorv2.normalizer.RideDataNormalizer
import pl.gov.mf.etoll.networking.api.model.EventStreamLocation
import pl.gov.mf.etoll.networking.api.model.EventStreamLocationWithoutLocation
import javax.inject.Inject

class RideDataNormalizerLatitude @Inject constructor() : RideDataNormalizer {

    override fun formatData(data: EventStreamLocation): EventStreamLocation {
        if (data.latitude > 90.0)
            data.latitude = 90.0
        if (data.latitude < -90.0)
            data.latitude = -90.0
        return data
        // TODO: format somehow amount of number after . ?
    }

    override fun formatData(data: EventStreamLocationWithoutLocation): EventStreamLocationWithoutLocation {
        return data
    }
}