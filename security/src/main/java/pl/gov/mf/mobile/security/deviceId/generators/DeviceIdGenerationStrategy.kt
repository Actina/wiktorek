package pl.gov.mf.mobile.security.deviceId.generators

interface DeviceIdGenerationStrategy {
    fun generateDeviceId(): String?
}