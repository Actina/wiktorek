package pl.gov.mf.mobile.security.deviceId

import android.annotation.SuppressLint
import pl.gov.mf.mobile.security.deviceId.generators.IdGenerators.*
import javax.inject.Inject

class DeviceIdProviderImpl @Inject constructor(
    private val mediaDrmDeviceIdGenerator: MediaDrmDeviceIdGenerator,
    private val secureIdDeviceIdGenerator: SecureIdDeviceIdGenerator,
    private val buildSerialDeviceIdGenerator: BuildSerialDeviceIdGenerator,
    private val pseudoUniqueDeviceIdGenerator: PseudoUniqueDeviceIdGenerator
) : DeviceIdProvider {

    private lateinit var stored: String

    @SuppressLint("HardwareIds")
    override fun getDeviceId(): String {
        if (!::stored.isInitialized) {
            val mediaDrmId = mediaDrmDeviceIdGenerator.generateDeviceId()
            stored = if (!mediaDrmId.isNullOrBlank()) mediaDrmId
            else {
                /**
                 * If there will be (uncharted for now) problems with mediaDrmId generation on Android >= O,
                 * we should consider here, if we want to use secure id or another deviceId generation
                 * methods (please, see comments above SecureIdDeviceGenerator)
                 */
                val secureId = secureIdDeviceIdGenerator.generateDeviceId()
                if (!secureId.isNullOrBlank()) secureId
                else {
                    val buildSerialId = buildSerialDeviceIdGenerator.generateDeviceId()
                    /**
                    "In Android 9, Build.SERIAL is always set to "UNKNOWN" to protect users' privacy.
                    If your app needs to access a device's hardware serial number, you should instead
                    request the READ_PHONE_STATE permission, then call getSerial()."
                    (https://developer.android.com/about/versions/pie/android-9.0-changes-28)
                     **/
                    if (!buildSerialId.isBlank() && !buildSerialId.equals("unknown", true))
                        buildSerialId
                    else pseudoUniqueDeviceIdGenerator.generateDeviceId()
                }
            }
        }
        return stored
    }

}