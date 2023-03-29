package pl.gov.mf.etoll.core.model

import com.google.gson.annotations.SerializedName

data class CoreLastPositionsSent(
    @SerializedName("applicationLastPosition") val applicationLastPosition: CoreApplicationLastPosition,
    @SerializedName("zslLastPosition") val zslLastPosition: CoreZslLastPosition?
)

data class CoreApplicationLastPosition(
    @SerializedName("applicationId") val applicationId: String,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("dateTimestamp") val dateTimestamp: Long
)

data class CoreZslLastPosition(
    @SerializedName("zslBussinessId") val zslBussinessId: String,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("dateTimestamp") val dateTimestamp: Long
)