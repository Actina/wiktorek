package pl.gov.mf.etoll.interfaces

interface GpsStateInterface {
    fun onGpsChanged(newLevel: WarningsBasicLevels)
}


interface GpsStateInterfaceController {
    fun setCallback(gpsInterface: GpsStateInterface, tag: String)
    fun removeCallback(tag: String)
    fun start()
    fun stop()
}