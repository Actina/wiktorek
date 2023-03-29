package pl.gov.mf.etoll.core.ridecoordinatorv2.sent.adv.testhelpers.player

interface RideDataPlayer {
    fun start(trackName: String)
    fun stop()
    fun playNextLocation(): Boolean
    fun getSumOfGeneratedEvents(): Int
}