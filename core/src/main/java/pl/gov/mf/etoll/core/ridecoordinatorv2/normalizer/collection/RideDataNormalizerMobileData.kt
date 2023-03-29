package pl.gov.mf.etoll.core.ridecoordinatorv2.normalizer.collection

import pl.gov.mf.etoll.core.ridecoordinatorv2.normalizer.RideDataNormalizer
import pl.gov.mf.etoll.networking.api.model.EventStreamLocation
import pl.gov.mf.etoll.networking.api.model.EventStreamLocationWithoutLocation
import javax.inject.Inject

class RideDataNormalizerMobileData @Inject constructor() : RideDataNormalizer {
    override fun formatData(data: EventStreamLocation): EventStreamLocation = data.apply {
        mcc.let {
            if (it.isEmpty() || !it.matches(Regex("^[0-9]{3}$"))) {
                mcc = "000"
            }
        }
        mnc.let {
            if (it.isEmpty() || !it.matches(Regex("^[0-9]{2,3}$")))
                mnc = "00"
        }

        mobileCellId.let {
            if (it.isEmpty() || !it.matches(Regex("^[A-Fa-f0-9]{9}$")))
                mobileCellId = "0"
        }

        lac.let {
            try {
                if (it.isEmpty() || it.toInt() == 0)
                    lac = "0"
            } catch (ex: Exception) {
                lac = "0"
            }
        }
    }

    override fun formatData(data: EventStreamLocationWithoutLocation): EventStreamLocationWithoutLocation =
        data.apply {
            mcc.let {
                if (it.isEmpty() || !it.matches(Regex("^[0-9]{3}$"))) {
                    mcc = "000"
                }
            }
            mnc.let {
                if (it.isEmpty() || !it.matches(Regex("^[0-9]{2,3}$")))
                    mnc = "00"
            }

            mobileCellId.let {
                if (it.isEmpty() || !it.matches(Regex("^[A-Fa-f0-9]{9}$")))
                    mobileCellId = "0"
            }

            lac.let {
                try {
                    if (it.isEmpty() || it.toInt() == 0)
                        lac = "0"
                } catch (ex: Exception) {
                    lac = "0"
                }
            }
        }
}