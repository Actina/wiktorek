package pl.gov.mf.etoll.networking.api.model

import com.google.gson.annotations.SerializedName

data class ApiSentResponse(
    @SerializedName("data") val data: Map<String, List<ApiSentItem>>
)

data class ApiSentItem(
    @SerializedName("sentNumber") val sentNumber: String,
    @SerializedName("startTimestamp") val startTimestamp: Int,
    @SerializedName("endTimestamp") val endTimestamp: Int,
    @SerializedName("sentStatus") val sentStatus: String?,
    @SerializedName("trailer") val trailer: ApiSentTrailer?,
    @SerializedName("vehicle") val vehicle: ApiSentVehicle?,
    @SerializedName("trailerNumber") val trailerNumber: String?,
    @SerializedName("truckNumber") val truckNumber: String?,
    @SerializedName("deliveryAddress") val deliveryAddress: DeliveryAddress,
    @SerializedName("loadingAddress") val loadingAddress: LoadingAddress
)

data class ApiSentTrailer(
    @SerializedName("licensePlateNumber") val licensePlateNumber: String?
)

data class ApiSentVehicle(
    @SerializedName("failoverGeolocator") val failoverGeolocator: ApiSentFailoverGeolocator?,
    @SerializedName("geolocator") val geolocator: ApiSentGeolocator?,
    @SerializedName("licensePlateNumber") val licensePlateNumber: String
)

data class ApiSentFailoverGeolocator(
    @SerializedName("number") val number: String,
    @SerializedName("obeType") val obeType: String
)

data class ApiSentGeolocator(
    @SerializedName("number") val number: String,
    @SerializedName("obeType") val obeType: String
)

data class DeliveryAddress(
    @SerializedName("country") val country: String,
    @SerializedName("postalCode") val postalCode: String,
    @SerializedName("city") val city: String,
    @SerializedName("street") val street: String,
    @SerializedName("houseNumber") val houseNumber: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
)

data class LoadingAddress(
    @SerializedName("country") val country: String,
    @SerializedName("postalCode") val postalCode: String,
    @SerializedName("city") val city: String,
    @SerializedName("street") val street: String,
    @SerializedName("houseNumber") val houseNumber: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
)