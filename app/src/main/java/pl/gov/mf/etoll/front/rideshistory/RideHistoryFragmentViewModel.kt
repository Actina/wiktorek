package pl.gov.mf.etoll.front.rideshistory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import org.joda.time.LocalDate
import pl.gov.mf.etoll.base.BaseDatabindingViewModel
import pl.gov.mf.etoll.core.ridehistory.RideHistoryUC
import pl.gov.mf.etoll.core.ridehistory.model.HistoryItemType.*
import pl.gov.mf.etoll.core.ridehistory.model.RideHistoryDataItem
import pl.gov.mf.etoll.core.ridehistory.model.formattedDate
import pl.gov.mf.etoll.front.rideshistory.adapter.RideHistoryCellViewModel
import pl.gov.mf.etoll.front.rideshistory.adapter.RideHistoryCellViewModel.*
import pl.gov.mf.mobile.utils.addSafe
import javax.inject.Inject

class RideHistoryFragmentViewModel : BaseDatabindingViewModel() {

    @Inject
    lateinit var readHistoryUseCaseUC: RideHistoryUC.ReadHistoryUseCase

    @Inject
    lateinit var writeHistoryUseCaseUC: RideHistoryUC.WriteHistoryUseCase

    private val _navigate: MutableLiveData<NavigationTargets> =
        MutableLiveData(NavigationTargets.Current)
    val navigate: LiveData<NavigationTargets>
        get() = _navigate

    private val _viewData: MutableLiveData<ViewData> = MutableLiveData()
    val viewData: LiveData<ViewData> = _viewData

    var historyDate: LocalDate? = null
        private set

    var firstHistoryDate: LocalDate? = null
        private set

    var lastHistoryDate: LocalDate? = null
        private set

    fun resetNavigation() {
        _navigate.value = NavigationTargets.Current
    }

    fun loadHistory() {
        _viewData.value = ViewData(ViewState.LOADING)

        compositeDisposable.addSafe(
            readHistoryUseCaseUC.execute(historyDate)
                .flatMap { it.sort().toRideHistoryCellViewModels() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { rideHistoryCellViewModels ->
                        _viewData.value =
                            ViewData(ViewState.HISTORY_LOADED, rideHistoryCellViewModels)
                    },
                    {
                        _viewData.value = ViewData(ViewState.ERROR)
                    }
                ))
    }

    private fun List<RideHistoryDataItem>.sort(): List<RideHistoryDataItem> =
        sortedWith { o1, o2 ->
            if (o1 == null && o2 == null) 0
            else if (o2 == null) 1
            else if (o1 == null) -1
            else o2.timestamp.time.compareTo(o1.timestamp.time)
        }

    private fun List<RideHistoryDataItem>.toRideHistoryCellViewModels() =
        Single.create<List<RideHistoryCellViewModel>> { emitter ->
            val result = mutableListOf<RideHistoryCellViewModel>()
            var currentSectionDate = ""
            val firstActivity = this.firstOrNull()
            val lastActivity = this.lastOrNull()
            // grab these two just once, warning - sorting changed(!)
            if (firstHistoryDate == null) lastActivity?.let {
                firstHistoryDate = LocalDate(DateTime(it.timestamp).withDayOfMonth(1))
            }
            if (lastHistoryDate == null) firstActivity?.let {
                lastHistoryDate = LocalDate(DateTime(it.timestamp))
            }
            this.forEachIndexed { index, rideHistoryDataItem ->
                createHeaderViewModel(rideHistoryDataItem, currentSectionDate)?.run {
                    currentSectionDate = rideHistoryDataItem.formattedDate()
                    result.add(this)
                }

                val hasPrevious =
                    firstActivity != rideHistoryDataItem
                            && this[index - 1].formattedDate() == currentSectionDate
                            && rideHistoryDataItem.historyItemType != RIDE_END

                val hasNext =
                    lastActivity != rideHistoryDataItem
                            && this[index + 1].formattedDate() == currentSectionDate
                            && this[index + 1].historyItemType != RIDE_END

                result.add(
                    createRideHistoryItemViewModel(
                        rideHistoryDataItem,
                        hasPrevious,
                        hasNext
                    )
                )
            }
            emitter.onSuccess(result)
        }.subscribeOn(Schedulers.io())

    private fun createHeaderViewModel(
        rideHistoryDataItem: RideHistoryDataItem,
        currentSectionDate: String
    ) = when {
        currentSectionDate != rideHistoryDataItem.formattedDate() -> RideHistoryHeaderItemViewModel(
            rideHistoryDataItem.formattedDate()
        )
        else -> null
    }

    private fun createRideHistoryItemViewModel(
        rideHistoryDataItem: RideHistoryDataItem,
        hasPrevious: Boolean,
        hasNext: Boolean
    ) = when (rideHistoryDataItem.historyItemType) {
        BATTERY_LOW,
        BATTERY_GOOD,
        GPS_LOW,
        GPS_GOOD,
        INTERNET_LOW,
        INTERNET_GOOD -> RideHistoryDisruptionItemViewModel(
            rideHistoryDataItem,
            hasNext,
            hasPrevious
        )
        else -> RideHistoryEventItemViewModel(
            rideHistoryDataItem,
            hasNext,
            hasPrevious
        )
    }

    fun updateHistoryDate(date: Long?) {
        historyDate = if (date == null) null else LocalDate(DateTime(date))
        loadHistory()
    }

    data class ViewData(
        val state: ViewState,
        val rideHistoryCellsViewModels: List<RideHistoryCellViewModel> = emptyList(),
    ) {
        val isHistoryListVisible =
            rideHistoryCellsViewModels.isNotEmpty() && state == ViewState.HISTORY_LOADED

        val isNoHistoryDataInfoVisible = !isHistoryListVisible
    }

    enum class ViewState {
        HISTORY_LOADED,
        LOADING,
        ERROR
    }
}


sealed class NavigationTargets {
    object Current : NavigationTargets()
    data class Details(val item: RideHistoryDataItem) : NavigationTargets()
}