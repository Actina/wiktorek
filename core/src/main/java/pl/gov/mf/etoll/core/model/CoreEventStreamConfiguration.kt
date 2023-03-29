package pl.gov.mf.etoll.core.model

import com.google.gson.annotations.SerializedName
import pl.gov.mf.mobile.utils.JsonConvertible

data class CoreEventStreamConfiguration(
    @SerializedName("address") val address: String,
    @SerializedName("authorizationHeader") val authorizationHeader: String
) : JsonConvertible