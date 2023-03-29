package pl.gov.mf.etoll.networking.api.model

import com.google.gson.annotations.SerializedName

data class ApiTecsOpenTransactionResponse(
    @SerializedName("status") val status: String,
    @SerializedName("address") val url: String?,
    @SerializedName("validationErrorHeader") val errorHeader: String?,
    @SerializedName("validationErrorContent") val errorContent: String?,
    @SerializedName("validationError") val validationError: String?
)

enum class ApiTecsOpenTransactionResponseStatus(val apiName: String) {
    SUCCESS("Success"), FAILURE("Failure"), CANCELLED("Cancelled ");

    companion object {
        fun fromString(value: String): ApiTecsOpenTransactionResponseStatus? {
            values().forEach {
                if (it.apiName.uppercase().contentEquals(value.uppercase()))
                    return it
            }
            return null
        }
    }
}

enum class ApiTecsOpenTransactionResponseError(val validationError: String) {
    INCORRECT_MAX_TOP_UP_AMOUNT("incorrectMaxTopUpAmount"),
    INCORRECT_MIN_TOP_UP_AMOUNT("incorrectMinTopUpAmount"),
    INCORRECT_MAX_ACCOUNT_BALANCE("incorrectMaxAccountBalance"),
    NOT_FOUND_OR_UNKNOWN("");

    companion object {
        fun fromString(value: String?): ApiTecsOpenTransactionResponseError = if (value == null) NOT_FOUND_OR_UNKNOWN
        else {
            values().forEach {
                if (it.validationError.uppercase().contentEquals(value.uppercase()))
                    return it
            }
            NOT_FOUND_OR_UNKNOWN
        }
    }
}