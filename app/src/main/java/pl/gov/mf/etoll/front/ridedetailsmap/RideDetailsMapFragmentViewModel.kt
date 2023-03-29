package pl.gov.mf.etoll.front.ridedetailsmap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import org.joda.time.DateTime
import pl.gov.mf.etoll.base.BaseDatabindingViewModel
import pl.gov.mf.etoll.commons.Constants.DEFAULT_LATITUDE
import pl.gov.mf.etoll.commons.Constants.DEFAULT_LONGITUDE
import pl.gov.mf.etoll.commons.TimeUtils
import pl.gov.mf.etoll.core.CoreComposedUC
import pl.gov.mf.etoll.core.NetworkManagerUC
import pl.gov.mf.etoll.core.locationTracking.LocationTrackingUC
import pl.gov.mf.etoll.core.locationTracking.LocationTrackingUC.GetStartingLocationUseCase
import pl.gov.mf.etoll.core.model.CoreApplicationLastPosition
import pl.gov.mf.etoll.core.model.CoreMonitoringDeviceType
import pl.gov.mf.etoll.core.model.CoreTransitType
import pl.gov.mf.etoll.core.model.CoreZslLastPosition
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorConfiguration
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorV3
import pl.gov.mf.etoll.storage.settings.SettingsUC
import pl.gov.mf.mobile.networking.api.interceptors.WrongSystemTimeException
import pl.gov.mf.mobile.utils.LocationWrapper
import pl.gov.mf.mobile.utils.addSafe
import java.net.UnknownHostException
import javax.inject.Inject

class RideDetailsMapFragmentViewModel : BaseDatabindingViewModel() {

    @Inject
    lateinit var getLastRemotePositionsSentUseCase: NetworkManagerUC.GetLastPositionsSentUseCase

    @Inject
    lateinit var readSettingsUseCase: SettingsUC.ReadSettingsUseCase

    @Inject
    lateinit var rideCoordinatorV3: RideCoordinatorV3

    @Inject
    lateinit var getLastLocationUseCase: LocationTrackingUC.GetLastLocationUseCase

    @Inject
    lateinit var getStartingLocationUseCase: GetStartingLocationUseCase

    @Inject
    lateinit var timeIssuesDetectedUseCase: CoreComposedUC.TimeIssuesDetectedUseCase

    private val _navigate: MutableLiveData<NavigationTargets> =
        MutableLiveData(NavigationTargets.CURRENT)
    val navigate: LiveData<NavigationTargets>
        get() = _navigate

    private val _remotePositionsViewData = MutableLiveData<RemoteGpsViewData>()
    val remotePositionsViewData: LiveData<RemoteGpsViewData>
        get() = _remotePositionsViewData

    private val _localPositionsViewData = MutableLiveData<LocalGpsViewData>()
    val localPositionsViewData: LiveData<LocalGpsViewData>
        get() = _localPositionsViewData

    private val _startPositionViewData = MutableLiveData<StartLocationViewData>()
    val startPositionViewData: LiveData<StartLocationViewData>
        get() = _startPositionViewData

    val untranslatedScreenTitle: String
        get() = if (monitoringByZsl) "ride_details_map_zsl_title" else "ride_details_map_location_title"

    val monitoringByApp: Boolean
        get() = rideCoordinatorV3.getConfiguration()!!.monitoringDeviceConfiguration!!.monitoringByApp

    val monitoringByZsl: Boolean
        get() = !monitoringByApp

