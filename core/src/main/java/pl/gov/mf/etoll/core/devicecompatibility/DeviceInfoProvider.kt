package pl.gov.mf.etoll.core.devicecompatibility

import android.content.Intent
import android.os.PowerManager
import io.reactivex.Single
import pl.gov.mf.etoll.core.devicecompatibility.types.BatteryOptimisationInfo
import pl.gov.mf.etoll.core.devicecompatibility.types.BiometricState
import pl.gov.mf.etoll.core.devicecompatibility.types.GpsType
import pl.gov.mf.etoll.core.devicecompatibility.types.SystemInfoData

interface DeviceInfoProvider {
    fun getDeviceManufacturer(): String
    fun getDeviceManufacturerBatteryOptimisationIntent(): Intent
    fun getDeviceManufacturerOverlayPermissionIntent(): Intent
    fun isBatteryOptimisationEnabled(): BatteryOptimisationInfo
    fun isConnectedToNetwork(): Boolean
    fun isInternetConnection(): Single<Boolean>
    fun getPowerManager(): PowerManager?
    fun getGpsType(): GpsType
    fun isGpsEnabled(): Single<Boolean>
    fun getSystemInfoData(): SystemInfoData
    fun getBiometricState(): BiometricState
    fun isAutoTimeEnabled(): Boolean
    fun isWifiServiceAvailable(): Boolean
    fun isLocationServiceAvailable(): Boolean
}