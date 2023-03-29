package pl.gov.mf.etoll.front.rideshistory.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pl.gov.mf.etoll.base.BaseDatabindingViewModel
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorV3Callbacks.RideType.SENT
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorV3Callbacks.RideType.TOLLED
import pl.gov.mf.etoll.core.ridehistory.RideHistoryUC
import pl.gov.mf.etoll.core.ridehistory.model.ActivityAdditionalData
import pl.gov.mf.etoll.core.ridehistory.model.HistoryItemType.RIDE_END
import pl.gov.mf.etoll.core.ridehistory.model.HistoryItemType.RIDE_START
import pl.gov.mf.etoll.core.ridehistory.model.RideHistoryDataItem
import pl.gov.mf.etoll.core.ridehistory.model.getTitle
import pl.gov.mf.etoll.front.rideshistory.details.adapter.RideHistoryDetailsCellViewModel
import pl.gov.mf.etoll.front.rideshistory.details.adapter.RideHistoryDetailsCellViewModel.*
import pl.gov.mf.mobile.utils.addSafe
import javax.inject.Inject

class RideHistoryDetailsFragmentViewModel : BaseDatabindingViewModel() {
    @Inject
    lateinit var readHistoryDetailsUC: RideHistoryUC.ReadHistoryDetailsUseCase

    private val _title = MutableLiveData<String>()
    val title: LiveData<String> = _title

    private val _detailsCellsViewModels = MutableLiveData<List<RideHistoryDetailsCellViewModel>>()
    val detailsCellsViewModels: LiveData<List<RideHistoryDetailsCellViewModel>> =
        _detailsCellsViewModels

    fun loadRideHistoryDetails(id: Int) {
        compositeDisposable.addSafe(
            readHistoryDetailsUC.execute(id)
                .subscribe(
                    { rideHistoryDataItem ->
                        rideHistoryDataItem.additionalData<ActivityAdditionalData.RideSnapshot>()
                            ?.let { it ->
                                _title.value =
                                    rideHistoryDataItem.historyItemType.getTitle(it.rideType)
                                _detailsCellsViewModels.value =
                                    initDetailsCellsViewModels(it, rideHistoryDataItem)
                            }
                    },
                    {
                        /* error handling */
                    }
                )
        )
    }

    private fun initDetailsCellsViewModels(
        rideSnapshot: ActivityAdditionalData.RideSnapshot,
        historyItem: RideHistoryDataItem,
    ): List<RideHistoryDetailsCellViewModel> {
        val result = mutableListOf<RideHistoryDetailsCellViewModel>()

        val untranslatedHeader = when (historyItem.historyItemType) {
            RIDE_START -> "ride_history_ride_started"
            RIDE_END -> "ride_history_ride_ended"
            else -> ""
        }
        when (rideSnapshot.rideType) {
            TOLLED -> {
                result.add(RideTimeItemCellViewModel(historyItem.timestamp, untranslatedHeader))
                result.add(LocationReportItemCellViewModel(rideSnapshot.monitoringData.byApp))
                result.add(VehicleItemCellViewModel(rideSnapshot))
                result.add(CategoryWeightExceededItemCellViewModel(rideSnapshot))
            }
            SENT -> {
                result.add(RideTimeItemCellViewModel(historyItem.timestamp, untranslatedHeader))
                result.add(LocationReportItemCellViewModel(rideSnapshot.monitoringData.byApp))
                result.add(ConnectedSentRidesHeaderCellViewModel(rideSnapshot.selectedSentList))
                rideSnapshot.selectedSentList?.forEachIndexed { index, coreSent ->
                    result.add(
                        SentNumberItemCellViewModel(
                            index, coreSent.sentNumber,
                            index == rideSnapshot.selectedSentList?.lastIndex
                        )
                    )
                }
            }
        }

        return result
    }
}