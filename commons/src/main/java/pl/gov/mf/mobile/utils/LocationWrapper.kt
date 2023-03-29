package pl.gov.mf.mobile.utils

import android.location.Location
import android.os.Build
import androidx.annotation.RequiresApi
import org.joda.time.DateTime

data class LocationWrapper(private val location: Location) {

    private var _time: Long = location.time
    val time: Long
        get() = _time

    val altitude: Double
        get() = location.altitude

    val latitude: Double
        get() = location.latitude

    val longitude: Double
        get() = location.longitude

    val speed: Float
        get() = location.speed

    val accuracy: Float
        get() = location.accuracy

    val bearing: Float
        get() = location.bearing

    /*
     From doc: This method was deprecated in API level 31.
     Prefer isMock() instead.
    */
    @Deprecated("")
    val isFromMockProvider: Boolean
        get() = location.isFromMockProvider

    val isMock: Boolean
        @RequiresApi(Build.VERSION_CODES.S)
        get() = location.isMock


    fun normalize(currentDateTime: DateTime = DateTime.now()): LocationWrapper {
        val locationWrapperDateTime = DateTime(time)
        val hoursCount = 24
        val bottomBoundaryDateTime = currentDateTime.minusHours(hoursCount)
        val topBoundaryDateTime = currentDateTime.plusHours(hoursCount)

        _time =
            if (locationWrapperDateTime.isBefore(topBoundaryDateTime) &&
                locationWrapperDateTime.isAfter(bottomBoundaryDateTime)
            )
                locationWrapperDateTime.millis
            else
                currentDateTime.withTime(locationWrapperDateTime.toLocalTime()).millis

        return this
    }

}

fun Location.wrap(): LocationWrapper = LocationWrapper(this)

