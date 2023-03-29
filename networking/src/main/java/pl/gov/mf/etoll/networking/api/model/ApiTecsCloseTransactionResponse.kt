package pl.gov.mf.etoll.networking.api.model

import com.google.gson.annotations.SerializedName

data class ApiTecsCloseTransactionResponse(
    @SerializedName("status") val status: String,
    @SerializedName("validationErrorHeader") val errorHeader: String?,
    @SerializedName("validationErrorContent") val errorContent: String?
)

enum class ApiTecsCloseTransactionResponseStatus(val apiName: String) {
    SUCCESS("Success"), FAILURE("Failure"), CANCELLED("Cancelled");

    companion object {
        fun fromString(value: String): ApiTecsCloseTransactionResponseStatus? {
            values().forEach {
                if (it.apiName.uppercase().contentEquals(value.uppercase()))
                    return it
            }
            return null
        }
    }
}
