package pl.gov.mf.etoll.interfaces

sealed class CriticalMessageState
data class BatteryState(
    var batterySignal: WarningsBasicLevels
) : CriticalMessageState()

data class GpsState(var gpsSignal: WarningsBasicLevels) : CriticalMessageState()
data class DataConnectionState(var dataConnectionSignal: WarningsBasicLevels) :
    CriticalMessageState()
