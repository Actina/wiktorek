package pl.gov.mf.etoll.networking.api.model

import com.google.gson.annotations.SerializedName

data class ApiLastPositionsSentResponse(
    @SerializedName("applicationLastPosition") val applicationLastPosition: ApplicationLastPosition,
    @SerializedName("zslLastPosition") val zslLastPosition: ZslLastPosition?
)

data class ApplicationLastPosition(
    @SerializedName("applicationId") val applicationId: String,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("dateTimestamp") val dateTimestamp: Long
)

data class ZslLastPosition(
    @SerializedName("zslBussinessId") val zslBussinessId: String,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("dateTimestamp") val dateTimestamp: Long
)