    fun initDefaults() {
        _remotePositionsViewData.postValue(RemoteGpsViewData())
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun getStartLocation() {
        getStartingLocationUseCase.execute()?.run {
            _startPositionViewData.postValue(
                StartLocationViewData(
                    starLatLng = LatLng(first, second),
                    startLatLngVisible = isNotDefault()
                )
            )
        }
    }

    private fun getLastLocalAppLocation() {
        getLastLocationUseCase.execute()?.let { lastLocalAppLocation ->
            _localPositionsViewData.postValue(
                LocalGpsViewData(
                    appLatLng = LatLng(
                        lastLocalAppLocation.latitude,
                        lastLocalAppLocation.longitude
                    ),
                    appLatLngTimestampAsDate = TimeUtils.DateTimeFormatterForRideDetails.print(
                        lastLocalAppLocation.time
                    ),
                    localAppLatLngVisible = lastLocalAppLocation.isNotDefault()
                )
            )
        }
    }

    private fun getLastRemotePositions() {
        compositeDisposable.addSafe(
            getLastRemotePositionsSentUseCase.execute(
                getTransitType(),
                //When monitoring by app - zslBusinessId parameter is skipped,
                //in other case we get parameter prepared for given ride type
                if (monitoringByZsl) getZslBusinessId() else null
            )
                .subscribe({ lastRemotePositions ->
                    with(lastRemotePositions) {
                        if (zslLastPosition == null) {
                            _remotePositionsViewData.postValue(
                                RemoteGpsViewData(
                                    appLatLng = LatLng(
                                        applicationLastPosition.latitude,
                                        applicationLastPosition.longitude
                                    ),
                                    appLatLngTimestampAsDate = convertStatusDateToRideDetailsDate(
                                        applicationLastPosition.dateTimestamp
                                    ),
                                    appLogVisible = applicationLastPosition.isNotDefault(),
                                    infoHeaderVisible = true,
                                    locationNotReportedVisible = false
                                )
                            )
                        } else {
                            zslLastPosition?.let {
                                _remotePositionsViewData.postValue(
                                    RemoteGpsViewData(
                                        appLatLng = LatLng(
                                            applicationLastPosition.latitude,
                                            applicationLastPosition.longitude
                                        ),
                                        appLatLngTimestampAsDate = convertStatusDateToRideDetailsDate(
                                            applicationLastPosition.dateTimestamp
                                        ),
                                        appLogVisible = applicationLastPosition.isNotDefault(),
                                        zslLatLng = LatLng(it.latitude, it.longitude),
                                        zslLatLngTimestampAsDate = convertStatusDateToRideDetailsDate(
                                            it.dateTimestamp
                                        ),
                                        zslVisible = it.isNotDefault(),
                                        infoHeaderVisible = true,
                                        locationNotReportedVisible = false
                                    )
                                )
                            }
                        }
                    }
                }, { error ->
                    if (error is WrongSystemTimeException) {
                        timeIssuesDetectedUseCase.execute(true)
                    }
                    if (error is UnknownHostException) {
                        _navigate.postValue(NavigationTargets.LOCATION_DATA_NOT_REPORTED)
                        _remotePositionsViewData.postValue(RemoteGpsViewData())
                    }
                })
        )
    }

    private fun getZslBusinessId(): String? {
        rideCoordinatorV3.getConfiguration()?.let { configuration ->
            return if (configuration.duringSent) {
                if (configuration.duringTolled) {
                    getZslBusinessIdParameterForMixedRide(configuration)
                } else {
                    getZslBusinessIdParameterForSentRide(configuration)
                }
            } else {
                getZslBusinessIdParameterForTolledRide(configuration)
            }
        }
        return null
    }

    //Tolled ride can become disabled during mixed ride, so we take zslBusinessId from SENT data
    private fun getZslBusinessIdParameterForMixedRide(rideConfiguration: RideCoordinatorConfiguration): String? =
        getZslBusinessIdParameterForSentRide(rideConfiguration)

    /**
     * For SENT we pass geolocatorNumber from sent list as zslBusinessId, if it's not available we pass failoverGeolocatorNumber.
     * If it's also not available we skip zslBusinessId parameter (by returning null for retrofit).
     */
    private fun getZslBusinessIdParameterForSentRide(rideConfiguration: RideCoordinatorConfiguration): String? {
        val selectedSentList = rideConfiguration.sentConfiguration?.selectedSentList
        var geolocatorNumber: String? = null
        var failoverGeolocatorNumber: String? = null
        if (!rideConfiguration.sentConfiguration?.selectedSentList.isNullOrEmpty()) {
            val firstSent = selectedSentList!![0]

            val geolocatorObeType = firstSent.vehicle?.geolocator?.obeType
            if (isNotMobile(geolocatorObeType)) {
                geolocatorNumber = firstSent.vehicle?.geolocator?.number
            }

            val failoverGeolocatorObeType = firstSent.vehicle?.failoverGeolocator?.obeType
            if (isNotMobile(failoverGeolocatorObeType)) {
                failoverGeolocatorNumber = firstSent.vehicle?.failoverGeolocator?.number
            }
        }

        return if (!geolocatorNumber.isNullOrEmpty()) geolocatorNumber
        else if (!failoverGeolocatorNumber.isNullOrEmpty()) failoverGeolocatorNumber
        else null
    }

    //For tolled rides we pass geolocatorNumber from status as zslBusinessId.
    //If geolocator is not available - pass null and skip zslBusinessId parameter
    private fun getZslBusinessIdParameterForTolledRide(rideConfiguration: RideCoordinatorConfiguration): String? {
        var geolocatorNumber: String? = null
        val geolocatorObeType = rideConfiguration.tolledConfiguration?.vehicle?.geolocator?.obeType

        if (isNotMobile(geolocatorObeType)) {
            geolocatorNumber = rideConfiguration.tolledConfiguration?.vehicle?.geolocator?.number
        }

        return geolocatorNumber
    }

    private fun isNotMobile(geolocatorObeType: String?) =
        geolocatorObeType != CoreMonitoringDeviceType.APP.apiName

    private fun getTransitType(): CoreTransitType {
        val rideConfiguration = rideCoordinatorV3.getConfiguration()
        return if (rideConfiguration?.duringSent == true) {
            if (rideConfiguration.duringTolled) CoreTransitType.MIXED
            else CoreTransitType.SENT
        } else {
            CoreTransitType.SPOE
        }
    }

    fun refresh() {
        getStartLocation()
        getLastLocalAppLocation()
        getLastRemotePositions()
    }

    private fun convertStatusDateToRideDetailsDate(date: Long): String {
        val inputDateTime = DateTime(date)
        return TimeUtils.DateTimeFormatterForRideDetails.print(inputDateTime)
    }

    fun resetNavigation() {
        _navigate.postValue(NavigationTargets.CURRENT)
    }

    private fun CoreApplicationLastPosition.isNotDefault(): Boolean =
        latitude != DEFAULT_LATITUDE && longitude != DEFAULT_LONGITUDE && dateTimestamp != DEFAULT_DATETIME

    private fun CoreZslLastPosition.isNotDefault(): Boolean =
        latitude != DEFAULT_LATITUDE && longitude != DEFAULT_LONGITUDE && dateTimestamp != DEFAULT_DATETIME

    private fun LocationWrapper.isNotDefault(): Boolean =
        latitude != DEFAULT_LATITUDE && longitude != DEFAULT_LONGITUDE

    private fun Pair<Double, Double>.isNotDefault(): Boolean =
        first != DEFAULT_LATITUDE && second != DEFAULT_LONGITUDE

    data class RemoteGpsViewData(
        val appLatLng: LatLng = LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE),
        val appLatLngTimestampAsDate: String = DEFAULT_DATETIME.toString(),
        val appLogVisible: Boolean = false,
        val zslLatLng: LatLng = LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE),
        val zslLatLngTimestampAsDate: String = DEFAULT_DATETIME.toString(),
        val zslVisible: Boolean = false,
        val locationNotReportedVisible: Boolean = true,
        val infoHeaderVisible: Boolean = false,
    )

    data class LocalGpsViewData(
        val appLatLng: LatLng = LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE),
        val appLatLngTimestampAsDate: String = DEFAULT_DATETIME.toString(),
        val localAppLatLngVisible: Boolean = false,
    )

    data class StartLocationViewData(
        val starLatLng: LatLng = LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE),
        val startLatLngVisible: Boolean = false,
    )

    enum class NavigationTargets {
        CURRENT, LOCATION_DATA_NOT_REPORTED
    }

    companion object {
        const val DEFAULT_DATETIME = 0L
    }

}