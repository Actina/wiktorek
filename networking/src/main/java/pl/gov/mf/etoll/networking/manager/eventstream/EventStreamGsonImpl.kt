package pl.gov.mf.etoll.networking.manager.eventstream

import com.google.gson.*
import pl.gov.mf.etoll.commons.CurrencyUtils.format
import pl.gov.mf.etoll.networking.api.model.EventStreamLocation
import pl.gov.mf.etoll.networking.api.model.EventStreamStartSent
import pl.gov.mf.etoll.networking.api.model.EventStreamStopSent
import java.lang.reflect.Type
import javax.inject.Inject


class EventStreamGsonImpl @Inject constructor() : EventStreamGson {
    override fun getGson(): Gson =
        GsonBuilder()
            .setLenient()
            .registerTypeAdapter(
                EventStreamLocation::class.java, EventStreamLocationSerializer()
            )
            .registerTypeAdapter(
                EventStreamStartSent::class.java, EventStreamStartSentSerializer()
            )
            .registerTypeAdapter(
                EventStreamStopSent::class.java, EventStreamStopSentSerializer()
            )
            .registerTypeAdapter(
                EventStreamLocation::class.java, EventStreamLocationSerializer()
            )
            .create()
}

class EventStreamLocationSerializer : JsonSerializer<EventStreamLocation> {
    override fun serialize(
        src: EventStreamLocation?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement? {
        if (src == null) return null
        val result = JsonObject()
        result.add("type", JsonPrimitive(src.type))
        result.add("targetSystem", JsonPrimitive(src.targetSystem))
        result.add("dataId", JsonPrimitive(src.dataId))
        result.add("serialNumber", JsonPrimitive(src.serialNumber))
        result.add("latitude", JsonPrimitive(src.latitude.format(10).toDouble()))
        result.add("longitude", JsonPrimitive(src.longitude.format(10).toDouble()))
        result.add("altitude", JsonPrimitive(src.altitude.format(2).toDouble()))
        result.add("fixTimeEpoch", JsonPrimitive(src.fixTime))
        result.add("gpsSpeed", JsonPrimitive(src.gpsSpeed.format(2).toDouble()))
        result.add("accuracy", JsonPrimitive(src.accuracy.format(2).toDouble()))
        result.add("gpsHeading", JsonPrimitive(src.gpsHeading.format(2).toDouble()))
        result.add("eventType", JsonPrimitive(src.eventType))
        result.add("lac", JsonPrimitive(src.lac))
        result.add("mcc", JsonPrimitive(src.mcc))
        result.add("mnc", JsonPrimitive(src.mnc))
        result.add("mnc", JsonPrimitive(src.mnc))
        if (src.mobileCellId.toInt() != 0)
            result.add("mobileCellId", JsonPrimitive(src.mobileCellId))
        result.add("dataFromHardwareGps", JsonPrimitive(src.dataFromHardwareGps))
        result.add("foregroundMode", JsonPrimitive(src.appInForeground))
        result.add("internetAvailable", JsonPrimitive(src.internetAvailable))
        result.add("batteryLevel", JsonPrimitive(src.batteryLevel))
        if (src.trackingDevice != null) result.add(
            "trackingDevice",
            JsonPrimitive(src.trackingDevice)
        )
        return result
    }

}

class EventStreamStartSentSerializer : JsonSerializer<EventStreamStartSent> {
    override fun serialize(
        src: EventStreamStartSent?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement? {
        if (src == null) return null
        val result = JsonObject()
        result.add("type", JsonPrimitive(src.type))
        if (src.latitude != null) result.add("latitude", JsonPrimitive(src.latitude))
        if (src.longitude != null) result.add("longitude", JsonPrimitive(src.longitude))
        result.add("fixTimeEpoch", JsonPrimitive(src.fixTime))
        if (src.sentNumber != null) result.add("sentNumber", JsonPrimitive(src.sentNumber))
        if (src.dataFromHardwareGps != null) result.add(
            "dataFromHardwareGps",
            JsonPrimitive(src.dataFromHardwareGps)
        )
        result.add("foregroundMode", JsonPrimitive(src.appInForeground))
        return result
    }

}

class EventStreamStopSentSerializer : JsonSerializer<EventStreamStopSent> {
    override fun serialize(
        src: EventStreamStopSent?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement? {
        if (src == null) return null
        val result = JsonObject()
        result.add("type", JsonPrimitive(src.type))
        if (src.latitude != null) result.add("latitude", JsonPrimitive(src.latitude))
        if (src.longitude != null) result.add("longitude", JsonPrimitive(src.longitude))
        result.add("fixTimeEpoch", JsonPrimitive(src.fixTime))
        if (src.sentNumber != null) result.add("sentNumber", JsonPrimitive(src.sentNumber))
        if (src.dataFromHardwareGps != null) result.add(
            "dataFromHardwareGps",
            JsonPrimitive(src.dataFromHardwareGps)
        )
        result.add("foregroundMode", JsonPrimitive(src.appInForeground))
        return result
    }

}