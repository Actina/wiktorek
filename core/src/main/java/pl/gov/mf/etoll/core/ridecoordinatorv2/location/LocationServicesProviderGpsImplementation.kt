package pl.gov.mf.etoll.core.ridecoordinatorv2.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import pl.gov.mf.etoll.core.BuildConfig
import pl.gov.mf.etoll.core.ridecoordinatorv2.filter.DebugGpsChecker
import pl.gov.mf.etoll.core.ridecoordinatorv2.location.issues.LocationIssuesDetector
import pl.gov.mf.etoll.core.ridecoordinatorv2.location.startloc.StartLocationContainer
import pl.gov.mf.etoll.core.watchdog.fakegps.FakeGpsCollector
import pl.gov.mf.etoll.logging.LogUseCase
import pl.gov.mf.mobile.utils.LocationWrapper
import pl.gov.mf.mobile.utils.wrap
import javax.inject.Inject

class LocationServicesProviderGpsImplementation @Inject constructor(
    private val context: Context,
    private val logUseCase: LogUseCase,
    private val fakeGpsCollector: FakeGpsCollector,
    private val debugGpsChecker: DebugGpsChecker,
    private val startLocation: StartLocationContainer,
    private val locationIssuesDetector: LocationIssuesDetector,
) :
    LocationServiceProvider, LocationListener {

    companion object {
        private const val MINIMUM_INTERVAL = 1000L
        private const val providerName = LocationManager.GPS_PROVIDER
    }

    private var lastKnownLocationTolled: LocationWrapper? = null
    private var lastKnownLocationSent: LocationWrapper? = null
    private var lastSavedLocation: LocationWrapper? = null
    private var lastDataReceivingTimestamp = 0L
    private var startLocationShouldBeOverwritten = false

    var locationManager: LocationManager? = null

    @SuppressLint("MissingPermission")
    override fun start() {
        lastKnownLocationTolled = null
        lastKnownLocationSent = null
        lastSavedLocation = null
        lastDataReceivingTimestamp = 0L
        logUseCase.log(LogUseCase.GPS, "Starting gps")
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager?.requestLocationUpdates(
            providerName, MINIMUM_INTERVAL, 0f, this
        )
    }

    override fun stop() {
        logUseCase.log(LogUseCase.GPS, "Stopping gps")
        locationManager?.removeUpdates(this)
    }

    @SuppressLint("MissingPermission")
    override fun getLastKnownLocation(forTolled: Boolean): LocationWrapper? = if (forTolled) {
        lastKnownLocationTolled.orNullIfOutdated(fakeGpsCollector)
    } else
        lastKnownLocationSent.orNullIfOutdated(fakeGpsCollector)

    override fun consumeLastKnownLocation(forTolled: Boolean, location: LocationWrapper?) {
        if (forTolled) {
            if (location == null || location.time == lastKnownLocationTolled?.time)
                lastKnownLocationTolled = null
        } else {
            if (location == null || location.time == lastKnownLocationSent?.time)
                lastKnownLocationSent = null
        }
    }

    override fun isInOnlyGpsMode(): Boolean = true

    override fun getLastLocationReceiveTimestamp(): Long = lastDataReceivingTimestamp

    override fun getLastKnownSavedLocation(): LocationWrapper? = lastSavedLocation

    override fun getStartLocation(): Pair<Double, Double>? = startLocation.getLocation()

    override fun setFlagNextLocationWillBeStartLocation() {
        startLocation.setLocation(null)
        startLocationShouldBeOverwritten = true
    }

    override fun onLocationChanged(location: Location) {
        val locationWrapper = location.wrap().normalize()
        lastKnownLocationTolled = locationWrapper
        lastKnownLocationSent = locationWrapper
        lastSavedLocation = locationWrapper
        if (startLocationShouldBeOverwritten && startLocation.getLocation() == null) {
            startLocation.setLocation(locationWrapper)
            startLocationShouldBeOverwritten = false
        }
        lastDataReceivingTimestamp = System.currentTimeMillis()
        locationIssuesDetector.onNextLocation(locationWrapper)
    }

    /*
    Methods overriden below are required to prevent crashes which happens when gps
    provider changes state without them
     */
    override fun onStatusChanged(
        provider: String?,
        status: Int,
        extras: Bundle?,
    ) {
        logUseCase.log(LogUseCase.GPS, "Provider status changed $status")
        Log.d("LOCATION_CHECK", "Gps provider status changed: $status")
    }

    override fun onProviderEnabled(provider: String) {
        logUseCase.log(LogUseCase.GPS, "Provider enabled $provider")
        Log.d("LOCATION_CHECK", "Gps provider enabled: $provider")
    }

    override fun onProviderDisabled(provider: String) {
        logUseCase.log(LogUseCase.GPS, "Provider disabled $provider")
        Log.d("LOCATION_CHECK", "Provider disabled: $provider")
    }

    /**
     * Pass even too old locations
     */
    private fun LocationWrapper?.orNullIfOutdated(fakeGpsCollector: FakeGpsCollector): LocationWrapper? {
        if (this == null) {
            logUseCase.log(LogUseCase.GPS, "Location validation: null")
            Log.d("LOCATION_CHECK", "Walidacja lokalizacji: null")
            return null
        }
        // set info if last known location was fake
        fakeGpsCollector.changeLastKnownLocationState(isFromMockProvider)
        // block data from fake providers
        if (isFromMockProvider) {
            logUseCase.log(LogUseCase.GPS, "Location validation: fake gps!")
            Log.d("LOCATION_CHECK", "Walidacja lokalizacji: - fake gps, odpada " + this)
            return null
        }

        // remove locations from outside Poland - TODO: do ustalenia z klientem (!!) 20.06.2021
        if (BuildConfig.DEBUG) {
            return if (debugGpsChecker.isLocationValid(this)) this else null
        }
        return this
    }

}