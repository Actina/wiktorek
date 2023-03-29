package pl.gov.mf.etoll.core.ridehistory.collector

import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import pl.gov.mf.etoll.core.model.CoreAccountInfo
import pl.gov.mf.etoll.core.model.CoreSent
import pl.gov.mf.etoll.core.model.CoreSentAddress
import pl.gov.mf.etoll.core.model.CoreVehicle
import pl.gov.mf.etoll.core.ridecoordinatorv2.events.EventType
import pl.gov.mf.etoll.core.ridecoordinatorv2.events.SentChangeActionType
import pl.gov.mf.etoll.core.ridecoordinatorv2.events.ServiceEventType
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.MonitoringDeviceConfiguration
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorV3
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorV3Callbacks
import pl.gov.mf.etoll.core.ridehistory.RideHistoryUC
import pl.gov.mf.etoll.core.ridehistory.model.ActivityAdditionalData
import pl.gov.mf.etoll.core.ridehistory.model.HistoryItemFrameType
import pl.gov.mf.etoll.core.ridehistory.model.HistoryItemType
import pl.gov.mf.etoll.core.ridehistory.model.RideHistoryDataItem
import pl.gov.mf.mobile.utils.addSafe
import javax.inject.Inject


class RideHistoryCollectorImpl @Inject constructor(
    private val writeHistoryUseCase: RideHistoryUC.WriteHistoryUseCase,
    private val coordinator: RideCoordinatorV3
) : RideCoordinatorV3Callbacks, RideHistoryCollector {

    private lateinit var compositeDisposable: CompositeDisposable

    override fun start() {
        compositeDisposable = CompositeDisposable()
        coordinator.setCallbacks(this, CALLBACK_TAG)
    }

    override fun onFakeGpsDetected(): Completable =
        writeHistoryUseCase.execute(
            RideHistoryDataItem(
                historyItemType = HistoryItemType.FAKE_GPS_DETECTED,
                historyItemFrameType = HistoryItemFrameType.COLOR_RED
            )
        )

    override fun onInternetStrengthMoveFromFairToCritical(): Completable =
        writeHistoryUseCase.execute(
            RideHistoryDataItem(
                historyItemType = HistoryItemType.INTERNET_LOW,
                historyItemFrameType = HistoryItemFrameType.NO_FRAME
            )
        )

    override fun onInternetStrengthMoveFromCriticalToFair(): Completable =
        writeHistoryUseCase.execute(
            RideHistoryDataItem(
                historyItemType = HistoryItemType.INTERNET_GOOD,
                historyItemFrameType = HistoryItemFrameType.NO_FRAME
            )
        )

    override fun onBatteryLevelMoveFromFairToRed(): Completable =
        writeHistoryUseCase.execute(
            RideHistoryDataItem(
                historyItemType = HistoryItemType.BATTERY_LOW,
                historyItemFrameType = HistoryItemFrameType.NO_FRAME
            )
        )

    override fun onBatteryLevelMoveFromRedToFair(): Completable =
        writeHistoryUseCase.execute(
            RideHistoryDataItem(
                historyItemType = HistoryItemType.BATTERY_GOOD,
                historyItemFrameType = HistoryItemFrameType.NO_FRAME
            )
        )

    override fun onGpsSignalLost(): Completable =
        writeHistoryUseCase.execute(
            RideHistoryDataItem(
                historyItemType = HistoryItemType.GPS_LOW,
                historyItemFrameType = HistoryItemFrameType.NO_FRAME
            )
        )

    override fun onGpsSignalRetrieve(): Completable =
        writeHistoryUseCase.execute(
            RideHistoryDataItem(
                historyItemType = HistoryItemType.GPS_GOOD,
                historyItemFrameType = HistoryItemFrameType.NO_FRAME
            )
        )

    override fun onPrePaidTopUp(data: ActivityAdditionalData.PrePaidAccountTopUp): Completable =
        writeHistoryUseCase.execute(
            RideHistoryDataItem(
                historyItemType = HistoryItemType.PRE_PAID_TOP_UP,
                frame = HistoryItemFrameType.NO_FRAME,
                additionalData = data
            )
        )

    override fun onObservationDeviceChanged(
        newOneIsMobileApp: Boolean,
        obeId: String
    ) {
        compositeDisposable.addSafe(
            writeHistoryUseCase.execute(
                RideHistoryDataItem(
                    historyItemType = HistoryItemType.MONITORING_DEVICE_CHANGED,
                    frame = HistoryItemFrameType.NO_FRAME,
                    additionalData = ActivityAdditionalData.MonitoringData(newOneIsMobileApp)
                )
            ).subscribe()
        )
    }

    override fun onSentRideChange(
        action: SentChangeActionType,
        sentItem: CoreSent?,
        monitoring: MonitoringDeviceConfiguration,
        savedSuccessfully: () -> Unit
    ) {
        var type = HistoryItemType.SENT_START
        var frame = HistoryItemFrameType.COLOR_GREEN
        val additionalData = sentItem?.toSentPackage(monitoring)?.toJSON() ?: ""

        when (action) {
            SentChangeActionType.START -> {
            } // SENT_START - handled above
            SentChangeActionType.STOP -> {
                type = HistoryItemType.SENT_END
                frame = HistoryItemFrameType.COLOR_RED
            }
            SentChangeActionType.CANCEL -> {
                type = HistoryItemType.SENT_CANCEL
                frame = HistoryItemFrameType.COLOR_RED
            }
        }

        compositeDisposable.addSafe(
            writeHistoryUseCase.execute(
                RideHistoryDataItem(
                    historyItemType = type,
                    historyItemFrameType = frame,
                    additionalData = additionalData
                )
            ).subscribe({
                savedSuccessfully()
            }, {})
        )
    }

    override fun onRideStart(
        rideType: RideCoordinatorV3Callbacks.RideType,
        vehicle: CoreVehicle?,
        monitoring: MonitoringDeviceConfiguration,
        exceedingWeightDeclared: Boolean,
        accountInfo: CoreAccountInfo?,
        selectedSentList: MutableList<CoreSent>?
    ) {
        val additionalData = ActivityAdditionalData.RideSnapshot(
            rideType = rideType,
            monitoringData = monitoring.toMonitoringData(),
            accountInfo = accountInfo?.toAccountInfoSnapshot(),
            categoryIncrease = exceedingWeightDeclared,
            vehicle = vehicle?.toVehicleData(),
            selectedSentList = selectedSentList
        )

        compositeDisposable.addSafe(
            writeHistoryUseCase.execute(
                RideHistoryDataItem(
                    historyItemType = HistoryItemType.RIDE_START,
                    frame = HistoryItemFrameType.COLOR_GREEN,
                    additionalData = additionalData
                )
            ).subscribe()
        )
    }

    override fun onRideStop(
        type: RideCoordinatorV3Callbacks.RideType,
        vehicle: CoreVehicle?,
        monitoring: MonitoringDeviceConfiguration,
        exceedingWeightDeclared: Boolean,
        accountInfo: CoreAccountInfo?,
        selectedSentList: MutableList<CoreSent>?
    ) {
        compositeDisposable.addSafe(
            writeHistoryUseCase.execute(
                RideHistoryDataItem(
                    historyItemType = HistoryItemType.RIDE_END,
                    frame = HistoryItemFrameType.COLOR_RED,
                    additionalData = ActivityAdditionalData.RideSnapshot(
                        rideType = type,
                        vehicle = vehicle?.toVehicleData(),
                        categoryIncrease = exceedingWeightDeclared,
                        monitoringData = monitoring.toMonitoringData(),
                        accountInfo = accountInfo?.toAccountInfoSnapshot(),
                        selectedSentList = selectedSentList
                    )
                )
            ).subscribe()
        )
    }

    private fun onTrailerWeightExceeded() {
        compositeDisposable.addSafe(
            writeHistoryUseCase.execute(
                RideHistoryDataItem(
                    historyItemType = HistoryItemType.TRAILER_WEIGHT_EXCEEDED,
                    historyItemFrameType = HistoryItemFrameType.NO_FRAME,
                    additionalData = ActivityAdditionalData.TrailerData(true).toJSON()
                )
            ).subscribe()
        )
    }

    private fun onTrailerWeightNotExceeded() {
        compositeDisposable.addSafe(
            writeHistoryUseCase.execute(
                RideHistoryDataItem(
                    historyItemType = HistoryItemType.TRAILER_WEIGHT_NOT_EXCEEDED,
                    historyItemFrameType = HistoryItemFrameType.NO_FRAME,
                    additionalData = ActivityAdditionalData.TrailerData(false).toJSON()
                )
            ).subscribe()
        )
    }

    override fun onTrailerChange(
        exceedingWeightDeclared: Boolean
    ) {
        if (exceedingWeightDeclared) onTrailerWeightExceeded() else onTrailerWeightNotExceeded()
    }

    override fun onEventGenerated(
        type: EventType,
        technicalType: ServiceEventType,
        data: String,
        sent: Boolean
    ) {
    }

    override fun onEventDidntPassedFiltering(eventType: String) {}
    override fun onMonitoringDeviceChanged(newDevice: String) {}
    override fun onRideCoordinatorError(cause: String?) {}
    override fun onTolledRideChange(
        started: Boolean,
        usedVehicle: Long?,
        exceedingWeightDeclared: Boolean
    ) {
    }

    override fun onResumeRideEvent(applicationTerminationTimeIsMsecs: Long, hardRestart: Boolean) {}

    override fun onGpsWarmingStateChanged(warming: Boolean, readyToUse: Boolean) {}

    private fun CoreSent.toSentPackage(monitoring: MonitoringDeviceConfiguration): ActivityAdditionalData.SentStartSnapshot =
        ActivityAdditionalData.SentStartSnapshot(
            sentNumber = sentNumber,
            startTimestamp = startTimestamp,
            endTimestamp = endTimestamp,
            loadingAddress = loadingAddress.toAddressData(),
            deliveryAddress = deliveryAddress.toAddressData(),
            monitoringData = ActivityAdditionalData.MonitoringData(monitoring.monitoringByApp)
        )

    private fun CoreSentAddress.toAddressData(): ActivityAdditionalData.AddressData =
        ActivityAdditionalData.AddressData(
            country = country,
            postalCode = postalCode,
            city = city,
            street = street,
            houseNumber = houseNumber,
            latitude = latitude,
            longitude = longitude
        )

    private fun MonitoringDeviceConfiguration.toMonitoringData(): ActivityAdditionalData.MonitoringData =
        ActivityAdditionalData.MonitoringData(monitoringByApp)

    private fun CoreVehicle.toVehicleData(): ActivityAdditionalData.VehicleSnapshot =
        ActivityAdditionalData.VehicleSnapshot(
            licensePlate = licensePlate,
            brand = brand,
            model = model,
            emissionClass = emissionClass,
            category = category,
        )

    private fun CoreAccountInfo.toAccountInfoSnapshot(): ActivityAdditionalData.AccountInfoSnapshot =
        ActivityAdditionalData.AccountInfoSnapshot(
            type = type,
            balanceValue = balance?.value,
            balanceIsInitialized = balance?.isInitialized ?: false
        )

    companion object {
        private const val CALLBACK_TAG = "RideHistoryCollector"
    }
}