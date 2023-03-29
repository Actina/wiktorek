package pl.gov.mf.etoll.storage.database.notifications.model

import com.google.gson.annotations.SerializedName
import pl.gov.mf.etoll.translations.Translatable
import pl.gov.mf.mobile.utils.JsonConvertible

data class NotificationHistoryItemModel(
    val id: Long? = -1,
    val notificationHistoryItemType: NotificationHistoryItemType,
    val title: String,
    val content: String,
    val timestamp: Long,
    val iconResource: Int? = null,
    val payload: NotificationHistoryItemPayloadModel? = null,
    val contentExtraValue: String? = null,
    val apiMessageId: String = ""
)

data class NotificationHistoryItemPayloadModel(
    @SerializedName("contents") val contents: Translatable?,
    @SerializedName("headers") val headers: Translatable?,
    @SerializedName("title") val title: Translatable?,
    @SerializedName("sendDate") val date: String,
    @SerializedName("type") var backofficeMessageType: String?
) : JsonConvertible