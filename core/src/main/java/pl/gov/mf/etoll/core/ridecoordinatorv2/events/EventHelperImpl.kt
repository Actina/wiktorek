package pl.gov.mf.etoll.core.ridecoordinatorv2.events

import android.util.Log
import pl.gov.mf.etoll.commons.TimeUtils
import pl.gov.mf.etoll.core.model.CoreMonitoringDeviceType
import pl.gov.mf.etoll.core.ridecoordinatorv2.filter.RideDataFilterDecorator
import pl.gov.mf.etoll.core.ridecoordinatorv2.location.LocationServiceProvider
import pl.gov.mf.etoll.core.ridecoordinatorv2.merger.EventStreamLocationMerger
import pl.gov.mf.etoll.core.ridecoordinatorv2.merger.RideTargetSystems
import pl.gov.mf.etoll.core.ridecoordinatorv2.mobile.MobileDataprovider
import pl.gov.mf.etoll.core.ridecoordinatorv2.normalizer.RideDataNormalizerDecorator
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorConfiguration
import pl.gov.mf.etoll.networking.api.model.*
import pl.gov.mf.etoll.storage.database.ridecache.RideCacheDatabase
import pl.gov.mf.etoll.storage.database.ridecache.model.RideCacheModel
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC
import pl.gov.mf.mobile.utils.CallbacksContainer
import javax.inject.Inject

