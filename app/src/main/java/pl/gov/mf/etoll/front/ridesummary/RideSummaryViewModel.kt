package pl.gov.mf.etoll.front.ridesummary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.Disposable
import pl.gov.mf.etoll.base.BaseDatabindingViewModel
import pl.gov.mf.etoll.commons.TimeUtils
import pl.gov.mf.etoll.commons.formatMillisToReadableUiTime
import pl.gov.mf.etoll.core.CoreComposedUC
import pl.gov.mf.etoll.core.model.CoreAccountTypes
import pl.gov.mf.etoll.core.model.CoreSent
import pl.gov.mf.etoll.core.model.CoreVehicle
import pl.gov.mf.etoll.core.model.CoreVehicleCategory
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorConfiguration
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorUC
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.SentConfiguration
import pl.gov.mf.etoll.front.configsentridesselection.details.SentMapDetailsData
import pl.gov.mf.etoll.front.shared.*
import pl.gov.mf.etoll.front.shared.adapter.CardsAdapter
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC
import pl.gov.mf.mobile.utils.addSafe
import pl.gov.mf.mobile.utils.disposeSafe
import javax.inject.Inject

class RideSummaryViewModel : BaseDatabindingViewModel(), CardSentDetailsCallbacks {

    private var updaterDisposable: Disposable? = null

    @Inject
    lateinit var cleanSystemAfterSummaryUseCase: SummaryDataUC.CleanSystemAfterSummaryUseCase

    @Inject
    lateinit var getRideConfigurationUseCase: RideCoordinatorUC.GetRideCoordinatorConfigurationUseCase

    @Inject
    lateinit var readSettingsUseCase: SettingsUC.ReadSettingsUseCase

    @Inject
    lateinit var isDataSyncInProgressUseCase: CoreComposedUC.IsDataSyncInProgressUseCase

    private val _navigateBack = MutableLiveData(false)
    val navigateBack: LiveData<Boolean>
        get() = _navigateBack

    private val _navigate: MutableLiveData<RideSummaryNavTargets> =
        MutableLiveData(RideSummaryNavTargets.IDLE)
    val navigate: LiveData<RideSummaryNavTargets>
        get() = _navigate

    private val dataSyncInProgress: MutableLiveData<Boolean> = MutableLiveData(true)

    private var selectedSent: CoreSent? = null

    fun onCloseClick() {
        compositeDisposable.addSafe(cleanSystemAfterSummaryUseCase.execute().subscribe {
            _navigateBack.postValue(true)
        })
    }

    fun onToolbarCrossClick() {
        onCloseClick()
    }

    fun getParamsToShowMapDetails(): String? = selectedSent?.let {
        SentMapDetailsData(it, true, showRawDataForRideSummary = true).toJSON()
    }

    fun resetNavigation() {
        _navigate.postValue(RideSummaryNavTargets.IDLE)
    }

    fun addCallbacksForCards(rideSummaryAdapter: CardsAdapter) {
        rideSummaryAdapter.cards.forEach { card ->
            if (card is CardSentSingleDetails) {
                card.setCallbacks(this)
            }
        }
        // add data sync updater
        updaterDisposable.disposeSafe()
        updaterDisposable =
            isDataSyncInProgressUseCase.execute()
                .subscribe { value ->
                    if (value != dataSyncInProgress.value!!) {
                        dataSyncInProgress.value = value
                        rideSummaryAdapter.cards.find { it is CardDataSyncStatus }?.refresh()
                    }
                }
    }

    fun removeCallbacksForCards(rideSummaryAdapter: CardsAdapter) {
        rideSummaryAdapter.cards.forEach { card ->
            if (card is CardSentSingleDetails) {
                card.setCallbacks(null)
            }
        }
        updaterDisposable.disposeSafe()
    }

