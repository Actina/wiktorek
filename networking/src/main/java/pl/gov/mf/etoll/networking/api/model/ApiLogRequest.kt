package pl.gov.mf.etoll.networking.api.model

import com.google.gson.annotations.SerializedName

data class ApiLogRequest(
    @SerializedName("logs") val logs: List<ApiLog>
)

data class ApiLog(
    @SerializedName("date") val date: String,
    @SerializedName("tag") val tag: String,
    @SerializedName("details") val details: String
)