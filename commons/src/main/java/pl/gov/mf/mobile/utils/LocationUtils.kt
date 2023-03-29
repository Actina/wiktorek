package pl.gov.mf.mobile.utils

import kotlin.math.*

object LocationUtils {

    /**
     * Calculate bearing difference - only increasing (!!)
     */
    fun bearingDifferenceAbs(lastBearing: Double, newBearing: Double): Double =
        if (newBearing > lastBearing) newBearing - lastBearing else lastBearing - newBearing

    /**
     * Calculate bearing difference - classic method; Possible values in range (-360; 360)
     */
    fun bearingDifferenceClassic(lastBearing: Double, newBearing: Double): Double {
        var diff = newBearing - lastBearing
        while (diff <= -360.0) diff += 360.0
        while (diff >= 360.0) diff -= 360.0
        return diff
    }


    /**
     * Calculate distance between two points in latitude and longitude taking
     * into account height difference. If you are not interested in height
     * difference pass 0.0. Uses Haversine method as its base.
     *
     * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
     * el2 End altitude in meters
     * @returns Distance in Meters
     */
    fun distanceBetween(
        lat1: Double, lat2: Double, lon1: Double,
        lon2: Double, el1: Double, el2: Double,
    ): Double {
        val R = 6371 // Radius of the earth
        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = (sin(latDistance / 2) * sin(latDistance / 2)
                + (cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2))
                * sin(lonDistance / 2) * sin(lonDistance / 2)))
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        var distance = R * c * 1000 // convert to meters
        val height = el1 - el2
        distance = distance.pow(2.0) + height.pow(2.0)
        return sqrt(distance)
    }


    fun bearingBetweenPoints(newLocation: LocationWrapper, lastLocation: LocationWrapper): Double {
        // http://stackoverflow.com/questions/3925942/cllocation-category-for-calculating-bearing-w-haversine-function
        val lat1 = Math.PI * newLocation.latitude / 180.0
        val long1 = Math.PI * newLocation.longitude / 180.0
        val lat2 = Math.PI * lastLocation.latitude / 180.0
        val long2 = Math.PI * lastLocation.longitude / 180.0
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