package pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.processors

import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.data.LocationData
import pl.gov.mf.mobile.utils.LocationUtils
import javax.inject.Inject
import kotlin.math.absoluteValue
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class SentGeoAlgorithmProcessor @Inject constructor() : LocationDataProcessor {

    companion object {
        // slowest speed treated as movement
        private const val MIN_SPEED_10kmph_inMps = 2.8001
    }

    private var lastUsedLocationData: LocationData? = null
    private var lastAnalyzedLocationData: LocationData? = null
    private var timeDiff = 0L
    private var distanceDiff = 0.0
    private var lastLocalBearingDiff = 0.0
    private var lastLongBearingDiff = 0.0
    private var isStanding = false

    override fun newProbeSet() {
        // reset state
        lastUsedLocationData = null
        lastAnalyzedLocationData = null
        timeDiff = 0L
        distanceDiff = 0.0
    }

    override fun processData(input: LocationData): LocationData? {
        // zero state
        if (lastUsedLocationData == null) {
            lastUsedLocationData = input
            lastAnalyzedLocationData = lastUsedLocationData
            lastLocalBearingDiff = lastUsedLocationData!!.bearing
            lastLongBearingDiff = lastUsedLocationData!!.bearing
            return lastUsedLocationData
        }
        // check condition about time
        val timestampDiff = input.timestampInMs - lastAnalyzedLocationData!!.timestampInMs
        // warning, this line could require adjustments if format of timestamp would be different! here it is in nanos
        val probeTimeDiffInMs = timestampDiff

        timeDiff += probeTimeDiffInMs
        val probeLocationDiff = LocationUtils.distanceBetween(
            lastAnalyzedLocationData!!.lat,
            input.lat,
            lastAnalyzedLocationData!!.lon,
            input.lon,
            lastAnalyzedLocationData!!.altitude,
            input.altitude
        )
        distanceDiff += probeLocationDiff

        // calculate bearing difference
        val probeBearing = bearingBetweenPoints(input, lastAnalyzedLocationData!!)
        val longTermProbeBearing = bearingBetweenPoints(input, lastUsedLocationData!!)
        // check if we're moving or standing, based on location change in time
        val calculatedSpeed = probeLocationDiff / (probeTimeDiffInMs / 1000.0) // speed in m/s


//        println("ID: ${input.id} TimestampDiff: $timestampDiff Distance: $distanceDiff speed: $calculatedSpeed probeBearing: $probeBearing longProbeBearing: $longTermProbeBearing")
        // this should be done after all regarding last location was taken
        lastAnalyzedLocationData = input

        // checking conditions
        if (calculatedSpeed < MIN_SPEED_10kmph_inMps) {
            // we're standing
            if (isStanding) {
                // check timer
                if (timeDiff >= 60 * 5 * 1000) {
                    useAndResetData(input, probeBearing)
                    input.source = "case_standing_time5min"
                    return lastUsedLocationData
                }
            }
            // we were not standing before
            // should we send "standing" event now?
            isStanding = true
        } else {
            // we're moving

            // if we were standing before, send event and info
            if (isStanding) {
                // we just started movement, send event
                isStanding = false
                // TODO: should we sent events when restarting movement ? - for now on, I'll disable this
//                useAndResetData(input, probeBearing)
//                input.source = "started_movement_after_standing"
//                return lastUsedLocationData
            }
            // passed 5 mins, or (1km and 1min) or 40'
            if (timeDiff >= 60 * 5 * 1000) {
                useAndResetData(input, probeBearing)
                input.source = "case_time_5min"
                return lastUsedLocationData
            } else if (timeDiff >= 60 * 1000 && distanceDiff >= 1000.0) {
                useAndResetData(input, probeBearing)
                input.source = "case_time1min_or_1km"
                return lastUsedLocationData
            } else if ((lastLocalBearingDiff - probeBearing).absoluteValue >= 40.0) {
                input.source =
                    "local_case_bearing ${(lastLocalBearingDiff - probeBearing).absoluteValue}"
                useAndResetData(input, probeBearing)
                return lastUsedLocationData
            } else
                if ((lastLongBearingDiff - longTermProbeBearing).absoluteValue >= 40.0) {
                    input.source =
                        "long_case_bearing ${(lastLongBearingDiff - longTermProbeBearing).absoluteValue}"
                    useAndResetData(input, probeBearing)
                    return lastUsedLocationData
                }
        }

        return null
    }

    private fun useAndResetData(input: LocationData, probeBearing: Double) {
        lastUsedLocationData = input
        distanceDiff = 0.0
        timeDiff = 0
        lastLocalBearingDiff = probeBearing
        lastLongBearingDiff = probeBearing
    }

    private fun bearingBetweenPoints(
        newLocation: LocationData,
        lastLocation: LocationData
    ): Double {
        // http://stackoverflow.com/questions/3925942/cllocation-category-for-calculating-bearing-w-haversine-function
        val lat1 = Math.PI * newLocation.lat / 180.0
        val long1 = Math.PI * newLocation.lon / 180.0
        val lat2 = Math.PI * lastLocation.lat / 180.0
        val long2 = Math.PI * lastLocation.lon / 180.0
        // Formula: θ = atan2( sin Δλ ⋅ cos φ2 , cos φ1 ⋅ sin φ2 − sin φ1 ⋅ cos φ2 ⋅ cos Δλ )
        // Source: http://www.movable-type.co.uk/scripts/latlong.html
        val rads = atan2(
            sin(long2 - long1) * cos(lat2),
            cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(long2 - long1)
        )
        var degrees = rads * 180 / Math.PI
        while (degrees >= 360.0) degrees -= 360.0
        while (degrees < 0.00000000001) degrees += 360.0
        return degrees
    }
}


