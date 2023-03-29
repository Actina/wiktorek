package pl.gov.mf.etoll.core.model

import com.google.gson.annotations.SerializedName
import pl.gov.mf.etoll.networking.api.model.*
import pl.gov.mf.mobile.utils.JsonConvertible

data class CoreSentMap(
    // when SENT_LIST from shared prefs is empty, data field is null. Is this possible in real case?
    @SerializedName("data") val data: Map<String, MutableList<CoreSent>>?
) : JsonConvertible

data class CoreSent(
    @SerializedName("sentNumber") val sentNumber: String,
    @SerializedName("startTimestamp") val startTimestamp: Int,
    @SerializedName("endTimestamp") val endTimestamp: Int,
    @SerializedName("sentStatus") val sentStatus: String?,
    @SerializedName("trailer") val trailer: CoreSentTrailer?,
    @SerializedName("vehicle") val vehicle: CoreSentVehicle?,
    @SerializedName("trailerNumber") val trailerNumber: String?,
    @SerializedName("truckNumber") val truckNumber: String?,
    @SerializedName("deliveryAddress") val deliveryAddress: CoreSentAddress,
    @SerializedName("loadingAddress") val loadingAddress: CoreSentAddress
) : JsonConvertible

data class CoreSentTrailer(
    @SerializedName("licensePlateNumber") val licensePlateNumber: String?
)

data class CoreSentVehicle(
    @SerializedName("failoverGeolocator") val failoverGeolocator: CoreSentFailoverGeolocator?,
    @SerializedName("geolocator") val geolocator: CoreSentGeolocator?,
    @SerializedName("licensePlateNumber") val licensePlateNumber: String
)

data class CoreSentFailoverGeolocator(
    @SerializedName("number") val number: String,
    @SerializedName("obeType") val obeType: String
)

data class CoreSentGeolocator(
    @SerializedName("number") val number: String,
    @SerializedName("obeType") val obeType: String
)

data class CoreSentAddress(
    @SerializedName("country") val country: String,
    @SerializedName("postalCode") val postalCode: String,
    @SerializedName("city") val city: String,
    @SerializedName("street") val street: String,
    @SerializedName("houseNumber") val houseNumber: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
) : JsonConvertible

fun ApiSentItem.toCoreModel(): CoreSent = CoreSent(
    sentNumber,
    startTimestamp,
    endTimestamp,
    sentStatus,
    trailer?.toCoreModel(),
    vehicle?.toCoreModel(),
    trailerNumber,
    truckNumber,
    deliveryAddress.toCoreModel(),
    loadingAddress.toCoreModel()
)

private fun ApiSentVehicle.toCoreModel(): CoreSentVehicle = CoreSentVehicle(
    failoverGeolocator?.toCoreModel(),
    geolocator?.toCoreModel(),
    licensePlateNumber
)

private fun ApiSentFailoverGeolocator.toCoreModel(): CoreSentFailoverGeolocator =
    CoreSentFailoverGeolocator(number, obeType)

private fun ApiSentGeolocator.toCoreModel(): CoreSentGeolocator =
    CoreSentGeolocator(number, obeType)

private fun ApiSentTrailer.toCoreModel(): CoreSentTrailer? = CoreSentTrailer(licensePlateNumber)

private fun DeliveryAddress.toCoreModel(): CoreSentAddress =
    CoreSentAddress(country, postalCode, city, street, houseNumber, latitude, longitude)

private fun LoadingAddress.toCoreModel(): CoreSentAddress =
    CoreSentAddress(country, postalCode, city, street, houseNumber, latitude, longitude)