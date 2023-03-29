package pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.testhelpers

import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.data.LocationData

class LocationBuffer(private val size: Int = 10) {
    private val buffer: MutableList<LocationData> = mutableListOf()

    fun add(location: LocationData) {
        if (buffer.size < size)
            buffer.add(location)
        else {
            buffer.removeAt(0)
            buffer.add(location)
        }
    }

    fun get(): List<LocationData> = buffer
}