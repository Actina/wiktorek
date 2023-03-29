package pl.gov.mf.etoll.networking.api.model

import com.google.gson.annotations.SerializedName
import pl.gov.mf.mobile.utils.JsonConvertible

data class TecsTransactionResult(
    @SerializedName("responsecode") val responseCode: String?,
    @SerializedName("SVC") val svc: String?,
    @SerializedName("Authorization-number") val authorizationNumber: String?,
    @SerializedName("AcquirerName") val acquirerName: String?,
    @SerializedName("VU-NUMMER") val vuNummer: String?,
    @SerializedName("STAN") val stan: String?,
    @SerializedName("sign") val sign: String?,
    @SerializedName("txid") val txid: String?,
    @SerializedName("mid") val mid: String?,
    @SerializedName("Orig-TX-ID") val origTxtId: String?,
    @SerializedName("Orig-STAN") val origStan: String?,
    @SerializedName("User-Data") val userData: String?,
    @SerializedName("CardReferenceNumber") val cardRefNumber: String?,
    @SerializedName("CardType") val cardType: String?,
    @SerializedName("responsetext") val responseText: String?,
    @SerializedName("Operator") val operator: String?,
    @SerializedName("Date-Time-TX") val dateTime: String?,
    @SerializedName("Operator-ID") val operatorId: String?
) : JsonConvertible {
    companion object {
        fun from(map: Map<String, String>): TecsTransactionResult {
            return TecsTransactionResult(
                map["responsecode"],
                map["SVC"],
                map["Authorization-number"],
                map["AcquirerName"],
                map["VU-NUMMER"],
                map["STAN"],
                map["sign"],
                map["txid"],
                map["mid"],
                map["Orig-TX-ID"],
                map["Orig-STAN"],
                map["User-Data"],
                map["CardReferenceNumber"],
                map["CardType"],
                map["responsetext"],
                map["Operator"],
                map["Date-Time-TX"],
                map["Operator-ID"]
            )
        }
    }
}