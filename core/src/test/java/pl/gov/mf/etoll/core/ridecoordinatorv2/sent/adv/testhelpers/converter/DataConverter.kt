package pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.testhelpers.converter

import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.data.LocationData

interface DataConverter {
    fun convertInput(input: String): LocationData?
}