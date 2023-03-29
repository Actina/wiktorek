package pl.gov.mf.etoll.front.configsentridesselection.details

import pl.gov.mf.etoll.commons.TimeUtils
import pl.gov.mf.etoll.core.model.CoreSent
import pl.gov.mf.mobile.utils.JsonConvertible

data class SentMapDetailsData(
    val item: CoreSent,
    var enabled: Boolean,
    var checked: Boolean = false,
    var showConfirmDialog: Boolean = false,
    var showRawDataForRideSummary: Boolean = false
) : JsonConvertible {
    val untranslatedScreenTitle: String
        get() = if (showRawDataForRideSummary) "sent_ride_details_summary_title" else "sent_ride_details_config_title"

    fun formattedStartDate() =
        TimeUtils.DefaultDateFormatterForUi.print(item.startTimestamp.toLong() * 1000L)

    fun formattedEndDate() =
        TimeUtils.DefaultDateFormatterForUi.print(item.endTimestamp.toLong() * 1000L)

    fun loadingAddress() = "${item.loadingAddress.city}, ${item.loadingAddress.country}"
    fun deliveryAddress() = "${item.deliveryAddress.city}, ${item.deliveryAddress.country}"

    fun confirmButtonIsVisible() = enabled && !checked && !showRawDataForRideSummary
    fun backButtonIsVisible() = !showRawDataForRideSummary
    fun bottomSpaceIsVisible() = showRawDataForRideSummary
}