    fun generateCards(rideSummaryAdapter: CardsAdapter) {
        val configuration = getRideConfigurationUseCase.execute()
        // sending status
        rideSummaryAdapter.addCard(CardDataSyncStatus(CardDataSyncStatusViewModel(dataSyncInProgress)))
        // summary will be always generated
        rideSummaryAdapter.addCard(CardRideSummary(generateSubModelForRideSummary()))
        // then monitoring device
        configuration?.let { conf ->
            rideSummaryAdapter.addCard(
                CardDevice(
                    generateSubModelForDevice(
                        conf
                    )
                )
            )
        }
        // then tolled
        configuration?.tolledConfiguration?.let {
            it.vehicle?.let { vehicle ->
                rideSummaryAdapter.addCard(
                    CardSummaryTolled(
                        generateSubModelForTolledSummary(
                            vehicle
                        )
                    )
                )
            }
            rideSummaryAdapter.addCard(CardAccount(generateSubModelForAccount(configuration)))
        }
        // then sent
        configuration?.let { conf ->
            conf.sentConfiguration?.let { sentConf ->
                if (sentConf.finishedSentList.size == 0 && sentConf.selectedSentList.size == 0) return
                rideSummaryAdapter.addCard(CardSentHeader(CardSentHeaderViewModel()))
                sentConf.finishedSentList.forEach { sent ->
                    rideSummaryAdapter.addCard(CardSentSingleDetails(generateSubModelForSent(sent)))
                }
                sentConf.selectedSentList.forEach { sent ->
                    rideSummaryAdapter.addCard(CardSentSingleDetails(generateSubModelForSent(sent)))
                }
            }
        }
        addCallbacksForCards(rideSummaryAdapter)
    }

    private fun generateSubModelForTolledSummary(configuration: CoreVehicle): CardSummaryTolledViewModel =
        CardSummaryTolledViewModel(
            MutableLiveData(configuration.brand),
            MutableLiveData(configuration.model),
            MutableLiveData(configuration.licensePlate),
            MutableLiveData(
                CoreVehicleCategory.fromInt(configuration.category).uiLiteral
            ),
            MutableLiveData(configuration.emissionClass)
        )

    private fun generateSubModelForSent(sent: CoreSent): CardSentSingleDetailsViewModel =
        CardSentSingleDetailsViewModel(
            MutableLiveData(sent.sentNumber), MutableLiveData(sent.generateTimeSpanString())
        )

    private fun generateSubModelForRideSummary(): CardRideSummaryViewModel {
        val rideStartTimestamp =
            readSettingsUseCase.executeForString(Settings.RIDE_START_TIMESTAMP).toLong()
        val rideEndTimestamp =
            readSettingsUseCase.executeForString(Settings.RIDE_END_TIMESTAMP).toLong()
        val rideLength: String =
            (rideEndTimestamp - rideStartTimestamp).formatMillisToReadableUiTime() ?: "--.--.--"
        val rideStart: String =
            TimeUtils.DateTimeFormatterForRideSummaryView.print(rideStartTimestamp)
        val rideEnd: String = TimeUtils.DateTimeFormatterForRideSummaryView.print(rideEndTimestamp)
        return CardRideSummaryViewModel(
            MutableLiveData(rideLength),
            MutableLiveData(rideStart),
            MutableLiveData(rideEnd)
        )
    }

    private fun generateSubModelForDevice(configuration: RideCoordinatorConfiguration): CardDeviceViewModel =
        CardDeviceViewModel(
            MutableLiveData(
                if (configuration.monitoringDeviceConfiguration?.monitoringByApp == false)
                    "ride_summary_monitoring_device_zsl"
                else
                    "ride_summary_monitoring_device_phone"
            )
        )

    private fun generateSubModelForAccount(configuration: RideCoordinatorConfiguration): CardAccountViewModel =
        CardAccountViewModel(
            MutableLiveData(configuration.tolledConfiguration?.vehicle?.accountInfo?.alias ?: ""),
            MutableLiveData(
                configuration.tolledConfiguration?.vehicle?.accountInfo?.id?.toString() ?: ""
            ),
            CoreAccountTypes.fromLiteral(
                configuration.tolledConfiguration?.vehicle?.accountInfo?.type ?: ""
            )
        )

    override fun onSentDetailsRequested(sentNumber: String) {
        selectedSent =
            getRideConfigurationUseCase.execute()?.sentConfiguration?.findSentByNumber(sentNumber)
        if (selectedSent != null) // fallback
            _navigate.value = RideSummaryNavTargets.FORWARD_SENT_RIDE_DETAILS
    }

    enum class RideSummaryNavTargets {
        IDLE, FORWARD_SENT_RIDE_DETAILS
    }

}

private fun SentConfiguration?.findSentByNumber(sentNumber: String): CoreSent? {
    if (this == null) return null
    finishedSentList.forEach { if (it.sentNumber.contentEquals(sentNumber)) return it }
    selectedSentList.forEach { if (it.sentNumber.contentEquals(sentNumber)) return it }
    return null
}

private fun CoreSent.generateTimeSpanString(): String {
    val start =
        TimeUtils.DefaultDateFormatterForUi.print(startTimestamp.toLong() * 1000L)
    val end =
        TimeUtils.DefaultDateFormatterForUi.print(endTimestamp.toLong() * 1000L)
    return "$start - $end"
}
