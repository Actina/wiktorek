package pl.gov.mf.etoll.logging

import javax.inject.Inject

class LogUseCase @Inject constructor(private val logger: LoggingHelper) {
    fun log(tag: String, desc: String) = logger.log(tag, desc)

    companion object {
        const val APP = "App"
        const val NETWORK = "Net"
        const val GPS = "GPS"
        const val RIDE_COORDINATOR = "RideCoord"
    }
}