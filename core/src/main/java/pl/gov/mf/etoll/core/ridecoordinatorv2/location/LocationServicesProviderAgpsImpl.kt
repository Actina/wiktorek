package pl.gov.mf.etoll.core.ridecoordinatorv2.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import pl.gov.mf.etoll.core.BuildConfig
import pl.gov.mf.etoll.core.ridecoordinatorv2.filter.DebugGpsChecker
import pl.gov.mf.etoll.core.ridecoordinatorv2.location.issues.LocationIssuesDetector
import pl.gov.mf.etoll.core.ridecoordinatorv2.location.startloc.StartLocationContainer
import pl.gov.mf.etoll.core.watchdog.fakegps.FakeGpsCollector
import pl.gov.mf.etoll.logging.LogUseCase
import pl.gov.mf.mobile.utils.LocationWrapper
import pl.gov.mf.mobile.utils.wrap
import javax.inject.Inject


class LocationServicesProviderAgpsImpl @Inject constructor(
    private val context: Context,
    private val fusedLocationProviderClient: FusedLocationProviderClient,
    private val fakeGpsCollector: FakeGpsCollector,
    private val logUseCase: LogUseCase,
    private val debugGpsChecker: DebugGpsChecker,
    private val startLocation: StartLocationContainer,
    private val locationIssuesDetector: LocationIssuesDetector,
) :
    LocationServiceProvider {

    private var lastUsedLocationHashCode = -1
    private var lastKnownLocationTolled: LocationWrapper? = null
    private var lastKnownLocationSent: LocationWrapper? = null
    private var lastSavedLocation: LocationWrapper? = null
    private var lastDataReceivingTimestamp = 0L
    private var startLocationShouldBeOverwritten = false

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val lastLocationWrapper = locationResult.lastLocation.wrap().normalize()
            logUseCase.log(LogUseCase.GPS, "Got location from GPS! $lastLocationWrapper")
            Log.d("LOCATION_CHECK", "Otrzymano lokalizację z gps: $lastLocationWrapper")
            val location = lastLocationWrapper.orNullIfOutdated(fakeGpsCollector)
            lastKnownLocationTolled = location
            lastKnownLocationSent = location
            lastDataReceivingTimestamp = System.currentTimeMillis()
            if (startLocationShouldBeOverwritten && startLocation.getLocation() == null && location != null) {
                startLocation.setLocation(location)
                startLocationShouldBeOverwritten = false
            }
            locationIssuesDetector.onNextLocation(location)
        }
    }


    override fun start() {
        lastKnownLocationTolled = null
        lastKnownLocationSent = null
        lastSavedLocation = null
        lastDataReceivingTimestamp = 0L
        fakeGpsCollector.changeLastKnownLocationState(false)
        logUseCase.log(LogUseCase.GPS, "Starting GPS!")
        Log.d("LOCATION_CHECK", "STARTING GPS")
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        // start warming
        // this part should be done on correct thread
        Completable.create {
            val locationRequest = LocationRequest.create()
            locationRequest.interval = 5000
            locationRequest.fastestInterval =
                5000
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest.maxWaitTime = 9000
            // new change
            locationRequest.isWaitForAccurateLocation = true
            locationRequest.smallestDisplacement = 0.0f//6.0f
            Looper.myLooper()?.let { looper ->
                fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback, looper
                )
            }
        }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())
            .subscribe()
//        }

        logUseCase.log(LogUseCase.GPS, "STARTED GPS")
        Log.d("LOCATION_CHECK", "STARTED GPS")
    }

    override fun stop() {
        logUseCase.log(LogUseCase.GPS, "STOPPING GPS")
        Log.d("LOCATION_CHECK", "STOPPING GPS")
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        logUseCase.log(LogUseCase.GPS, "STOPPED GPS")
        Log.d("LOCATION_CHECK", "STOPPED GPS")
    }

    override fun getLastKnownSavedLocation(): LocationWrapper? = lastSavedLocation

    override fun getStartLocation(): Pair<Double, Double>? = startLocation.getLocation()

    override fun setFlagNextLocationWillBeStartLocation() {
        startLocation.setLocation(null)
        startLocationShouldBeOverwritten = true
    }

    @SuppressLint("MissingPermission")
    override fun getLastKnownLocation(forTolled: Boolean): LocationWrapper? = if (forTolled) {
        lastKnownLocationTolled.orNullIfOutdated(fakeGpsCollector)
    } else
        lastKnownLocationSent.orNullIfOutdated(fakeGpsCollector)

    override fun consumeLastKnownLocation(forTolled: Boolean, location: LocationWrapper?) {
        if (location != null)
            lastUsedLocationHashCode = location.hashCode()
    }

    override fun isInOnlyGpsMode(): Boolean = false

    override fun getLastLocationReceiveTimestamp(): Long = lastDataReceivingTimestamp

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
