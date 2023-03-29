package pl.gov.mf.etoll.core.devicecompatibility

import android.content.Intent
import android.location.LocationManager
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import pl.gov.mf.etoll.core.devicecompatibility.types.BatteryOptimisationInfo
import pl.gov.mf.etoll.core.devicecompatibility.types.GpsType
import pl.gov.mf.etoll.core.devicecompatibility.types.SystemInfoData
import javax.inject.Inject


sealed class DeviceCompatibilityUC {

    class GetManufacturerBatteryOptimizationIntent(private val deviceInfoProvider: DeviceInfoProvider) {
        fun execute(): Intent = deviceInfoProvider.getDeviceManufacturerBatteryOptimisationIntent()
    }

    class CheckBatteryOptimisationUseCase(private val deviceInfoProvider: DeviceInfoProvider) {
        fun execute(): BatteryOptimisationInfo = deviceInfoProvider.isBatteryOptimisationEnabled()
    }

    class CheckInternetConnectionUseCase(private val deviceInfoProvider: DeviceInfoProvider) {
        fun executeAsync(): Single<Boolean> = deviceInfoProvider.isInternetConnection()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

        fun executeSync(): Boolean = deviceInfoProvider.isConnectedToNetwork()
    }

    class CheckAutoTimeEnabledUseCase(private val deviceInfoProvider: DeviceInfoProvider){
        fun execute(): Boolean = deviceInfoProvider.isAutoTimeEnabled()
    }

    class CheckWifiServiceAvailableUseCase(private val deviceInfoProvider: DeviceInfoProvider) {
        fun execute(): Boolean = deviceInfoProvider.isWifiServiceAvailable()
    }

    class CheckGpsLocationServiceAvailable(private val deviceInfoProvider: DeviceInfoProvider) {
        fun execute(): Boolean = deviceInfoProvider.isLocationServiceAvailable()
    }

    class CheckGpsTypeUseCase(private val deviceInfoProvider: DeviceInfoProvider) {
        fun execute(): GpsType = deviceInfoProvider.getGpsType()
    }

    class CheckGpsStateUseCase(
        private val deviceInfoProvider: DeviceInfoProvider,
        private val locationManager: LocationManager,
    ) {
        fun executeAsync(): Single<Boolean> = deviceInfoProvider.isGpsEnabled()

        fun execute(): Boolean = deviceInfoProvider.isGpsEnabled()
            .blockingGet()

        fun executeAlternativeCode(): Boolean {
            var gps_enabled = false
            var network_enabled = false

            try {
                gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            } catch (ex: Exception) {
            }

            try {
                network_enabled =
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            } catch (ex: Exception) {
            }

            return (gps_enabled || network_enabled)
        }
    }

    class GetSystemInfoUseCase @Inject constructor(private val deviceInfoProvider: DeviceInfoProvider) :
        DeviceCompatibilityUC() {
        fun execute(): SystemInfoData = deviceInfoProvider.getSystemInfoData()
    }

    class GetBiometricStateUseCase @Inject constructor(private val deviceInfoProvider: DeviceInfoProvider) :
        DeviceCompatibilityUC() {
        fun execute() = deviceInfoProvider.getBiometricState()
    }
}