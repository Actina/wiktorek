package pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.processors

import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.data.LocationData

interface LocationDataProcessor {
    fun newProbeSet()
    fun processData(input: LocationData): LocationData?
}