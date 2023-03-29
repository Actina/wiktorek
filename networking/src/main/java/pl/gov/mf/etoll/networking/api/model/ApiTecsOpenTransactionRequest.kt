package pl.gov.mf.etoll.networking.api.model

import com.google.gson.annotations.SerializedName

data class ApiTecsOpenTransactionRequest(
    @SerializedName("billingAccountId") val billingAccountId: Long,
    @SerializedName("amount") val amount: String,
    @SerializedName("returnUrl") val returnUrl: String,
    @SerializedName("category") val category: Int
)