package pl.gov.mf.etoll.networking.api.model

import com.google.gson.annotations.SerializedName
import pl.gov.mf.etoll.commons.TimeUtils
import pl.gov.mf.mobile.utils.JsonConvertible

data class ApiStatusResponse(
    @SerializedName("applicationId") val applicationId: String,
    @SerializedName("dateTimestamp") val dateTimestamp: Long,
    @SerializedName("vehicles") val vehicles: Array<ApiModelVehicle>,
    @SerializedName("transit") val transit: Transit,
    @SerializedName("messageIds") val messageIds: Array<String>,
    @SerializedName("sentActivated") val sentActivated: Boolean,
    @SerializedName("crmActivated") val crmActivated: Boolean,
    @SerializedName("eventStream") val eventStream: ApiEventStreamConfiguration
) : JsonConvertible {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ApiStatusResponse
        if (applicationId != other.applicationId) return false
        if (dateTimestamp != other.dateTimestamp) return false
        if (sentActivated != other.sentActivated) return false
        if (!vehicles.contentEquals(other.vehicles)) return false
        if (transit != other.transit) return false
        if (!messageIds.contentEquals(other.messageIds)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = applicationId.hashCode()
        result = 31 * result + dateTimestamp.hashCode()
        result = 31 * result + vehicles.contentHashCode()
        result = 31 * result + transit.hashCode()
        result = 31 * result + messageIds.contentHashCode()
        return result
    }
}

data class ApiModelVehicle(
    @SerializedName("id") val id: Long,
    @SerializedName("licensePlateNumber") val licensePlate: String,
    @SerializedName("brand") val brand: String,
    @SerializedName("model") val model: String,
    @SerializedName("emissionClass") val emissionClass: String,
    @SerializedName("category") val category: Int,
    @SerializedName("categoryCanBeIncreased") val categoryCanBeIncreased: Boolean,
    @SerializedName("zslIsPrimaryGeolocator") val zslIsPrimaryGeolocator: Boolean,
    @SerializedName("categoryPlateVerification") val categoryPlateVerification: Boolean,
    @SerializedName("billingAccount") val accountBalance: ApiModelAccountInfo,
    @SerializedName("geolocator") val geolocator: ApiModelGeolocator?,
    @SerializedName("lowTopUpFlag") val lowTopUpFlag: Boolean = true,
    @SerializedName("tollCategoryIncreaseFlag") val tollCategoryIncreaseFlag: Boolean
) : JsonConvertible

data class ApiModelAccountInfo(
    @SerializedName("id") val id: Long,
    @SerializedName("alias") val alias: String,
    @SerializedName("type") val type: String,
    @SerializedName("balanceInfo") val balance: BalanceInfo?
) : JsonConvertible

data class BalanceInfo(
    @SerializedName("priority") val priority: String,
    @SerializedName("updateAtTimestamp") val updateAtTimestamp: Long,
    @SerializedName("value") val value: Double,
    @SerializedName("isInitialized") val isInitialized: Boolean
) : JsonConvertible

data class Transit(
    @SerializedName("samplingRate") val samplingRate: Int,
    @SerializedName("collectionRate") val collectionRate: Int,
    @SerializedName("sentSamplingRate") val sentSamplingRate: Int,
    @SerializedName("sentCollectionRate") val sentCollectionRate: Int,
    @SerializedName("isSentAlgorithmUsed") val sentAlgorithmUsed: Boolean
) : JsonConvertible

data class ApiModelGeolocator(
    @SerializedName("number") val number: String,
    @SerializedName("obeType") val obeType: String
) : JsonConvertible

data class ApiEventStreamConfiguration(
    @SerializedName("address") val address: String,
    @SerializedName("authorizationHeader") val authorizationHeader: String
) : JsonConvertible {
    val formattedAddress: String
        get() {
            if (address.isEmpty()) return TimeUtils.EMPTY_STRING
            return address.substring(0, address.length - "messages".length)
        }
}