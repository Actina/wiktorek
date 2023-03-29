package pl.gov.mf.etoll.core.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import pl.gov.mf.mobile.utils.JsonConvertible

data class CoreStatus(
    @SerializedName("applicationId") val applicationId: String,
    @SerializedName("dateTimestamp") val dateTimestamp: Long = 0,
    @SerializedName("vehicles") val vehicles: Array<CoreVehicle>,
    @SerializedName("transit") val configuration: CoreConfiguration,
    @SerializedName("messageIds") val messageIds: Array<String>,
    @SerializedName("sentActivated") val sentActivated: Boolean,
    @SerializedName("crmActivated") val crmActivated: Boolean,
    @SerializedName("eventStream") val eventStreamConfiguration: CoreEventStreamConfiguration
) : JsonConvertible {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CoreStatus

        if (applicationId != other.applicationId) return false
        if (dateTimestamp != other.dateTimestamp) return false
        if (!vehicles.contentEquals(other.vehicles)) return false
        if (configuration != other.configuration) return false
        if (!messageIds.contentEquals(other.messageIds)) return false
        if (sentActivated != other.sentActivated) return false
        if (crmActivated != other.crmActivated) return false
        if (eventStreamConfiguration != other.eventStreamConfiguration) return false

        return true
    }

    override fun hashCode(): Int {
        var result = applicationId.hashCode()
        result = 31 * result + dateTimestamp.hashCode()
        result = 31 * result + vehicles.contentHashCode()
        result = 31 * result + configuration.hashCode()
        result = 31 * result + messageIds.contentHashCode()
        result = 31 * result + sentActivated.hashCode()
        result = 31 * result + crmActivated.hashCode()
        result = 31 * result + eventStreamConfiguration.hashCode()
        return result
    }
}

data class CoreVehicle(
    @SerializedName("id") val id: Long,
    @SerializedName("licensePlateNumber") val licensePlate: String,
    @SerializedName("brand") val brand: String,
    @SerializedName("model") val model: String,
    @SerializedName("emissionClass") val emissionClass: String,
    @SerializedName("category") val category: Int,
    @SerializedName("categoryCanBeIncreased") val categoryCanBeIncreased: Boolean,
    @SerializedName("zslIsPrimaryGeolocator") val zslIsPrimaryGeolocator: Boolean,
    @SerializedName("categoryPlateVerification") val categoryPlateVerification: Boolean,
    @SerializedName("billingAccount") var accountInfo: CoreAccountInfo,
    @SerializedName("geolocator") val geolocator: CoreGeolocator?,
    @SerializedName("lowTopUpFlag") val lowTopUpFlag: Boolean = true,
    @SerializedName("tollCategoryIncreaseFlag") val tollCategoryIncreaseFlag: Boolean = false
) : JsonConvertible


data class CoreAccountInfo(
    @SerializedName("id") val id: Long,
    @SerializedName("alias") val alias: String,
    @SerializedName("type") val type: String,
    @SerializedName("balanceInfo") val balance: CoreBalance?
)

data class CoreConfiguration(
    @SerializedName("samplingRate") val sendingSamplingRateInSeconds: Int,
    @SerializedName("collectionRate") val collectionSamplingRateInSeconds: Int,
    @SerializedName("sentSamplingRate") val sentSamplingRate: Int = 60,
    @SerializedName("sentCollectionRate") val sentCollectionRate: Int = 25,
    @SerializedName("isSentAlgorithmUsed") val sentAlgorithmUsed: Boolean = false
) : JsonConvertible

data class CoreBalance(
    @SerializedName("priority") val priority: String,
    @SerializedName("updateAtTimestamp") val updateAtTimestamp: Long = 0,
    @SerializedName("value") val value: Double,
    @SerializedName("isInitialized") val isInitialized: Boolean
)

data class CoreGeolocator(
    @SerializedName("number") val number: String,
    @SerializedName("obeType") val obeType: String
)

enum class CoreMonitoringDeviceType(val apiName: String) {
    @Keep
    ZSL("zsl"),

    @Keep
    APP("mobile");

    companion object {
        fun getStringByTrackByAppState(monitoringByApp: Boolean) =
            (if (monitoringByApp) APP else ZSL).apiName
    }
}

enum class CoreVehicleCategory(val value: Int, val uiLiteral: String) {
    @Keep
    MOTORCYCLE(0, "vehicle_category_light"),

    @Keep
    LIGHT(13, "vehicle_category_light"),

    @Keep
    LIGHT_MEDIUM(14, "vehicle_category_light_medium"),

    @Keep
    BUS(30, "vehicle_category_bus"),

    @Keep
    MEDIUM(41, "vehicle_category_medium"),

    @Keep
    MEDIUM_HEAVY(42, "vehicle_category_medium_heavy"),

    @Keep
    HEAVY(50, "vehicle_category_heavy");

    companion object {
        fun fromInt(v: Int): CoreVehicleCategory {
            return values().firstOrNull { it.value == v } ?: LIGHT
        }
    }
}

enum class CoreAccountPriorityType(val priority: String) {
    @Keep
    ZERO_BALANCE("zerobalance"),

    @Keep
    LOW_BALANCE("lowbalance"),

    @Keep
    FAULT("fault"),

    @Keep
    WARNING("warning"),

    @Keep
    INFO("info"),

    @Keep
    UNKNOWN("unknown");

    companion object {
        fun fromString(priority: String?): CoreAccountPriorityType {
            for (priorityTypeEnum in values()) {
                if (priorityTypeEnum.priority == priority) {
                    return priorityTypeEnum
                }
            }
            return UNKNOWN
        }
    }
}

enum class CoreAccountTypes(val literal: String, val uiLiteral: String) {
    @Keep
    NONE("", ""),

    @Keep
    PREPAID("prepaid", "dashboard_ride_control_account_type_prepaid"),

    @Keep
    POSTPAID("postpaid", "dashboard_ride_control_account_type_postpaid");

    companion object {
        fun fromLiteral(literal: String?): CoreAccountTypes {
            if (literal == null) {
                return NONE
            } else {
                for (type in values())
                    if (type.literal.uppercase().contentEquals(literal.uppercase()))
                        return type
                return POSTPAID
            }
        }

        fun toUiLiteral(accountType: String): String = when (fromLiteral(accountType)) {
            PREPAID -> PREPAID.uiLiteral
            POSTPAID -> POSTPAID.uiLiteral
            else -> NONE.uiLiteral
        }
    }
}
