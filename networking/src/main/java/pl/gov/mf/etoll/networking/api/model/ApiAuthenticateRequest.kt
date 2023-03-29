package pl.gov.mf.etoll.networking.api.model

import com.google.gson.annotations.SerializedName

data class ApiAuthenticateRequest(
    @SerializedName("applicationId") val applicationId: String,
    @SerializedName("date") val date: String
)
