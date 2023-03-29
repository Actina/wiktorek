package pl.gov.mf.etoll.core.model

import com.google.gson.annotations.SerializedName
import pl.gov.mf.etoll.translations.Translatable
import pl.gov.mf.mobile.utils.JsonConvertible


data class CoreMessage(
    @SerializedName("messageId") val messageId: String,
    @SerializedName("contents") val contents: Translatable?,
    @SerializedName("headers") val headers: Translatable?,
    @SerializedName("sendDateTimestamp") val sendDateTimestamp: Long = 0,
    @SerializedName("type") val type: String
) : JsonConvertible