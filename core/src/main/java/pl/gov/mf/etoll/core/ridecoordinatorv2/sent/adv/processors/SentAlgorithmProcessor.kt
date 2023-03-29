package pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.processors

import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.SentAdvancedAlgorithm
import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.data.LocationData
import pl.gov.mf.mobile.utils.LocationUtils
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class SentAlgorithmProcessor @Inject constructor() : LocationDataProcessor {

    private var lastUsedLocation: LocationData? = null
    private var lastAnalyzedLocation: LocationData? = null
    private var timeDiff = 0L
    private var distanceDiff = 0.0
    private var bearingDiff = 0.0
    private var lastBearing = 0.0
    private var isStanding = false

    override fun newProbeSet() {
        // reset state
        lastUsedLocation = null
        lastAnalyzedLocation = null
        timeDiff = 0L
        distanceDiff = 0.0
        lastBearing = 0.0
        // we leave this class for quick switching for future tests, TODO: should be removed around 09.2021
        throw RuntimeException("Should not be used! Use SENTGEO version instead")
    }

    override fun processData(input: LocationData): LocationData? {
        // zero state
        if (lastUsedLocation == null) {
            lastUsedLocation = input
            lastAnalyzedLocation = lastUsedLocation
            lastBearing = lastUsedLocation!!.bearing
            return lastUsedLocation
        }
        // check condition about time
        val timestampDiff = input.timestampInMs - lastAnalyzedLocation!!.timestampInMs
        // warning, this line could require adjustments if format of timestamp would be different! here it is in nanos
        val probeTimeDiffInMs = timestampDiff

        timeDiff += probeTimeDiffInMs
        val probeLocationDiff = LocationUtils.distanceBetween(
            lastAnalyzedLocation!!.lat,
            input.lat,
            lastAnalyzedLocation!!.lon,
            input.lon,
            lastAnalyzedLocation!!.altitude,
            input.altitude
        )
        distanceDiff += probeLocationDiff.toFloat()

        // calculate bearing difference
        val probeBearing = bearingBetweenPoints(input, lastAnalyzedLocation!!)
        bearingDiff += if (SentAdvancedAlgorithm.selectedAbsBearingSumMethod)
            LocationUtils.bearingDifferenceAbs(lastBearing.toDouble(), probeBearing)
                .toFloat()
        else LocationUtils.bearingDifferenceClassic(
            lastBearing.toDouble(),
            probeBearing
        ).toFloat()

        lastBearing = probeBearing

        // check if we're moving or standing, based on location change in time
        val calculatedSpeed = probeLocationDiff / (probeTimeDiffInMs / 1000.0) // speed in m/s

        // this should be done after all regarding last location was taken
        lastAnalyzedLocation = input

        // checking conditions
        if (calculatedSpeed < SentAdvancedAlgorithm.minimumSpeedForMovementInMperS) {
            // we're standing
            if (isStanding) {
                // check timer
                if (timeDiff >= 60 * SentAdvancedAlgorithm.maxWaitingTimeBetweenEventsInMinutes.get() * 1000) {
                    lastUsedLocation = input
                    distanceDiff = 0.0
                    timeDiff = 0
                    bearingDiff = 0.0
                    return lastUsedLocation
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
                lastUsedLocation = input
                distanceDiff = 0.0
                timeDiff = 0
                bearingDiff = 0.0
                return lastUsedLocation
            }
            // passed 5 mins, or (1km and 1min) or 40'
            if (timeDiff >= 60 * SentAdvancedAlgorithm.maxWaitingTimeBetweenEventsInMinutes.get() * 1000) {
                lastUsedLocation = input
                distanceDiff = 0.0
                timeDiff = 0
                bearingDiff = 0.0
                return lastUsedLocation
            } else if (timeDiff >= SentAdvancedAlgorithm.minimumTimeForEventsInSeconds.get() * 1000
                && distanceDiff >= SentAdvancedAlgorithm.minimumDistanceForEventInMeters.toDouble()
            ) {
                lastUsedLocation = input
                distanceDiff = 0.0
                timeDiff = 0
                bearingDiff = 0.0
                return lastUsedLocation
            } else if (bearingDiff >= SentAdvancedAlgorithm.minimumBearingChangeInDegrees.toDouble()
                || bearingDiff <= -SentAdvancedAlgorithm.minimumBearingChangeInDegrees.toDouble()
            ) {
                lastUsedLocation = input
                distanceDiff = 0.0
                timeDiff = 0
                bearingDiff = 0.0
                return lastUsedLocation
            }
        }

        return null
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