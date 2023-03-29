package pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.testhelpers.converter

import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.data.LocationData

class NkspoConverter : DataConverter {
    override fun convertInput(input: String): LocationData? {
        val fields = input.split(",")
        try {
            val id = fields[1].toLong()
            val latitude = fields[3].toDouble()
            val longitude = fields[4].toDouble()
            val altitude = fields[5].toDouble()
            val speed = fields[6].toDouble()
            val accuracy = fields[7].toDouble()
            val heading = fields[8].toDouble()
            val timestamp = fields[21].toLong() / 1000L
            return LocationData(
                id,
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