package pl.gov.mf.etoll.front.configsentridesselection

import pl.gov.mf.etoll.commons.TimeUtils
import pl.gov.mf.etoll.core.model.CoreSent
import pl.gov.mf.mobile.utils.JsonConvertible

data class SentRideData(
    val item: CoreSent,
    var enabled: Boolean,
    var checked: Boolean = false,
) : JsonConvertible {
    fun formattedStartDate() =
        TimeUtils.DefaultDateFormatterForUi.print(item.startTimestamp.toLong() * 1000L)

    fun formattedEndDate() =
        TimeUtils.DefaultDateFormatterForUi.print(item.endTimestamp.toLong() * 1000L)

    fun loadingAddress() = "${item.loadingAddress.city}, ${item.loadingAddress.country}"
    fun deliveryAddress() = "${item.deliveryAddress.city}, ${item.deliveryAddress.country}"

}