package pl.gov.mf.mobile.security.deviceId

interface DeviceIdProvider {
    fun getDeviceId(): String
}