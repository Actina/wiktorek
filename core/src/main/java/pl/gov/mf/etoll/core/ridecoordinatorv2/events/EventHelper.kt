package pl.gov.mf.etoll.core.ridecoordinatorv2.events

import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorConfiguration

interface EventHelper {
    fun setStartOfTracking(rideCoordinatorConfiguration: RideCoordinatorConfiguration)
    fun generateIntervalEventForTolled(rideCoordinatorConfiguration: RideCoordinatorConfiguration)
    fun generateIntervalEventForSent(rideCoordinatorConfiguration: RideCoordinatorConfiguration)
    fun generateStopEvents(rideCoordinatorConfiguration: RideCoordinatorConfiguration)
    fun setCallbacks(callbacks: EventHelperCallbacks?, tag: String)
    fun generateTolledChangedEvent(rideCoordinatorConfiguration: RideCoordinatorConfiguration)
    fun generateSentChangedEvent(
        type: SentChangeActionType,
        sentId: String?,
        rideCoordinatorConfiguration: RideCoordinatorConfiguration
    )

    fun generateTrackingDeviceChangedEvent(
        rideCoordinatorConfiguration: RideCoordinatorConfiguration, zslIsTheNewDeviceType: Boolean,
        sentForTolled: Boolean,
        sentForSent: Boolean
    )

    fun generateResumeApplicationEvent(
        applicationTerminationTimeIsMsecs: Long,
        hardRestart: Boolean
    )
}

interface EventHelperCallbacks {
    fun onEventGenerated(
        type: EventType,
        technicalType: ServiceEventType,
        data: String,
        sent: Boolean
    )

    fun onEventDidntPassedFiltering(eventType: String)
    fun onMonitoringDeviceChanged(newDevice: String)
    fun onResumeRideEvent(applicationTerminationTimeIsMsecs: Long, hardRestart: Boolean)
}

enum class SentChangeActionType {
    START, STOP, CANCEL
}