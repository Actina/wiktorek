package pl.gov.mf.etoll.core.ridehistory.model

import pl.gov.mf.etoll.commons.TimeUtils
import pl.gov.mf.mobile.utils.JsonConvertible
import pl.gov.mf.mobile.utils.toObject
import java.util.*

data class RideHistoryDataItem(
    val id: Int = 0,
    val historyItemType: HistoryItemType,
    val historyItemFrameType: HistoryItemFrameType,
    val timestamp: Date = Date(),
    val additionalData: String = "{}"
) : JsonConvertible {

    constructor(
        historyItemType: HistoryItemType,
        frame: HistoryItemFrameType,
        timestamp: Date = Date(),
        additionalData: ActivityAdditionalData
    ) : this(
        historyItemType = historyItemType,
        historyItemFrameType = frame,
        timestamp = timestamp,
        additionalData = additionalData.toJSON()
    )

    inline fun <reified T : ActivityAdditionalData> additionalData(): T? = when (historyItemType) {
        HistoryItemType.TRAILER_WEIGHT_NOT_EXCEEDED -> null
        else -> additionalData.toObject()
    }
}

fun RideHistoryDataItem.formattedDate(): String =
    TimeUtils.RideHistoryDateFormatter.print(timestamp.time)

fun RideHistoryDataItem.formattedTime(): String =
    TimeUtils.RideHistoryTimeFormatter.print(timestamp.time)