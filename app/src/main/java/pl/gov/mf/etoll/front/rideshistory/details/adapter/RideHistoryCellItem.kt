package pl.gov.mf.etoll.front.rideshistory.details.adapter

import android.content.Context
import androidx.core.text.HtmlCompat
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.front.rideshistory.details.adapter.RideHistoryDetailsCellViewModel.*

sealed class RideHistoryCellItem(val layoutId: Int) {

    data class RideTimeItem(val viewModel: RideTimeItemCellViewModel) :
        RideHistoryCellItem(R.layout.item_history_details_time)

    data class LocationReportItem(val viewModel: LocationReportItemCellViewModel) :
        RideHistoryCellItem(R.layout.item_history_details_location_report)

    data class VehicleItem(val viewModel: VehicleItemCellViewModel) :
        RideHistoryCellItem(R.layout.item_ride_history_details_vehicle)

    data class CategoryWeightExceededItem(val viewModel: CategoryWeightExceededItemCellViewModel) :
        RideHistoryCellItem(R.layout.item_history_details_category_weight_exceeded)

    data class ConnectedSentRidesHeader(val viewModel: ConnectedSentRidesHeaderCellViewModel) :
        RideHistoryCellItem(R.layout.item_history_details_connected_sent_rides_header)

    data class SendNumberItem(val context: Context, val viewModel: SentNumberItemCellViewModel) :
        RideHistoryCellItem(R.layout.item_history_sent_number) {

        val sentNumberRow = HtmlCompat.fromHtml(
            viewModel.sentNumberHtml,
            HtmlCompat.FROM_HTML_MODE_COMPACT
        )

        val sentItemBottomPadding = (
                if (viewModel.isLastNumberOnList) R.dimen.xl_spacing else R.dimen.s_spacing
                ).let { context.resources.getDimension(it) }
    }

}