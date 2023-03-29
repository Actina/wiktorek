package pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.testhelpers.converter

import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.data.LocationData

class GpsConverter : DataConverter {
    override fun convertInput(input: String): LocationData? {
        val fields = input.split(",")
        try {
            val latitude = fields[1].toDouble()
            val longitude = fields[2].toDouble()
            val altitude = fields[3].toDouble()
            val speed = fields[4].toDouble()
            val accuracy = fields[6].toDouble()
            val heading = fields[5].toDouble()
            val timestamp = fields[7].toLong()
            return LocationData(
                0,
                latitude,
                longitude,
                altitude,
                heading,
                speed,
                accuracy,
                timestamp
            )
        } catch (ex: Exception) {
            return null
        }
    }
}