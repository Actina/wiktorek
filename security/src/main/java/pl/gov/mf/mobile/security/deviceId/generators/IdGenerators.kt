package pl.gov.mf.mobile.security.deviceId.generators

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.media.MediaDrm
import android.os.Build
import android.provider.Settings.Secure
import pl.gov.mf.etoll.commons.TimeUtils
import java.util.*

sealed class IdGenerators : DeviceIdGenerationStrategy {

    class BuildSerialDeviceIdGenerator : IdGenerators() {
        @SuppressLint("HardwareIds")
        override fun generateDeviceId(): String = Build.SERIAL
    }

    /** Most reliable solution for unique device id according to:
    https://beltran.work/blog/2018-03-27-device-unique-id-android
    https://stackoverflow.com/questions/58103580/android-10-imei-no-longer-available-on-api-29-looking-for-alternatives
    Features:
     * - ID initializes during "device provisioning" phase, when device uses DRM for the first time.
     * - Should works fine also on Android >=9.
     * - "This ID is not only the same on all apps, but also it is the same for all users of the device.
     * So a guest account, for example, will also obtain the same ID, as opposed to the ANDROID_ID."
     * - No permissions are required to access this ID.
     * - Factory reset restarts "provisioning profile" and device receives new ID.
     **/
    class MediaDrmDeviceIdGenerator : IdGenerators() {
        override fun generateDeviceId(): String? {
            /**
            Passing correct values as UUID parameters we select proper Widevine DRM provider.
            Other options are described in https://beltran.work/blog/2018-03-27-device-unique-id-android/
             **/
            val widevineUuid = UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L)
            var mediaDRM: MediaDrm? = null
            try {
                mediaDRM = MediaDrm(widevineUuid)
                val widevineId = mediaDRM.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID)
                /**
                It's possible to apply SHA-256 for widevineId, to have fixed-length ids:
                val md = MessageDigest.getInstance("SHA-256")
                md.update(widevineId)
                return md.digest().toHexString()
                but it "introduces an extremely low probability of losing UUID uniqueness"
                (https://stackoverflow.com/questions/58103580/android-10-imei-no-longer-available-on-api-29-looking-for-alternatives)
                 **/
                return widevineId.toHexString()
            } catch (e: Exception) {
                //Widevine is not available
                return null
            } finally {
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> mediaDRM?.close()
                    else -> mediaDRM?.release()
                }
            }
        }

        private fun ByteArray.toHexString() =
            joinToString(TimeUtils.EMPTY_STRING) { "%02x".format(it) }
    }

    /**
     * Important to note:
     * - "For apps that were installed prior to an OTA to a version of Android 8.0
    (API level 26), the value of ANDROID_ID remains the same unless
    uninstalled and then reinstalled after the OTA. To preserve values across uninstalls
    after OTA, developers can associate the old and new values by using Key/Value Backup."
    (https://developer.android.com/about/versions/oreo/android-8.0-changes)

    -  Since Android 8.0, the ANDROID_ID is generated for each app, but does not change on
    package uninstall or reinstall, as long as the signing key is the same (and the app was not
    installed prior to an OTA to a version of Android 8.0). In other words, since Android 8.0 apps
    with different signing keys running on the same device no longer see the same Android ID
    (even for the same user). However, value of ANDROID_ID does not change when system
    update causes the package signing key to change.

    - Other account on the same device, will not obtain the same ID, as opposed to ID generated
    using MediaDrm.
     */
    class SecureIdDeviceIdGenerator(private val contentResolver: ContentResolver) : IdGenerators() {
        @SuppressLint("HardwareIds")
        override fun generateDeviceId(): String? =
            Secure.getString(contentResolver, Secure.ANDROID_ID)
    }

    /**
     * Solution inspired by https://www.pocketmagic.net/android-unique-device-id/
     * Generates Pseuo-Unique ID. "It is possible to find two devices with the same ID
     * (based on the same hardware and rom image) but the chances in real
     * world applications are negligible." Generated deviceId can serve as emergency identifier and
     * for e.g. be locked/specially handled on server side. It shows details about edge-case devices,
     * which cannot generate deviceId using other methods.
     * POTENTIALLY_CHANGEABLE_PARAMETERS were added to minimise risk of two users
     * will have the same device id.
     */
    class PseudoUniqueDeviceIdGenerator : IdGenerators() {

        companion object {
            val CONST_HARDWARE_PARAMETERS = (Build.BOARD + " " + Build.BRAND + " "
                    + Build.CPU_ABI + " " + Build.DEVICE + " " + Build.MANUFACTURER + " " + Build.MODEL + " "
                    + Build.PRODUCT)
            val POTENTIALLY_CHANGEABLE_PARAMETERS = Build.ID + " " +
                    Build.DISPLAY + " " + Build.HOST + " " + Build.TYPE + " " + Build.USER + " " + Build.TAGS
        }

        @SuppressLint("HardwareIds")
        override fun generateDeviceId(): String =
            CONST_HARDWARE_PARAMETERS + POTENTIALLY_CHANGEABLE_PARAMETERS

    }

}