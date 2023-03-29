package pl.gov.mf.etoll.core.ridecoordinatorv2.normalizer.collection

import pl.gov.mf.etoll.core.ridecoordinatorv2.normalizer.RideDataNormalizer
import pl.gov.mf.etoll.networking.api.model.EventStreamLocation
import pl.gov.mf.etoll.networking.api.model.EventStreamLocationWithoutLocation
import javax.inject.Inject

class RideDataNormalizerLongitude @Inject constructor() : RideDataNormalizer {

    override fun formatData(data: EventStreamLocation): EventStreamLocation {
        if (data.longitude > 180.0)
            data.longitude = 180.0
        if (data.longitude < -180.0)
            data.longitude = -180.0
        return data
        // TODO: format somehow amount of number after . ?
    }

    override fun formatData(data: EventStreamLocationWithoutLocation): EventStreamLocationWithoutLocation {
        return data
    }
}