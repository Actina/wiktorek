package pl.gov.mf.etoll.core.ridecoordinatorv2.merger

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import pl.gov.mf.etoll.commons.TimeUtils
import pl.gov.mf.etoll.core.ridecoordinatorv2.events.EventType
import pl.gov.mf.etoll.core.ridecoordinatorv2.mobile.MobileData
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorConfiguration
import pl.gov.mf.etoll.networking.api.model.EventStreamBaseEvent
import pl.gov.mf.etoll.networking.api.model.EventStreamLocation
import pl.gov.mf.etoll.networking.api.model.EventStreamLocationWithoutLocation
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC
import pl.gov.mf.mobile.utils.BatteryUtils
import pl.gov.mf.mobile.utils.LocationWrapper
import javax.inject.Inject


class EventStreamLocationMergerImpl @Inject constructor(
    private val context: Context,
    private val readSettingsUseCase: SettingsUC.ReadSettingsUseCase
) :
    EventStreamLocationMerger {
    override fun merge(
        location: LocationWrapper?,
        mobileData: MobileData?,
        eventType: EventType,
        configuration: RideCoordinatorConfiguration,
        targetIsSent: Boolean
    ): EventStreamBaseEvent {
        val serialNumber = readSettingsUseCase.executeForString(Settings.DEVICE_ID)
        val targetSystem = if (targetIsSent) {
            RideTargetSystems.SENT
        } else {
            if (configuration.lightMode)
                RideTargetSystems.TOLLED_LIGHT
            else
                RideTargetSystems.TOLLED
        }

        val mcc = mobileData?.mccMnc ?: "0"
        val mnc = mobileData?.mnc ?: "0"
        val cid = mobileData?.cid ?: "0"
        val lac = mobileData?.lacTac ?: "0"
        val foreground = readSettingsUseCase.executeForBoolean(Settings.APP_IN_FOREGROUND)

        var internetAvailable = isNetworkAvailable()
        var batteryLevel = BatteryUtils.getCurrentBatteryState(context) ?: BatteryUtils.BATTERY_UNKNOWN
        return if (location == null) {
            EventStreamLocationWithoutLocation(
                targetSystem = targetSystem.apiValue,
                dataId = "",
                serialNumber = serialNumber,
                fixTime = TimeUtils.getCurrentTimeForEvents(),
                eventType = eventType.apiName,
                lac = lac,
                mcc = mcc,
                mnc = mnc,
                mobileCellId = cid,
                appInForeground = foreground,
                internetAvailable = internetAvailable,
                batteryLevel = batteryLevel
            )
        } else {
            EventStreamLocation(
                targetSystem = targetSystem.apiValue,
                dataId = "",
                serialNumber = serialNumber,
                latitude = location.latitude,
                longitude = location.longitude,
                altitude = location.altitude,
                fixTime = TimeUtils.getCurrentTimeForEventsFromLocation(location),
                gpsSpeed = location.speed.toDouble(),
                accuracy = location.accuracy.toDouble(),
                gpsHeading = location.bearing.toDouble(),
                eventType = eventType.apiName,
                lac = lac,
                mcc = mcc,
                mnc = mnc,
                mobileCellId = cid,
                dataFromHardwareGps = !location.isFromMockProvider,
                appInForeground = foreground,
                internetAvailable = internetAvailable,
                batteryLevel = batteryLevel
            )
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nw = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                //for other device how are able to connect with Ethernet
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                //for check internet over Bluetooth
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                else -> false
            }
        } else {
            return connectivityManager.activeNetworkInfo?.isConnected ?: false
        }
    }
}

