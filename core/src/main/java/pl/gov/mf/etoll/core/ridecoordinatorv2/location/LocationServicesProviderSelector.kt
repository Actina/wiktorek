package pl.gov.mf.etoll.core.ridecoordinatorv2.location

import android.util.Log
import pl.gov.mf.mobile.utils.LocationWrapper
import javax.inject.Inject

class LocationServicesProviderSelector @Inject constructor(
    private val agpsImplementation: LocationServicesProviderAgpsImpl,
    private val gpsImplementation: LocationServicesProviderGpsImplementation,
) : LocationServiceProvider {

    companion object {
        const val selectedModeGps = true
    }

    private val selectedImplementation: LocationServiceProvider
        get() = if (selectedModeGps) gpsImplementation else agpsImplementation

    override fun start() {
        Log.d("GPS_IMPL", "STARTING WITH IMPL: " + selectedImplementation)
        selectedImplementation.start()
    }

    override fun stop() {
        selectedImplementation.stop()
    }

    override fun getLastKnownLocation(forTolled: Boolean): LocationWrapper? =
        selectedImplementation.getLastKnownLocation(forTolled)

    override fun consumeLastKnownLocation(forTolled: Boolean, location: LocationWrapper?) {
        selectedImplementation.consumeLastKnownLocation(forTolled, location)
    }

    override fun isInOnlyGpsMode(): Boolean = selectedImplementation.isInOnlyGpsMode()

    override fun getLastLocationReceiveTimestamp(): Long =
        selectedImplementation.getLastLocationReceiveTimestamp()

    override fun getLastKnownSavedLocation(): LocationWrapper? =
        selectedImplementation.getLastKnownSavedLocation()

    override fun getStartLocation(): Pair<Double, Double>? =
        selectedImplementation.getStartLocation()

    override fun setFlagNextLocationWillBeStartLocation() {
        selectedImplementation.setFlagNextLocationWillBeStartLocation()
    }
}