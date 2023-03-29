package pl.gov.mf.etoll.core.ridecoordinatorv2.events

enum class EventType(val apiName: String) {
    LOCATION("location"),
    START("startjourney"),
    END("endjourney"),
    GPS_ONLINE("gpsonline"),
    GPS_OFFLINE("gpsoffline");

    companion object {
        fun fromString(literal: String): EventType {
            for (event in values())
                if (event.apiName.contentEquals(literal))
                    return event
            return LOCATION
        }
    }
}

enum class ServiceEventType(val apiName: String, val typeId: Int) {
    DEFAULT("spoekas", 0),
    ACTIVATE_VEHICLE("activateVehicle", 1),
    START_SENT("startSent", 2),
    STOP_SENT("stopSent", 3),
    CANCEL_SENT("cancelSent", 4),
    CHANGE_DEVICE("changetrackingdevice", 5),
    APPLICATION_RESUME("applicationResume", 6);

    companion object {
        fun fromString(literal: String): ServiceEventType {
            for (event in values())
                if (event.apiName.contentEquals(literal))
                    return event
            return DEFAULT
        }

        fun fromInt(typeId: Int): ServiceEventType {
            for (value in values())
                if (value.typeId == typeId)
                    return value
            return DEFAULT
        }
    }
}