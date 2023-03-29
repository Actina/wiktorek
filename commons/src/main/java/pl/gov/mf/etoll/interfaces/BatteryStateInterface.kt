package pl.gov.mf.etoll.interfaces

interface BatteryStateInterface {
    fun onBatteryChanged(newLevel: WarningsBasicLevels)
}

interface BatteryStateInterfaceController {
    fun setCallback(batteryInterface: BatteryStateInterface, tag: String)
    fun removeCallback(tag: String)
    fun start()
    fun stop()
}