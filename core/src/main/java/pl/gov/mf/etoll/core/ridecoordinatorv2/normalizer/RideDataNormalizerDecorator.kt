package pl.gov.mf.etoll.core.ridecoordinatorv2.normalizer

import pl.gov.mf.etoll.core.ridecoordinatorv2.normalizer.collection.RideDataNormalizerLatitude
import pl.gov.mf.etoll.core.ridecoordinatorv2.normalizer.collection.RideDataNormalizerLongitude
import pl.gov.mf.etoll.core.ridecoordinatorv2.normalizer.collection.RideDataNormalizerMobileData
import pl.gov.mf.etoll.networking.api.model.EventStreamLocation
import pl.gov.mf.etoll.networking.api.model.EventStreamLocationWithoutLocation
import javax.inject.Inject

class RideDataNormalizerDecorator @Inject constructor(
    longitude: RideDataNormalizerLongitude,
    latitude: RideDataNormalizerLatitude,
    mobile: RideDataNormalizerMobileData
) : RideDataNormalizer {

    private val normalizers = mutableListOf<RideDataNormalizer>()

    init {
        chain(longitude)
        chain(latitude)
        chain(mobile)
    }

    /**
     * Add next normalizer to chain
     */
    private fun chain(normalizer: RideDataNormalizer): RideDataNormalizerDecorator = apply {
        normalizers.add(normalizer)
    }

    override fun formatData(data: EventStreamLocation): EventStreamLocation {
        normalizers.forEach { normalizer ->
            normalizer.formatData(data)
        }
        return data
    }

    override fun formatData(data: EventStreamLocationWithoutLocation): EventStreamLocationWithoutLocation {
        normalizers.last().formatData(data)
        return data
    }
}