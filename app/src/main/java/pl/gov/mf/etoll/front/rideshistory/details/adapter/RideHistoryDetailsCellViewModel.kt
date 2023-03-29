package pl.gov.mf.etoll.front.rideshistory.details.adapter

import pl.gov.mf.etoll.commons.TimeUtils
import pl.gov.mf.etoll.core.model.CoreSent
import pl.gov.mf.etoll.core.model.CoreVehicleCategory
import pl.gov.mf.etoll.core.ridehistory.model.ActivityAdditionalData
import pl.gov.mf.etoll.utils.formatAccountBalanceText
import pl.gov.mf.mobile.ui.components.adapters.TranslationsAdapter
import pl.gov.mf.mobile.ui.components.adapters.TranslationsAdapter.toConditionallyTranslatedText
import java.util.*

sealed class RideHistoryDetailsCellViewModel {

    data class RideTimeItemCellViewModel(private val date: Date, val untranslatedHeader: String) :
        RideHistoryDetailsCellViewModel() {
        val rideStartDate: String
            get() = TimeUtils.DefaultDateFormatterForNotificationsHistory.print(date.time)

        val rideStartTime: String =
            TimeUtils.DefaultDateFormatterForNotificationsHistoryHoursSeconds.print(date.time)
    }

    data class LocationReportItemCellViewModel(private val monitoringByApp: Boolean) :
        RideHistoryDetailsCellViewModel() {
        val mainTrackingDevice: String
            get() = if (monitoringByApp) "ride_summary_monitoring_device_phone" else "ride_summary_monitoring_device_zsl"
    }

    data class VehicleItemCellViewModel(private val rideSnapshot: ActivityAdditionalData.RideSnapshot) :
        RideHistoryDetailsCellViewModel() {

        val accountBalance: TranslationsAdapter.ConditionallyTranslatedText
            get() = rideSnapshot.accountInfo?.run {
                formatAccountBalanceText(balanceValue, balanceIsInitialized, type)
            } ?: "".toConditionallyTranslatedText()

        val emissionClass: String
            get() = rideSnapshot.vehicle?.emissionClass ?: ""

        val brand: String
            get() = rideSnapshot.vehicle?.brand ?: ""

        val model: String
            get() = rideSnapshot.vehicle?.model ?: ""

        val licensePlate: String
            get() = rideSnapshot.vehicle?.licensePlate ?: ""

        val untranslatedVehicleCategory: String
            get() = CoreVehicleCategory.fromInt(rideSnapshot.vehicle?.category ?: 0).uiLiteral
    }

    data class CategoryWeightExceededItemCellViewModel(private val rideSnapshot: ActivityAdditionalData.RideSnapshot) :
        RideHistoryDetailsCellViewModel() {
        val categoryExceededVisible: Boolean
            get() = rideSnapshot.categoryIncrease

        val untranslatedCategoryExceededValue: String
            get() = rideSnapshot.categoryIncrease.let {
                if (it) "ride_history_details_toll_category_exceeding_yes" else "ride_history_details_toll_category_exceeding_no"
            }
    }

    data class SentNumberItemCellViewModel(
        private val index: Int,
        private val sentNumber: String,
        val isLastNumberOnList: Boolean
    ) : RideHistoryDetailsCellViewModel() {
        val sentNumberHtml: String
            get() = "${index + 1}. <b>$sentNumber</b>"
    }

    class ConnectedSentRidesHeaderCellViewModel(private val selectedSentList: MutableList<CoreSent>? = null) :
        RideHistoryDetailsCellViewModel() {
        val connectedSentRidesHeaderVisible: Boolean
            get() = !selectedSentList.isNullOrEmpty()
    }
}