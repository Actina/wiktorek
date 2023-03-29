package pl.gov.mf.etoll.front.rideshistory.adapter

import androidx.annotation.DrawableRes
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.core.ridehistory.model.*
import pl.gov.mf.etoll.core.ridehistory.model.HistoryItemType.*

sealed class RideHistoryCellViewModel {

    data class RideHistoryHeaderItemViewModel(val date: String) : RideHistoryCellViewModel()

    data class RideHistoryDisruptionItemViewModel(
        val data: RideHistoryDataItem,
        val hasNext: Boolean,
        val hasPrevious: Boolean
    ) : RideHistoryCellViewModel() {

        val formattedTime = data.formattedTime()

        val titleToTranslate: String = data.run { historyItemType.getTitle() }

        @DrawableRes
        val imageRes: Int = when (data.historyItemType) {
            BATTERY_GOOD,
            GPS_GOOD,
            INTERNET_GOOD -> R.drawable.ic_ride_history_status_good_light

            BATTERY_LOW,
            GPS_LOW,
            INTERNET_LOW -> R.drawable.ic_ride_history_status_bad_light
            else -> R.drawable.ic_ride_history_status_good_light  // this should never occur
        }
    }

    data class RideHistoryEventItemViewModel(
        val data: RideHistoryDataItem,
        val hasNext: Boolean,
        val hasPrevious: Boolean
    ) : RideHistoryCellViewModel() {
        val showInfoButton: Boolean = data.additionalData.isNotBlank() &&
                (data.historyItemType == RIDE_START || data.historyItemType == RIDE_END)
        val isFrameVisible: Boolean = data.historyItemFrameType != HistoryItemFrameType.NO_FRAME
        val formattedTime = data.formattedTime()
        val titleToTranslate: String = data.run {
            historyItemType.getTitle(additionalData<ActivityAdditionalData.RideSnapshot>()?.rideType)
        }

        val subtitleToTranslate: Subtitle =
            when (data.historyItemType) {
                RIDE_START -> Subtitle(mainTagToTranslate = "ride_history_ride_started")
                RIDE_END -> Subtitle(mainTagToTranslate = "ride_history_ride_ended")
                SENT_START -> Subtitle(
                    mainTagToTranslate = "ride_history_sent_start_android",
                    valueToInsertToMainTag = data.additionalData<ActivityAdditionalData.SentStartSnapshot>()?.sentNumber
                )
                SENT_END -> Subtitle(
                    mainTagToTranslate = "ride_history_sent_stop_android",
                    valueToInsertToMainTag = data.additionalData<ActivityAdditionalData.SentStartSnapshot>()?.sentNumber
                )
                SENT_CANCEL -> Subtitle(
                    mainTagToTranslate = "ride_history_sent_cancel_android",
                    valueToInsertToMainTag = data.additionalData<ActivityAdditionalData.SentStartSnapshot>()?.sentNumber
                )
                MONITORING_DEVICE_CHANGED -> Subtitle(
                    mainTagToTranslate = "ride_history_monitoring_device_change_android",
                    valueToInsertToMainTag = if (data.additionalData<ActivityAdditionalData.MonitoringData>()!!.byApp) "ride_history_monitoring_device_phone"
                    else "ride_history_monitoring_device_zsl",
                    translateInsertingValue = true
                )
                TRAILER_WEIGHT_EXCEEDED -> Subtitle(mainTagToTranslate = "ride_history_trailer_weight_exceeded")
                TRAILER_WEIGHT_NOT_EXCEEDED -> Subtitle(mainTagToTranslate = "ride_history_trailer_weight_notexceeded")
                PRE_PAID_TOP_UP -> Subtitle(
                    mainTagToTranslate = "ride_history_top_up_amount_android",
                    valueToInsertToMainTag = if (data.additionalData<ActivityAdditionalData.PrePaidAccountTopUp>()!!.isInitialized)
                        data.additionalData<ActivityAdditionalData.PrePaidAccountTopUp>()!!.amountAndCurrency else "-"
                )
                FAKE_GPS_DETECTED -> Subtitle(mainTagToTranslate = "dialog_fakegps_content_android")
                else -> Subtitle()
            }

        data class Subtitle(
            val mainTagToTranslate: String = "",
            val valueToInsertToMainTag: String? = null,
            val translateInsertingValue: Boolean = false
        )
    }

}