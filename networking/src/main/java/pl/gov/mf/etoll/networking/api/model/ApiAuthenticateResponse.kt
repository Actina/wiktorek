package pl.gov.mf.etoll.networking.api.model

import com.google.gson.annotations.SerializedName

data class ApiAuthenticateResponse(@SerializedName("accessToken") val accessToken: String)
