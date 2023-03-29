package pl.gov.mf.etoll.core.ridecoordinatorv2.mobile

interface MobileDataprovider {
    fun getStatus(): MobileDataproviderStatus
    fun getLatestData(): MobileData?
}

enum class MobileDataproviderStatus {
    WORKING, PERMISSION_ISSUES
}