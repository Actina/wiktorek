package pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.processors

import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.data.LocationData
import javax.inject.Inject

class FlowControlProcessor @Inject constructor() : LocationDataProcessor {

    companion object {
        private const val takeEvery: Int = 3 // ustaw fizyczny check na 2.5s timestamp diff
    }

    private var counter = 0
    override fun newProbeSet() {
        counter = 0
    }

    override fun processData(input: LocationData): LocationData? {
        counter++
        if (counter >= takeEvery) {
            counter = 0
            return input
        }
        return null
    }
}