package pl.gov.mf.etoll.front.rideshistory.adapter


import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.appmode.AppMode
import pl.gov.mf.etoll.core.app.NkspoApplication
import pl.gov.mf.etoll.core.ridehistory.model.HistoryItemFrameType
import pl.gov.mf.etoll.core.ridehistory.model.HistoryItemFrameType.COLOR_GREEN
import pl.gov.mf.etoll.core.ridehistory.model.HistoryItemFrameType.COLOR_RED
import pl.gov.mf.etoll.front.rideshistory.adapter.RideHistoryCellViewModel.*
import pl.gov.mf.mobile.utils.translate

sealed class RideHistoryCellUiItem(val layoutId: Int) {

    private val _frameColor = MutableLiveData(R.color.transparent_dark)
    val frameColor: LiveData<Int> = _frameColor

    protected fun updateFrameColor(frameType: HistoryItemFrameType, darkMode: Boolean) {
        val color = when (frameType) {
            COLOR_GREEN -> if (darkMode) R.color.ride_history_frame_green_dark else R.color.ride_history_frame_green_light
            COLOR_RED -> if (darkMode) R.color.ride_history_frame_red_dark else R.color.ride_history_frame_red_light
            else -> R.color.transparent_dark
        }
        _frameColor.postValue(color)
    }

    open fun compareContent(item: RideHistoryCellUiItem): Boolean = false
}

data class RideHistoryHeaderUiItem(val rideHistoryHeaderItemViewModel: RideHistoryHeaderItemViewModel) :
    RideHistoryCellUiItem(R.layout.item_rides_history_header) {
    override fun compareContent(item: RideHistoryCellUiItem): Boolean =
        if (item is RideHistoryHeaderUiItem)
            rideHistoryHeaderItemViewModel.date == item.rideHistoryHeaderItemViewModel.date
        else false
}

data class RideHistoryDisruptionUiItem(
    val context: Context,
    val rideHistoryDisruptionItemViewModel: RideHistoryDisruptionItemViewModel
) :
    RideHistoryCellUiItem(R.layout.item_ride_history_disruption_item) {

    init {
        val darkMode = (context.applicationContext as NkspoApplication).getApplicationComponent()
            .useCaseGetCurrentAppMode().execute() == AppMode.DARK_MODE
        updateFrameColor(rideHistoryDisruptionItemViewModel.data.historyItemFrameType, darkMode)
    }

    override fun compareContent(item: RideHistoryCellUiItem): Boolean =
        if (item is RideHistoryDisruptionUiItem)
            rideHistoryDisruptionItemViewModel.data == item.rideHistoryDisruptionItemViewModel.data
        else false
}

data class RideHistoryEventUiItem(
    val context: Context,
    val rideHistoryEventItemViewModel: RideHistoryEventItemViewModel
) : RideHistoryCellUiItem(R.layout.item_ride_history_event_item) {

    var detailsClickListener: OnRideDetailsClickListener? = null


    init {
        val darkMode = (context.applicationContext as NkspoApplication).getApplicationComponent()
            .useCaseGetCurrentAppMode().execute() == AppMode.DARK_MODE
        updateFrameColor(rideHistoryEventItemViewModel.data.historyItemFrameType, darkMode)
    }

    val translatedSubtitle: String = with(rideHistoryEventItemViewModel.subtitleToTranslate) {
        when {
            mainTagToTranslate.isBlank() -> ""
            valueToInsertToMainTag.isNullOrEmpty() -> mainTagToTranslate.translate(context)
            !valueToInsertToMainTag.isNullOrEmpty() ->
                mainTagToTranslate.translate(
                    context,
                    if (translateInsertingValue) valueToInsertToMainTag.translate(context) else valueToInsertToMainTag
                )
            else -> ""
        }
    }

    fun onDetailsClick() {
        detailsClickListener?.onRideDetailsClick(rideHistoryEventItemViewModel.data)
    }

    override fun compareContent(item: RideHistoryCellUiItem): Boolean =
        if (item is RideHistoryEventUiItem)
            rideHistoryEventItemViewModel.data == item.rideHistoryEventItemViewModel.data
        else false

}