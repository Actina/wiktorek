package pl.gov.mf.etoll.networking.api.model

import com.google.gson.annotations.SerializedName
import pl.gov.mf.mobile.utils.BatteryUtils
import pl.gov.mf.mobile.utils.JsonConvertible

data class EventStreamObject(
    @SerializedName("sentDate") val sentDate: String,
    @SerializedName("events") val events: List<EventStreamBaseEvent>
) : JsonConvertible

data class EventStreamLocation(
    @SerializedName("type") val type: String = "spoekas",
    @SerializedName("targetSystem") var targetSystem: Int,
    @SerializedName("dataId") var dataId: String,
    @SerializedName("serialNumber") var serialNumber: String,
    @SerializedName("latitude") var latitude: Double,
    @SerializedName("longitude") var longitude: Double,
    @SerializedName("altitude") var altitude: Double,
    @SerializedName("fixTimeEpoch") var fixTime: Long,
    @SerializedName("gpsSpeed") var gpsSpeed: Double,
    @SerializedName("accuracy") var accuracy: Double,
    @SerializedName("gpsHeading") var gpsHeading: Double,
    @SerializedName("eventType") var eventType: String,
    @SerializedName("lac") var lac: String = "000",
    @SerializedName("mcc") var mcc: String = "000",
    @SerializedName("mnc") var mnc: String = "000",
    @SerializedName("mobileCellId") var mobileCellId: String = "0",
    @SerializedName("dataFromHardwareGps") var dataFromHardwareGps: Boolean,
    @SerializedName("foregroundMode") var appInForeground: Boolean,
    @SerializedName("internetAvailable") var internetAvailable: Boolean = false,
    @SerializedName("batteryLevel") var batteryLevel: Int = BatteryUtils.BATTERY_UNKNOWN,
    @SerializedName("trackingDevice") var trackingDevice: String? = null
) : EventStreamBaseEvent()

data class EventStreamLocationWithoutLocation(
    @SerializedName("type") var type: String = "spoekas",
    @SerializedName("targetSystem") var targetSystem: Int,
    @SerializedName("dataId") var dataId: String,
    @SerializedName("serialNumber") var serialNumber: String,
    @SerializedName("fixTimeEpoch") var fixTime: Long,
    @SerializedName("eventType") var eventType: String,
    @SerializedName("lac") var lac: String = "000",
    @SerializedName("mcc") var mcc: String = "000",
    @SerializedName("mnc") var mnc: String = "000",
    @SerializedName("mobileCellId") var mobileCellId: String = "0",
    @SerializedName("foregroundMode") var appInForeground: Boolean,
    @SerializedName("internetAvailable") var internetAvailable: Boolean = false,
    @SerializedName("batteryLevel") var batteryLevel: Int = BatteryUtils.BATTERY_UNKNOWN,
    @SerializedName("trackingDevice") var trackingDevice: String? = null
) : EventStreamBaseEvent()

data class EventStreamActivateVehicle(
    @SerializedName("type") var type: String = "activateVehicle",
    @SerializedName("vehicleId") var vehicleId: Long,
    @SerializedName("fixTimeEpoch") var fixTime: Long,
    @SerializedName("billingAccountId") var billingAccountId: Long,
    @SerializedName("tollCategoryIncreaseFlag") var tollCategoryIncreaseFlag: Boolean = false,
    @SerializedName("trackingDevice") var trackingDevice: String
) : EventStreamBaseEvent()

data class EventStreamStartSent(
    @SerializedName("type") var type: String = "startSent",
    @SerializedName("latitude") var latitude: Double?,
    @SerializedName("longitude") var longitude: Double?,
    @SerializedName("fixTimeEpoch") var fixTime: Long,
    @SerializedName("sentNumber") var sentNumber: String?,
    @SerializedName("dataFromHardwareGps") var dataFromHardwareGps: Boolean? = null,
    @SerializedName("foregroundMode") var appInForeground: Boolean
) : EventStreamBaseEvent()

data class EventStreamStopSent(
    @SerializedName("type") var type: String = "stopSent",
    @SerializedName("latitude") var latitude: Double?,
    @SerializedName("longitude") var longitude: Double?,
    @SerializedName("fixTimeEpoch") var fixTime: Long,
    @SerializedName("sentNumber") var sentNumber: String?,
    @SerializedName("dataFromHardwareGps") var dataFromHardwareGps: Boolean? = null,
    @SerializedName("foregroundMode") var appInForeground: Boolean
) : EventStreamBaseEvent()

data class EventStreamCancelSent(
    @SerializedName("type") var type: String = "cancelSent",
    @SerializedName("fixTimeEpoch") var fixTime: Long,
    @SerializedName("sentNumber") var sentNumber: String
) : EventStreamBaseEvent()

data class EventStreamChangeTrackingDevice(
    @SerializedName("type") val type: String = "changetrackingdevice",
    @SerializedName("trackingDevice") var trackingDevice: String,
    @SerializedName("fixTimeEpoch") var fixTime: Long
) : EventStreamBaseEvent()

data class EventStreamResumeApplication(
    @SerializedName("type") val type: String = "applicationResume",
    @SerializedName("fixTimeEpoch") var fixTime: Long,
    @SerializedName("applicationTerminationTime") var terminationTimeInMsecs: Long,
    @SerializedName("hardRestart") var hardRestart: Boolean
) : EventStreamBaseEvent()

open class EventStreamBaseEvent : JsonConvertible
