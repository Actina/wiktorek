package pl.gov.mf.etoll.front.configsentridesselection.details

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import pl.gov.mf.etoll.base.BaseDatabindingViewModel
import pl.gov.mf.etoll.commons.Constants.DEFAULT_LATITUDE
import pl.gov.mf.etoll.commons.Constants.DEFAULT_LONGITUDE
import pl.gov.mf.etoll.core.locationTracking.LocationTrackingUC
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorV3
import pl.gov.mf.mobile.utils.LocationWrapper
import pl.gov.mf.mobile.utils.addSafe
import pl.gov.mf.mobile.utils.disposeSafe
import pl.gov.mf.mobile.utils.toObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SentRideDetailsFragmentViewModel : BaseDatabindingViewModel() {

    @Inject
    lateinit var startLocationTrackingUseCase: LocationTrackingUC.StartLocationTrackingUseCase

    @Inject
    lateinit var getLastLocationUseCase: LocationTrackingUC.GetLastLocationUseCase

    @Inject
    lateinit var stopLocationTrackingUseCase: LocationTrackingUC.StopLocationTrackingUseCase

    @Inject
    lateinit var rideCoordinatorV3: RideCoordinatorV3

    private var timerDisposable: Disposable? = null

    private val _sent = MutableLiveData<SentMapDetailsData>()
    val sent: LiveData<SentMapDetailsData> = _sent

    private val _navigate: MutableLiveData<SentRideDetailsNavigationLocation> =
        MutableLiveData(SentRideDetailsNavigationLocation.CURRENT)
    val navigate: LiveData<SentRideDetailsNavigationLocation> = _navigate

    private val _lastLocalAppLocation = MutableLiveData(LocalGpsViewData())
    val lastLocalAppLocation: LiveData<LocalGpsViewData> = _lastLocalAppLocation

    fun updateSent(arguments: Bundle) {
        val sent = arguments.getString("sent")?.toObject<SentMapDetailsData>()
        if (sent == null)
            _navigate.postValue(SentRideDetailsNavigationLocation.ERROR)
        else
            this._sent.postValue(sent)
    }

    override fun onResume() {
        super.onResume()
        if (canStartingAndStoppingGps()) {
            startLocationUpdates()
        } else if (duringRideBeingMonitoredByApp()) {
            getLastLocationUseCase.execute()?.let {
                _lastLocalAppLocation.value = LocalGpsViewData(
                    appLatLng = it.toLatLng(),
                    localAppLatLngVisible = true
                )
                Log.d(SENT_MAP_GPS_LOG, "Correct location received from already started GPS: $it")
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (canStartingAndStoppingGps())
            stopLocationUpdates()
    }

    private fun canStartingAndStoppingGps(): Boolean = rideCoordinatorV3.getConfiguration()?.run {
        !duringRide || (duringRide && !(monitoringDeviceConfiguration?.monitoringByApp ?: false))
    } ?: false

    private fun duringRideBeingMonitoredByApp() =
        rideCoordinatorV3.getConfiguration()?.run {
            duringRide && (monitoringDeviceConfiguration?.monitoringByApp ?: false)
        } == true

    private fun startLocationUpdates() {
        compositeDisposable.addSafe(startLocationTrackingUseCase.execute().subscribe({
            //Start GPS and until we don't get correct location - request for location every 1 sec.
            var gpsRetryCounter = 0
            timerDisposable = Observable.interval(0, 1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    getLastLocationUseCase.execute()
                        ?.takeIf { it.isNotDefault() }
                        ?.let { newLocation ->
                            //We got correct location - display it and stop GPS till screen refresh
                            _lastLocalAppLocation.value = LocalGpsViewData(
                                appLatLng = newLocation.toLatLng(),
                                localAppLatLngVisible = true
                            )
                            Log.d(
                                SENT_MAP_GPS_LOG,
                                "Correct location received from newly started GPS: $newLocation"
                            )

                            stopLocationUpdates()
                        } ?: run {
                        ++gpsRetryCounter

                        Log.d(
                            SENT_MAP_GPS_LOG,
                            "Bad or null location, incremented GPS retry counter: $gpsRetryCounter"
                        )

                        if (gpsRetryCounter >= GPS_RETRY_LIMIT) {
                            Log.d(
                                SENT_MAP_GPS_LOG,
                                "Retry limit reached, stopping location updates!"
                            )
                            stopLocationUpdates()
                        }
                    }
                }, {
                    Log.d(SENT_MAP_GPS_LOG, "Error in retry loop")

                    //In case of any error stop GPS and show just start and destination points
                    stopLocationUpdates()
                })
        }, {
            Log.d(SENT_MAP_GPS_LOG, "Error on GPS starting: $it")
            //In case of any error stop GPS and show just start and destination points
            stopLocationUpdates()
        }))
    }

    private fun stopLocationUpdates() {
        Log.d(SENT_MAP_GPS_LOG, "Stopping location updates")
        timerDisposable.disposeSafe()
        timerDisposable = null
        compositeDisposable.addSafe(stopLocationTrackingUseCase.execute().subscribe({}, {}))
    }

    private fun LocationWrapper.toLatLng(): LatLng = LatLng(latitude, longitude)

    private fun LocationWrapper.isNotDefault(): Boolean =
        latitude != DEFAULT_LATITUDE && longitude != DEFAULT_LONGITUDE

    fun resetNavigation() {
        _navigate.postValue(SentRideDetailsNavigationLocation.CURRENT)
    }

    data class LocalGpsViewData(
        val appLatLng: LatLng = LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE),
        val localAppLatLngVisible: Boolean = false,
    )

    enum class SentRideDetailsNavigationLocation {
        CURRENT,
        ERROR,
    }

    companion object {
        const val GPS_RETRY_LIMIT = 30
        const val SENT_MAP_GPS_LOG = "SENT_MAP_GPS_LOG"
    }

}