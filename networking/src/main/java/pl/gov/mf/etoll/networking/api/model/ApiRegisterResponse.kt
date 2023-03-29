package pl.gov.mf.etoll.networking.api.model

import com.google.gson.annotations.SerializedName

data class ApiRegisterResponse(@SerializedName("applicationId") val refreshToken: String)