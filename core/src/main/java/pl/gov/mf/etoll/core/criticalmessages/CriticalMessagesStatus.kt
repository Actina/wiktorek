package pl.gov.mf.etoll.core.criticalmessages

import pl.gov.mf.etoll.interfaces.*

data class CriticalMessagesStatus(
    val batteryState: WarningsBasicLevels = WarningsBasicLevels.UNKNOWN,
    val gpsState: WarningsBasicLevels = WarningsBasicLevels.UNKNOWN,
    val dataConnectionState: WarningsBasicLevels = WarningsBasicLevels.UNKNOWN
) {

    fun toCriticalMessagesTypeList(): ArrayList<CriticalMessageState> =
        arrayListOf(
            BatteryState(batteryState),
            GpsState(gpsState),
            DataConnectionState(dataConnectionState)
        )
}