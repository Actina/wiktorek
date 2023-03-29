package pl.gov.mf.etoll.front.ridedetails.sentselection

import pl.gov.mf.etoll.commons.TimeUtils
import pl.gov.mf.etoll.core.model.CoreSent
import pl.gov.mf.mobile.utils.JsonConvertible

data class RideDetailsSentData(
    val item: CoreSent,
    var enabled: Boolean = true,
    var checked: Boolean = false,
    var active: Boolean = false,
) : JsonConvertible {
    var group: String = ""

    fun sentDates(): String {
        val start = TimeUtils.DefaultDateFormatterForUi.print(item.startTimestamp.toLong() * 1000L)
        val end = TimeUtils.DefaultDateFormatterForUi.print(item.endTimestamp.toLong() * 1000L)

        return "$start - $end"
    }
}
