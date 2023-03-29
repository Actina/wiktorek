package pl.gov.mf.mobile.networking.api.jwt

import com.google.gson.annotations.SerializedName
import pl.gov.mf.mobile.utils.JsonConvertible
import java.security.PrivateKey

interface JwtTokenGenerator {

    fun generate(
        applicationId: String,
        privateKey: PrivateKey
    ): JwtToken
}

data class JwtToken(
    @SerializedName("token") val token: String,
    @SerializedName("appId") val aplicationId: String,
    @SerializedName("date") val date: String
) : JsonConvertible