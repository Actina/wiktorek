package pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.data

import pl.gov.mf.mobile.utils.JsonConvertible

data class LocationData(
    var id: Long,
    var lat: Double,
    var lon: Double,
    var altitude: Double,
    var bearing: Double,
    var speed: Double,
    val accuracy: Double,
    val timestampInMs: Long,
    var source: String = "-"
) : JsonConvertible