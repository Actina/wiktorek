package pl.gov.mf.etoll.core.ridehistory.model

import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorV3Callbacks.RideType
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorV3Callbacks.RideType.SENT
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorV3Callbacks.RideType.TOLLED
import pl.gov.mf.etoll.core.ridehistory.model.HistoryItemType.RIDE_END
import pl.gov.mf.etoll.core.ridehistory.model.HistoryItemType.RIDE_START

enum class HistoryItemType(val title: String, val alternativeTitle: String = "") {
    RIDE_START("ride_history_toll_ride_header", "ride_history_sent_header_android"),
    RIDE_END("ride_history_toll_ride_header", "ride_history_sent_header_android"),
    SENT_START("ride_history_sent_header_android"),
    SENT_END("ride_history_sent_header_android"),
    SENT_CANCEL("ride_history_sent_header_android"),
    MONITORING_DEVICE_CHANGED("ride_history_monitoring_device_header"),
    TRAILER_WEIGHT_EXCEEDED("ride_history_toll_ride_header"),
    TRAILER_WEIGHT_NOT_EXCEEDED("ride_history_toll_ride_header"),
    PRE_PAID_TOP_UP("ride_history_top_up_amount_header"),
    BATTERY_LOW("ride_history_battery_quality_low_header"),
    BATTERY_GOOD("ride_history_battery_quality_good_header"),
    GPS_LOW("ride_history_gps_quality_low_header"),
    GPS_GOOD("ride_history_gps_quality_good_header"),
    INTERNET_LOW("ride_history_internet_quality_low_header"),
    INTERNET_GOOD("ride_history_internet_quality_good_header"),
    FAKE_GPS_DETECTED("dialog_fakegps_title_android")
}

fun HistoryItemType.getTitle(rideType: RideType? = null): String =
    rideType?.let {
        when (this) {
            RIDE_START,
            RIDE_END -> when (rideType) {
                TOLLED -> title
                SENT -> alternativeTitle
            }
            else -> title
        }
    } ?: title