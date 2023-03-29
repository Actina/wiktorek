package pl.gov.mf.etoll.core.devicecompatibility

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.provider.Settings.System
import androidx.annotation.VisibleForTesting
import androidx.biometric.BiometricManager
import androidx.core.content.pm.PackageInfoCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import io.reactivex.Single
import pl.gov.mf.etoll.core.devicecompatibility.types.BatteryOptimisationInfo
import pl.gov.mf.etoll.core.devicecompatibility.types.BiometricState
import pl.gov.mf.etoll.core.devicecompatibility.types.GpsType
import pl.gov.mf.etoll.core.devicecompatibility.types.SystemInfoData
import pl.gov.mf.mobile.utils.translate
import javax.inject.Inject

class DeviceInfoProviderImpl @Inject constructor(
    private val context: Context,
) : DeviceInfoProvider {

    private val hardwareUnavailableStatuses = listOf(
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE
    )

    override fun getDeviceManufacturer(): String = Build.MANUFACTURER.uppercase()

    override fun getDeviceManufacturerBatteryOptimisationIntent(): Intent =
        when (getDeviceManufacturer()) {
            DeviceManufacturers.XIAOMI.name -> {
                Intent().apply {
                    addCategory(Intent.CATEGORY_DEFAULT)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    component = ComponentName(
                        "com.miui.powerkeeper",
                        "com.miui.powerkeeper.ui.HiddenAppsConfigActivity"
                    )
                    putExtra("package_name", context.packageName)
                    putExtra("package_label", getApplicationName())
                }
            }
            else -> {
                Intent().apply {
                    action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
                }
            }
        }

    override fun getDeviceManufacturerOverlayPermissionIntent(): Intent =
        when (getDeviceManufacturer()) {
            DeviceManufacturers.XIAOMI.name -> {
                Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                    setClassName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.permissions.PermissionsEditorActivity"
                    )
                    putExtra("extra_pkgname", context.packageName)
                }
            }
            else -> {
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + context.packageName)
                )
            }
        }

    private fun getApplicationName(): String {
        val packageManager = context.packageManager
        var applicationInfo: ApplicationInfo? = null
        try {
            applicationInfo =
                packageManager.getApplicationInfo(context.applicationInfo.packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
        }
        return (if (applicationInfo != null) packageManager.getApplicationLabel(applicationInfo) else "Unknown") as String
    }


    override fun isBatteryOptimisationEnabled(): BatteryOptimisationInfo =
        getPowerManager()?.run {
            if (isIgnoringBatteryOptimizations(context.packageName)) BatteryOptimisationInfo.OPTIMISATION_DISABLED else
                BatteryOptimisationInfo.OPTIMISATION_ENABLED
        } ?: BatteryOptimisationInfo.NO_INFO

    @VisibleForTesting
    override fun getPowerManager(): PowerManager? =
        context.getSystemService(Context.POWER_SERVICE) as? PowerManager

    override fun getGpsType(): GpsType {
        with(context.getSystemService(Context.LOCATION_SERVICE) as LocationManager) {
            when {
                /**
                 * - Most accurate
                 * - Uses GPS chip on the device without network support
                 * - Takes a long time to get a fix
                 **/
                getProvider(LocationManager.GPS_PROVIDER) != null &&
                        isProviderEnabled(LocationManager.GPS_PROVIDER) -> return GpsType.GPS

                /**
                 * - Less accurate
                 * - Uses GPS chip on device, as well as assistance from the network (cellular network)
                 * to provide a fast initial fix
                 */
                getProvider(LocationManager.NETWORK_PROVIDER) != null &&
                        isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> return GpsType.AGPS

                /**
                 *  "Rather than getting location information on its own, the passive location
                 *  providers get the location update when some other app has requested for it.
                 *  Since it depends on other’s data, it doesn’t control the source of the location update,
                 *  which means, it can either get the data from GPS provider, or from Network provider,
                 *  or from neither of them(if none of the other apps are receiving location updates)."
                (https://www.aanandshekharroy.com/articles/2018-01/android-location-sensors)
                 */
                getProvider(LocationManager.PASSIVE_PROVIDER) != null &&
                        isProviderEnabled(LocationManager.PASSIVE_PROVIDER) -> return GpsType.PASSIVE
                else -> return GpsType.NONE
            }
        }
    }

    /**
     * Allows to check synchronously that wifi/mobile data is off/on
     * Despite the informations in the documentation, this method doesn't show correctly that
     * our network has connection to internet, e.g. in test with tethering and sharing celluar
     * internet access between two phones
     */
    override fun isConnectedToNetwork(): Boolean {

        val networkCapabilities =
            (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).let {
                it.getNetworkCapabilities(it.activeNetwork)
            }

        /**
         * "Indicates that this network should be able to reach the internet."
         * (https://developer.android.com/reference/android/net/NetworkCapabilities#NET_CAPABILITY_INTERNET)
         */
        val shouldReachInternet: Boolean =
            networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false

        /**
         * "Indicates that connectivity on this network was successfully validated. For example,
         * for a network with NET_CAPABILITY_INTERNET, it means that Internet connectivity was successfully detected."
         * https://developer.android.com/reference/android/net/NetworkCapabilities#NET_CAPABILITY_VALIDATED
         * **/
        val networkConnectivityValidated: Boolean =
            networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                ?: false

        return shouldReachInternet && networkConnectivityValidated
    }

    /**
     *  Note: On releases before Build.VERSION_CODES.N, wifi service object should only be obtained from an
     *  Context#getApplicationContext(), and not from any other derived context to avoid memory
     *  leaks within the calling process.
     *  (https://developer.android.com/reference/android/net/wifi/WifiManager)
     */
    override fun isWifiServiceAvailable() =
//    context.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI) &&   // this is not
//    working on android api23 - at least on emu it gives always false. Anyway, checking system
//    service does same thing
        (context as Application).getSystemService(Context.WIFI_SERVICE) != null

    override fun isInternetConnection(): Single<Boolean> = Single.just(isConnectedToNetwork())
    // change made by decision of client at 11.10.2021 - we should not ping any external server to check
    // internet availability - we will work only with physical check of internet on device


    override fun isGpsEnabled(): Single<Boolean> = Single.create { emitter ->
        if (context.getSystemService(Context.LOCATION_SERVICE) != null) {
            with(context.getSystemService(Context.LOCATION_SERVICE) as LocationManager) {
                emitter.onSuccess(isProviderEnabled(LocationManager.GPS_PROVIDER))
            }
        } else {
            emitter.onError(IllegalStateException("gps not found"))
        }
    }

    override fun isLocationServiceAvailable(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

        val hasSystemFeatureGps =
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)
        val hasGpsProvider =
            locationManager?.allProviders?.contains(LocationManager.GPS_PROVIDER) ?: false

        return hasSystemFeatureGps && hasGpsProvider
    }

    override fun getSystemInfoData(): SystemInfoData {
        val versionNumber = Build.VERSION.SDK_INT
        val codeName = Build.VERSION_CODES::class.java.fields.firstOrNull {
            it.getInt(Build.VERSION_CODES::class) == versionNumber
        }?.name ?: "code_name_unknown_android".translate(context)

        val playServicesVersion = if (GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
        ) {
            PackageInfoCompat.getLongVersionCode(
                context.packageManager.getPackageInfo(
                    GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE,
                    0
                )
            ).toString()
        } else {
            "play_services_not_available".translate(context)
        }

        return SystemInfoData(
            Build.VERSION.RELEASE,
            versionNumber.toString(),
            codeName,
            playServicesVersion
        )
    }

    override fun getBiometricState(): BiometricState {
        val biometricState = BiometricManager.from(context).canAuthenticate()

        return when {
            hardwareUnavailableStatuses.contains(biometricState) -> BiometricState.NOT_AVAILABLE
            biometricState == BiometricManager.BIOMETRIC_SUCCESS -> BiometricState.AVAILABLE_AND_SWITCHED_ON
            else -> BiometricState.AVAILABLE_BUT_SWITCHED_OFF
        }
    }

    override fun isAutoTimeEnabled(): Boolean =
        System.getInt(context.contentResolver, Settings.Global.AUTO_TIME, 0) == 1

}