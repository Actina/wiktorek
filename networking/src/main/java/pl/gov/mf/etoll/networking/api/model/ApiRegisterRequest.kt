package pl.gov.mf.etoll.networking.api.model

import com.google.gson.annotations.SerializedName
import pl.gov.mf.etoll.networking.BuildConfig

data class ApiRegisterRequest(
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("publicKey") val publicKeyInPem: String,
    @SerializedName("encryptedFirebaseUserId") val firebaseUserId: String,
    @SerializedName("assignVehicleManually") val assignVehicleManually: Boolean = !BuildConfig.DEBUG
)