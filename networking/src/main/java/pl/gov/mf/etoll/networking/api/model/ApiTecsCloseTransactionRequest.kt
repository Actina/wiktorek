package pl.gov.mf.etoll.networking.api.model

import com.google.gson.annotations.SerializedName

class ApiTecsCloseTransactionRequest(
    @SerializedName("txId") val txId: Long,
    @SerializedName("sign") val sign: String?,
    @SerializedName("responseCode") val responseCode: Long,
    @SerializedName("responseText") val responseText: String?,
    @SerializedName("cardReferenceNumber") val cardRefNumber: String?,
    @SerializedName("userData") val userData: String?,
    @SerializedName("cardType") val cardType: String?,
    @SerializedName("authorizationNumber") val authorizationNumber: String?,
    @SerializedName("stan") val stan: String?,
    @SerializedName("informationDate") val informationDate: String?,
    @SerializedName("AcquirerName") val acquirerName: String?,
    @SerializedName("OperatorId") val operatorId: String?
)