package pl.gov.mf.etoll.demo.watchdog

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import org.joda.time.DateTime
import pl.gov.mf.etoll.BuildConfig
import pl.gov.mf.etoll.core.model.CoreAccountInfo
import pl.gov.mf.etoll.core.model.CoreSent
import pl.gov.mf.etoll.core.model.CoreVehicle
import pl.gov.mf.etoll.core.ridecoordinatorv2.events.EventType
import pl.gov.mf.etoll.core.ridecoordinatorv2.events.SentChangeActionType
import pl.gov.mf.etoll.core.ridecoordinatorv2.events.SentChangeActionType.*
import pl.gov.mf.etoll.core.ridecoordinatorv2.events.ServiceEventType
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.MonitoringDeviceConfiguration
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorV3
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorV3Callbacks
import pl.gov.mf.etoll.core.ridecoordinatorv2.sender.RideCoordinatorV3Sender
import pl.gov.mf.etoll.core.ridecoordinatorv2.sender.RideCoordinatorV3SenderCallbacks
import pl.gov.mf.etoll.front.watchdog.DemoListItem
import pl.gov.mf.etoll.front.watchdog.DemoWatchdog
import javax.inject.Inject

class DemoWatchdogImpl @Inject constructor(
    private val coordinator: RideCoordinatorV3,
    private val sender: RideCoordinatorV3Sender
) : DemoWatchdog, RideCoordinatorV3Callbacks, RideCoordinatorV3SenderCallbacks {

    private val _items: MutableList<DemoListItem> =
        java.util.Collections.synchronizedList(mutableListOf(DemoListItem.generateInfo("Start systemu")))
    private val subjectItems: BehaviorSubject<List<DemoListItem>> =
        BehaviorSubject.createDefault(_items)

    override fun start() {
        if (BuildConfig.DEMO_APP) {
            coordinator.setCallbacks(this, "DemoWatchdogImpl")
            sender.setCallbacks(this, "DemoWatchdogImpl")
        }
    }

    override fun observeChanges(): Observable<List<DemoListItem>> =
        subjectItems.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    override fun onGpsWarmingStateChanged(warming: Boolean, readyToUse: Boolean) {
        // TODO
    }

    override fun onResumeRideEvent(applicationTerminationTimeIsMsecs: Long, hardRestart: Boolean) {
        _items.add(
            0,
            DemoListItem.generateInfo(
                "Ride resume\nhard? ${hardRestart}\nlasttime: ${
                    DateTime(
                        applicationTerminationTimeIsMsecs
                    )
                }"
            )
        )
    }

    override fun onObservationDeviceChanged(
        newOneIsMobileApp: Boolean,
        obeId: String
    ) {
        _items.add(
            0,
            DemoListItem.generateInfo("Zmieniono urządzenie monitorujące na " + (if (newOneIsMobileApp) "aplikację" else "OBE"))
        )
        subjectItems.onNext(_items)
    }

    override fun onTrailerChange(
        exceedingWeightDeclared: Boolean
    ) {
    }

    override fun onTolledRideChange(
        started: Boolean,
        usedVehicle: Long?,
        exceedingWeightDeclared: Boolean
    ) {
        if (started) {
            _items.add(
                0,
                DemoListItem.generateInfo("Uruchomiono przejazd TOLLED, vehicleId = $usedVehicle, trailer = $exceedingWeightDeclared")
            )
        } else {
            _items.add(
                0,
                DemoListItem.generateInfo("Wyłączono przejazd TOLLED")
            )
        }
        subjectItems.onNext(_items)
    }

    override fun onSentRideChange(
        action: SentChangeActionType,
        sentItem: CoreSent?,
        monitoring: MonitoringDeviceConfiguration,
        savedSuccessfully: () -> Unit
    ) {
        when (action) {
            START ->
                _items.add(
                    0,
                    DemoListItem.generateInfo(
                        "Rozpoczęto przejazd SENT - aktywne " //+ (sentName ?: "")
                    )
                )
            STOP -> _items.add(
                0,
                DemoListItem.generateInfo(
                    "Zakończono przejazd SENT - pozostałe aktywne " //+ (sentName ?: "")
                )
            )
            CANCEL -> _items.add(
                0,
                DemoListItem.generateInfo(
                    "Anulowano przejazd SENT - pozostałe aktywne "// + (sentName ?: "")
                )
            )
        }
        subjectItems.onNext(_items)
    }

    override fun onRideCoordinatorError(cause: String?) {
        _items.add(
            0,
            DemoListItem.generateError(cause ?: "Błąd ride coordinatora")
        )
        subjectItems.onNext(_items)
    }

    override fun onRideStart(
        rideType: RideCoordinatorV3Callbacks.RideType,
        vehicle: CoreVehicle?,
        monitoring: MonitoringDeviceConfiguration,
        exceedingWeightDeclared: Boolean,
        accountInfo: CoreAccountInfo?,
        selectedSentList: MutableList<CoreSent>?
    ) {

    }

    override fun onRideStop(
        type: RideCoordinatorV3Callbacks.RideType,
        vehicle: CoreVehicle?,
        monitoring: MonitoringDeviceConfiguration,
        exceedingWeightDeclared: Boolean,
        accountInfo: CoreAccountInfo?,
        selectedSentList: MutableList<CoreSent>?
    ) {

    }

    override fun onEventGenerated(
        type: EventType,
        technicalType: ServiceEventType,
        data: String,
        sent: Boolean
    ) {
        when (technicalType) {
            ServiceEventType.DEFAULT ->
                _items.add(0, DemoListItem.generateLocationEvent(type, data, sent))
            else -> DemoListItem.generateInfo("Event techniczny: ${technicalType.apiName}")
        }
        subjectItems.onNext(_items)
    }

    override fun onEventDidntPassedFiltering(eventType: String) {
        _items.add(
            0,
            DemoListItem.generateError("Event nie przeszedł filtracji: $eventType")
        )
        subjectItems.onNext(_items)
    }

    override fun onMonitoringDeviceChanged(newDevice: String) {
        _items.add(
            0,
            DemoListItem.generateInfo("Wybrano urządzenie monitorujące : $newDevice")
        )
        subjectItems.onNext(_items)
    }

    override fun onPackageSent(size: Int, ids: List<Long>, sent: Boolean) {
        val convertedIds = mutableListOf<String>()
        ids.forEach { convertedIds.add(it.toString()) }
        _items.add(0, DemoListItem.generateSendingEvent(size, convertedIds, sent))
        subjectItems.onNext(_items)
    }

    override fun onPackageSendingError(details: String?) {
        _items.add(0, DemoListItem.generateError("Nie udało się wysłać paczki danych $details"))
        subjectItems.onNext(_items)
    }
}