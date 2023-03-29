package pl.gov.mf.etoll.core.ridecoordinatorv2.merger

import pl.gov.mf.etoll.core.ridecoordinatorv2.events.EventType
import pl.gov.mf.etoll.core.ridecoordinatorv2.mobile.MobileData
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorConfiguration
import pl.gov.mf.etoll.networking.api.model.EventStreamBaseEvent
import pl.gov.mf.mobile.utils.LocationWrapper

interface EventStreamLocationMerger {
    fun merge(
        location: LocationWrapper?,
        mobileData: MobileData?,
        eventType: EventType,
        configuration: RideCoordinatorConfiguration,
        targetIsSent: Boolean,
    ): EventStreamBaseEvent
}

enum class RideTargetSystems(val apiValue: Int) {
    TOLLED(1),
    SENT(2),
    TOLLED_LIGHT(4);

    companion object {
        fun from(value: Int): RideTargetSystems {
            for (found in values())
                if (found.apiValue == value)
                    return found
            return TOLLED
        }
    }
}