class EventHelperImpl @Inject constructor(
    private val locationServiceProvider: LocationServiceProvider,
    private val mobileDataProvider: MobileDataprovider,
    private val rideCacheDatabase: RideCacheDatabase,
    private val eventStreamLocationMerger: EventStreamLocationMerger,
    private val rideDataFilterDecorator: RideDataFilterDecorator,
    private val rideDataNormalizerDecorator: RideDataNormalizerDecorator,
    private val readSettingsUseCase: SettingsUC.ReadSettingsUseCase
) : EventHelper {

    companion object {
        // this is left for potential future changes
        // change from 23.01 - deviceChanged event should not be sent after activateVehicle or startSent
        private const val SEND_DEVICE_EVENT_AFTER_START_EVENT_FOR_SENT = false
        private const val SEND_DEVICE_EVENT_AFTER_START_EVENT_FOR_TOLLED = false

        // change from 23.01 - sent should NOT use change monitoring device event
        private const val SENT_SHOULD_SUPPORT_CHANGE_DEVICE = false
    }

    private val callbacksContainer: CallbacksContainer<EventHelperCallbacks> = CallbacksContainer()

    override fun setCallbacks(callbacks: EventHelperCallbacks?, tag: String) =
        callbacksContainer.set(callbacks, tag)

    override fun generateTolledChangedEvent(
        rideCoordinatorConfiguration: RideCoordinatorConfiguration
    ) {
        if (rideCoordinatorConfiguration.duringTolled) {
            // activate vehicle
            propagateEvent(
                EventStreamActivateVehicle(
                    vehicleId = rideCoordinatorConfiguration.tolledVehicleId!!,
                    fixTime = TimeUtils.getCurrentTimeForEvents(),
                    tollCategoryIncreaseFlag = rideCoordinatorConfiguration.tolledConfiguration!!.trailerUsedAndCategoryWillBeIncreased,
                    billingAccountId = rideCoordinatorConfiguration.tolledAccountNumberId!!,
                    trackingDevice = CoreMonitoringDeviceType.getStringByTrackByAppState(
                        rideCoordinatorConfiguration.trackByApp
                    )
                ),
                false,
                locationServiceProvider.isInOnlyGpsMode(),
                false
            )
            // make sure second device selection and start journey won't happen till we end tolled session
            // this prevents resending of those events if just trailer or vehicle changed
            if (!rideCoordinatorConfiguration.generatedAtLeastOneEventInSessionForTolled) {
                // select monitoring device
                if (SEND_DEVICE_EVENT_AFTER_START_EVENT_FOR_TOLLED) {
                    generateTrackingDeviceChangedEvent(
                        rideCoordinatorConfiguration,
                        !rideCoordinatorConfiguration.trackByApp,
                        sentForTolled = true,
                        sentForSent = false
                    )
                }
                // and now start journey
                if (rideCoordinatorConfiguration.trackByApp) {
                    var location =
                        locationServiceProvider.getLastKnownLocation(forTolled = true)
                    if (location == null) location =
                        locationServiceProvider.getLastKnownSavedLocation()
                    propagateEvent(
                        eventStreamLocationMerger.merge(
                            location,
                            mobileDataProvider.getLatestData(),
                            EventType.START,
                            rideCoordinatorConfiguration,
                            false
                        ).apply {
                            if (this is EventStreamLocation) this.trackingDevice =
                                CoreMonitoringDeviceType.APP.apiName
                            if (this is EventStreamLocationWithoutLocation) this.trackingDevice =
                                CoreMonitoringDeviceType.APP.apiName
                        }, true, locationServiceProvider.isInOnlyGpsMode(),
                        false
                    )
                } else {
                    propagateEvent(
                        eventStreamLocationMerger.merge(
                            null,
                            mobileDataProvider.getLatestData(),
                            EventType.START,
                            rideCoordinatorConfiguration,
                            false
                        ).apply {
                            if (this is EventStreamLocation) this.trackingDevice =
                                CoreMonitoringDeviceType.ZSL.apiName
                            if (this is EventStreamLocationWithoutLocation) this.trackingDevice =
                                CoreMonitoringDeviceType.ZSL.apiName
                        }, true, locationServiceProvider.isInOnlyGpsMode(),
                        false
                    )
                }
                rideCoordinatorConfiguration.generatedAtLeastOneEventInSessionForTolled = true
            }
        } else {
            var location =
                locationServiceProvider.getLastKnownLocation(forTolled = true)
            if (location == null) location = locationServiceProvider.getLastKnownSavedLocation()
            if (!rideCoordinatorConfiguration.trackByApp) {
                location = null
            }
            propagateEvent(
                eventStreamLocationMerger.merge(
                    location,
                    mobileDataProvider.getLatestData(),
                    EventType.END,
                    rideCoordinatorConfiguration,
                    false
                ), true, locationServiceProvider.isInOnlyGpsMode(),
                false
            )
            // inform system that next event for tolled will be of type "start"
            rideCoordinatorConfiguration.generatedAtLeastOneEventInSessionForTolled = false
            locationServiceProvider.consumeLastKnownLocation(forTolled = true, location)
        }
    }

    override fun generateIntervalEventForTolled(rideCoordinatorConfiguration: RideCoordinatorConfiguration) {
        if (!rideCoordinatorConfiguration.duringTolled) return
        if (!rideCoordinatorConfiguration.generatedAtLeastOneEventInSessionForTolled) {
            // generate start track event and all other stuffs
            generateTolledChangedEvent(
                rideCoordinatorConfiguration
            )
        } else {
            // generate location event
            // we will generate it only if we're in app tracking mode
            if (rideCoordinatorConfiguration.trackByApp) {
                val location =
                    locationServiceProvider.getLastKnownLocation(forTolled = true)
                propagateEvent(
                    eventStreamLocationMerger.merge(
                        location,
                        mobileDataProvider.getLatestData(),
                        EventType.LOCATION,
                        rideCoordinatorConfiguration,
                        false
                    ), false, locationServiceProvider.isInOnlyGpsMode(),
                    false
                )
                locationServiceProvider.consumeLastKnownLocation(forTolled = true, location)
            }
        }
    }

    override fun generateSentChangedEvent(
        type: SentChangeActionType,
        sentId: String?,
        rideCoordinatorConfiguration: RideCoordinatorConfiguration
    ) {
        var lastLocation = locationServiceProvider.getLastKnownLocation(forTolled = false)
        if (lastLocation == null) lastLocation = locationServiceProvider.getLastKnownSavedLocation()
        if (!rideCoordinatorConfiguration.trackByApp) {
            lastLocation = null
        }
        val longitude = lastLocation?.longitude
        val latitude = lastLocation?.latitude
        val foreground = readSettingsUseCase.executeForBoolean(Settings.APP_IN_FOREGROUND)
        when (type) {
            SentChangeActionType.START -> propagateEvent(
                EventStreamStartSent(
                    fixTime = TimeUtils.getCurrentTimeForEvents(),
                    longitude = longitude,
                    latitude = latitude,
                    sentNumber = sentId,
                    appInForeground = foreground
                ), false, locationServiceProvider.isInOnlyGpsMode(),
                true
            )
            SentChangeActionType.STOP -> propagateEvent(
                EventStreamStopSent(
                    fixTime = TimeUtils.getCurrentTimeForEvents(),
                    longitude = longitude,
                    latitude = latitude,
                    sentNumber = sentId,
                    appInForeground = foreground
                ), false, locationServiceProvider.isInOnlyGpsMode(),
                true
            )
            SentChangeActionType.CANCEL -> propagateEvent(
                EventStreamCancelSent(
                    fixTime = TimeUtils.getCurrentTimeForEvents(),
                    sentNumber = sentId!!
                ), false, locationServiceProvider.isInOnlyGpsMode(),
                true
            )
        }
    }

    override fun generateTrackingDeviceChangedEvent(
        rideCoordinatorConfiguration: RideCoordinatorConfiguration,
        zslIsTheNewDeviceType: Boolean,
        sentForTolled: Boolean,
        sentForSent: Boolean
    ) {
        if (!rideCoordinatorConfiguration.duringRide) return
        if (sentForSent && SENT_SHOULD_SUPPORT_CHANGE_DEVICE) {
            propagateEvent(
                EventStreamChangeTrackingDevice(
                    trackingDevice = CoreMonitoringDeviceType.getStringByTrackByAppState(!zslIsTheNewDeviceType),
                    fixTime = TimeUtils.getCurrentTimeForEvents()
                ), true, locationServiceProvider.isInOnlyGpsMode(),
                true
            )
        }
        if (sentForTolled) {
            propagateEvent(
                EventStreamChangeTrackingDevice(
                    trackingDevice = CoreMonitoringDeviceType.getStringByTrackByAppState(!zslIsTheNewDeviceType),
                    fixTime = TimeUtils.getCurrentTimeForEvents()
                ), true, locationServiceProvider.isInOnlyGpsMode(),
                false
            )
        }
        callbacksContainer.get().values.forEach {
            it.onMonitoringDeviceChanged(CoreMonitoringDeviceType.getStringByTrackByAppState(!zslIsTheNewDeviceType))
        }
    }

    override fun setStartOfTracking(rideCoordinatorConfiguration: RideCoordinatorConfiguration) {
        rideCoordinatorConfiguration.generatedAtLeastOneEventInSessionForTolled = false
        rideCoordinatorConfiguration.generatedAtLeastOneEventInSessionForSent = false
        // if it's obe monitoring, we should generate now events for starting ride
        if (!rideCoordinatorConfiguration.trackByApp) {
            // tolled
            if (rideCoordinatorConfiguration.duringTolled) {
                // tolled start events
                generateIntervalEventForTolled(rideCoordinatorConfiguration)
            }
            if (rideCoordinatorConfiguration.duringSent) {
                // sent start events
                generateIntervalEventForSent(rideCoordinatorConfiguration)
            }
        }
    }


    override fun generateIntervalEventForSent(rideCoordinatorConfiguration: RideCoordinatorConfiguration) {
        if (!rideCoordinatorConfiguration.duringSent) return
        // generate location event
        // we will generate it only if we're in app tracking mode
        if (!rideCoordinatorConfiguration.generatedAtLeastOneEventInSessionForSent) {
            // send start sent events for every package or for sent without package

            // generate SENT overall start event
            generateSentChangedEvent(
                SentChangeActionType.START,
                null,
                rideCoordinatorConfiguration
            )

            // generate events for each package
            rideCoordinatorConfiguration.sentConfiguration!!.selectedSentList.forEach { sentId ->
                generateSentChangedEvent(
                    SentChangeActionType.START,
                    sentId.sentNumber,
                    rideCoordinatorConfiguration
                )
            }

            // select monitoring device
            if (SEND_DEVICE_EVENT_AFTER_START_EVENT_FOR_SENT) {
                generateTrackingDeviceChangedEvent(
                    rideCoordinatorConfiguration,
                    !rideCoordinatorConfiguration.trackByApp,
                    sentForTolled = false,
                    sentForSent = true
                )
            }
            rideCoordinatorConfiguration.generatedAtLeastOneEventInSessionForSent = true
        } else
            if (rideCoordinatorConfiguration.trackByApp) {
                val location =
                    locationServiceProvider.getLastKnownLocation(forTolled = false)
                propagateEvent(
                    eventStreamLocationMerger.merge(
                        location,
                        mobileDataProvider.getLatestData(),
                        EventType.LOCATION,
                        rideCoordinatorConfiguration,
                        true
                    ), false, locationServiceProvider.isInOnlyGpsMode(),
                    true
                )
                locationServiceProvider.consumeLastKnownLocation(forTolled = false, location)
            }
    }

    override fun generateResumeApplicationEvent(
        applicationTerminationTimeIsMsecs: Long,
        hardRestart: Boolean
    ) {
        propagateEvent(
            EventStreamResumeApplication(
                fixTime = TimeUtils.getCurrentTimeForEvents(),
                terminationTimeInMsecs = applicationTerminationTimeIsMsecs,
                hardRestart = hardRestart
            ),
            true, locationServiceProvider.isInOnlyGpsMode(),
            false
        )
        callbacksContainer.get().values.forEach {
            it.onResumeRideEvent(applicationTerminationTimeIsMsecs, hardRestart)
        }
    }

    override fun generateStopEvents(rideCoordinatorConfiguration: RideCoordinatorConfiguration) {
        if (!rideCoordinatorConfiguration.generatedAtLeastOneEventInSessionForSent
            || !rideCoordinatorConfiguration.generatedAtLeastOneEventInSessionForTolled
        ) {
            // special case, ended before any event came
            // we should send start and stop immediately
            if (rideCoordinatorConfiguration.duringTolled && !rideCoordinatorConfiguration.generatedAtLeastOneEventInSessionForTolled) {
                propagateEvent(
                    eventStreamLocationMerger.merge(
                        null,
                        mobileDataProvider.getLatestData(),
                        EventType.START,
                        rideCoordinatorConfiguration,
                        false
                    ).apply {
                        if (this is EventStreamLocation) this.trackingDevice =
                            CoreMonitoringDeviceType.getStringByTrackByAppState(
                                rideCoordinatorConfiguration.trackByApp
                            )
                        if (this is EventStreamLocationWithoutLocation) this.trackingDevice =
                            CoreMonitoringDeviceType.getStringByTrackByAppState(
                                rideCoordinatorConfiguration.trackByApp
                            )
                    }, true, locationServiceProvider.isInOnlyGpsMode(),
                    false
                )
            }
            if (rideCoordinatorConfiguration.duringSent && !rideCoordinatorConfiguration.generatedAtLeastOneEventInSessionForSent) {
                // send start sent events for every package and for sent in overall

                // generate SENT overall start event
                generateSentChangedEvent(
                    SentChangeActionType.START,
                    null,
                    rideCoordinatorConfiguration
                )
                // and now package ones
                if (!rideCoordinatorConfiguration.sentConfiguration!!.selectedSentList.isNullOrEmpty()) {
                    // generate events for each package
                    rideCoordinatorConfiguration.sentConfiguration!!.selectedSentList.forEach { sentId ->
                        generateSentChangedEvent(
                            SentChangeActionType.START,
                            sentId.sentNumber,
                            rideCoordinatorConfiguration
                        )
                    }
                }
            }
        }

        // END for SENT
        if (rideCoordinatorConfiguration.duringSent) {
            // option with packages
            if (!rideCoordinatorConfiguration.sentConfiguration!!.selectedSentList.isNullOrEmpty()) {
                rideCoordinatorConfiguration.sentConfiguration!!.selectedSentList.forEach { sent ->
                    generateSentChangedEvent(
                        type = SentChangeActionType.STOP,
                        sentId = sent.sentNumber,
                        rideCoordinatorConfiguration = rideCoordinatorConfiguration
                    )
                }
            }

            // and overall stop
            generateSentChangedEvent(
                SentChangeActionType.STOP,
                null,
                rideCoordinatorConfiguration
            )

        }
        // end journey for TOLLED
        if (rideCoordinatorConfiguration.duringTolled) {
            var location =
                locationServiceProvider.getLastKnownLocation(forTolled = true)
            if (location == null) location = locationServiceProvider.getLastKnownSavedLocation()
            if (!rideCoordinatorConfiguration.trackByApp) {
                location = null
            }
            propagateEvent(
                eventStreamLocationMerger.merge(
                    location,
                    mobileDataProvider.getLatestData(),
                    EventType.END,
                    rideCoordinatorConfiguration,
                    false
                ), true, locationServiceProvider.isInOnlyGpsMode(),
                false
            )
        }

        rideCoordinatorConfiguration.generatedAtLeastOneEventInSessionForSent = false
        rideCoordinatorConfiguration.generatedAtLeastOneEventInSessionForTolled = false
    }


    private fun propagateEvent(
        eventIn: EventStreamBaseEvent?,
        ignoreFiltering: Boolean = false,
        useOnlyGps: Boolean,
        forSent: Boolean
    ) {
        // we do not propagate empty events
        if (eventIn == null) return
        when (eventIn) {
            is EventStreamLocation -> {
                // then filter it
                var event = eventIn
                val valid = rideDataFilterDecorator.isDataValid(eventIn)
                Log.d(
                    "LOCATION_CHECK",
                    "Filtracja ${if (valid || ignoreFiltering) "pozytywna" else "negatywna"} - event ${eventIn}"
                )
                if (!valid) {
                    if (ignoreFiltering) {
                        // drop info about location as it will for sure be wrong
                        // this is possible case when crossing boarders during ride
                        propagateEvent(
                            EventStreamLocationWithoutLocation(
                                eventIn.type,
                                eventIn.targetSystem,
                                eventIn.dataId,
                                eventIn.serialNumber,
                                eventIn.fixTime,
                                eventIn.eventType,
                                eventIn.lac,
                                eventIn.mcc,
                                eventIn.mnc,
                                eventIn.mobileCellId,
                                eventIn.appInForeground,
                                eventIn.internetAvailable,
                                eventIn.batteryLevel
                            ),
                            false,
                            locationServiceProvider.isInOnlyGpsMode(),
                            forSent
                        )
                        return
                    } else {
                        callbacksContainer.get().values.forEach {
                            it.onEventDidntPassedFiltering(
                                event.eventType
                            )
                        }
                        return
                    }
                }
                // and format
                val normalizedEvent = rideDataNormalizerDecorator.formatData(event)
                // add to cache
                val target = RideTargetSystems.from(normalizedEvent.targetSystem)
                val function =
                    if (target == RideTargetSystems.SENT) {
                        rideCacheDatabase.getAndIncrementLatestSentId()
                    } else
                        rideCacheDatabase.getAndIncrementLatestTolledId()

                function.subscribe({
                    normalizedEvent.dataId = it.toString()
                    rideCacheDatabase.addData(
                        RideCacheModel(
                            0,
                            normalizedEvent.fixTime,
                            EventType.fromString(normalizedEvent.type).ordinal,
                            ServiceEventType.DEFAULT.typeId,
                            normalizedEvent.toJSON(),
                            true,
                            useOnlyGps,
                            target.toDBValue()
                        )
                    ).subscribe({
                        // notify callbacks
                        callbacksContainer.get().values.forEach {
                            it.onEventGenerated(
                                EventType.fromString(
                                    event.type
                                ), ServiceEventType.DEFAULT, normalizedEvent.toJSON(), forSent
                            )
                        }
                    }, {})
                }, {})


            }
            is EventStreamLocationWithoutLocation -> {
                // we do not filter this type of events as filtering is always based on gps location
                // and now format
                Log.d("LOCATION_CHECK", "Filtracja - event[bez lokalizacji] ${eventIn}")
                val normalizedEvent = rideDataNormalizerDecorator.formatData(eventIn)
                // add to cache
                val target = RideTargetSystems.from(normalizedEvent.targetSystem)
                val function =
                    if (target == RideTargetSystems.SENT) {
                        rideCacheDatabase.getAndIncrementLatestSentId()
                    } else
                        rideCacheDatabase.getAndIncrementLatestTolledId()

                function.subscribe({
                    normalizedEvent.dataId = it.toString()
                    rideCacheDatabase.addData(
                        RideCacheModel(
                            0,
                            normalizedEvent.fixTime,
                            EventType.fromString(normalizedEvent.type).ordinal,
                            ServiceEventType.DEFAULT.typeId,
                            normalizedEvent.toJSON(),
                            true,
                            useOnlyGps,
                            target.toDBValue()
                        )
                    ).subscribe({
                        // notify callbacks
                        callbacksContainer.get().values.forEach {
                            it.onEventGenerated(
                                EventType.fromString(
                                    normalizedEvent.type
                                ), ServiceEventType.DEFAULT, normalizedEvent.toJSON(), forSent
                            )
                        }
                    }, {})
                }, {})
            }
            is EventStreamActivateVehicle -> {
                // add to cache
                rideCacheDatabase.addData(
                    RideCacheModel(
                        0,
                        eventIn.fixTime,
                        EventType.fromString(eventIn.type).ordinal,
                        ServiceEventType.ACTIVATE_VEHICLE.typeId,
                        eventIn.toJSON(),
                        true,
                        useOnlyGps,
                        RideTargetSystems.TOLLED.toDBValue()
                    )
                ).subscribe()
                // notify callbacks
                callbacksContainer.get().values.forEach {
                    it.onEventGenerated(
                        EventType.fromString(
                            eventIn.type
                        ), ServiceEventType.ACTIVATE_VEHICLE, eventIn.toJSON(), forSent
                    )
                }
            }
            is EventStreamStartSent -> {
                // add to cache
                rideCacheDatabase.addData(
                    RideCacheModel(
                        0,
                        eventIn.fixTime,
                        EventType.fromString(eventIn.type).ordinal,
                        ServiceEventType.START_SENT.typeId, eventIn.toJSON(), true, useOnlyGps,
                        RideTargetSystems.SENT.toDBValue()
                    )
                ).subscribe()
                // notify callbacks
                callbacksContainer.get().values.forEach {
                    it.onEventGenerated(
                        EventType.fromString(
                            eventIn.type
                        ), ServiceEventType.START_SENT, eventIn.toJSON(), forSent
                    )
                }
            }
            is EventStreamStopSent -> {
                // add to cache
                rideCacheDatabase.addData(
                    RideCacheModel(
                        0,
                        eventIn.fixTime,
                        EventType.fromString(eventIn.type).ordinal,
                        ServiceEventType.STOP_SENT.typeId, eventIn.toJSON(), true, useOnlyGps,
                        RideTargetSystems.SENT.toDBValue()
                    )
                ).subscribe()
                // notify callbacks
                callbacksContainer.get().values.forEach {
                    it.onEventGenerated(
                        EventType.fromString(
                            eventIn.type
                        ), ServiceEventType.STOP_SENT, eventIn.toJSON(), forSent
                    )
                }
            }
            is EventStreamCancelSent -> {
                // add to cache
                rideCacheDatabase.addData(
                    RideCacheModel(
                        0,
                        eventIn.fixTime,
                        EventType.fromString(eventIn.type).ordinal,
                        ServiceEventType.CANCEL_SENT.typeId, eventIn.toJSON(), true, useOnlyGps,
                        RideTargetSystems.SENT.toDBValue()
                    )
                ).subscribe()
                // notify callbacks
                callbacksContainer.get().values.forEach {
                    it.onEventGenerated(
                        EventType.fromString(
                            eventIn.type
                        ), ServiceEventType.CANCEL_SENT, eventIn.toJSON(), forSent
                    )
                }
            }
            is EventStreamChangeTrackingDevice -> {
                // add to cache
                if (forSent) {
                    rideCacheDatabase.addData(
                        RideCacheModel(
                            0,
                            eventIn.fixTime,
                            EventType.fromString(eventIn.type).ordinal,
                            ServiceEventType.CHANGE_DEVICE.typeId,
                            eventIn.toJSON(),
                            true,
                            useOnlyGps,
                            RideTargetSystems.SENT.toDBValue()
                        )
                    ).subscribe()
                } else {
                    rideCacheDatabase.addData(
                        RideCacheModel(
                            0,
                            eventIn.fixTime,
                            EventType.fromString(eventIn.type).ordinal,
                            ServiceEventType.CHANGE_DEVICE.typeId,
                            eventIn.toJSON(),
                            true,
                            useOnlyGps,
                            RideTargetSystems.TOLLED.toDBValue()
                        )
                    ).subscribe()
                }
            }
            is EventStreamResumeApplication -> {
                rideCacheDatabase.addData(
                    RideCacheModel(
                        0,
                        eventIn.fixTime,
                        EventType.fromString(eventIn.type).ordinal,
                        ServiceEventType.APPLICATION_RESUME.typeId,
                        eventIn.toJSON(),
                        false,
                        useOnlyGps,
                        // FYI: we can always use here just one target, regardless of real ride mode,
                        // as we do check always for both when sending
                        RideTargetSystems.TOLLED.toDBValue()
                    )
                ).subscribe()
            }
            else -> {
                throw RuntimeException("Nieznany typ eventu, EventHelperImpl")
            }
        }

    }
}

fun RideTargetSystems.toDBValue(): Int {
    return when (this) {
        RideTargetSystems.TOLLED -> apiValue
        RideTargetSystems.SENT -> apiValue
        RideTargetSystems.TOLLED_LIGHT -> RideTargetSystems.TOLLED.apiValue
    }
}
