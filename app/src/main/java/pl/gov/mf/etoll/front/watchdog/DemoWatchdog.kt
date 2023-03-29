package pl.gov.mf.etoll.front.watchdog

import io.reactivex.Observable
import org.joda.time.DateTime
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.commons.TimeUtils
import pl.gov.mf.etoll.core.ridecoordinatorv2.events.EventType
import pl.gov.mf.etoll.networking.api.model.EventStreamBaseEvent
import pl.gov.mf.etoll.networking.api.model.EventStreamLocation
import pl.gov.mf.etoll.networking.api.model.EventStreamLocationWithoutLocation
import pl.gov.mf.mobile.utils.toObject

interface DemoWatchdog {
    fun start()

    fun observeChanges(): Observable<List<DemoListItem>>
}

data class DemoListItem(
    val type: DemoListItemType,
    val data: EventStreamBaseEvent?,
    val error: String?,
    var info: String?,
    val amountOfSentData: Int?,
    val timestamp: String,
    val sentIdsList: List<String>? = null
) {
    companion object {
        fun generateInfo(text: String) =
            DemoListItem(
                DemoListItemType.INFO,
                null,
                null,
                text,
                null,
                TimeUtils.DefaultDateTimeFormatterForUi.print(
                    DateTime()
                )
            )

        fun generateError(text: String) =
            DemoListItem(
                DemoListItemType.ERROR,
                null,
                text,
                null,
                null,
                TimeUtils.DefaultDateTimeFormatterForUi.print(
                    DateTime()
                )
            )

        fun generateLocationEvent(type: EventType, data: String, sent: Boolean) =
            DemoListItem(
                if (sent) DemoListItemType.COLLECTED_DATA_SENT else DemoListItemType.COLLECTED_DATA,
                if (data.contains("longitude")) data.toObject<EventStreamLocation>() else data.toObject<EventStreamLocationWithoutLocation>(),
                null,
                null,
                null,
                TimeUtils.DefaultDateTimeFormatterForUi.print(DateTime())
            )

        fun generateSendingEvent(
            amount: Int,
            ids: List<String>,
            sent: Boolean
        ) =
            DemoListItem(
                if (sent) DemoListItemType.DATA_SENT_TRY_SENT else DemoListItemType.DATA_SENT_TRY,
                null,
                null,
                null,
                amount,
                TimeUtils.DefaultDateTimeFormatterForUi.print(DateTime()),
                ids
            )
    }
}

enum class DemoListItemType(val layout: Int) {
    COLLECTED_DATA(R.layout.row_demoactivity_collecteddata),
    COLLECTED_DATA_SENT(R.layout.row_demoactivity_collecteddata_sent),
    DATA_SENT_TRY(R.layout.row_demoactivity_sentinfo),
    DATA_SENT_TRY_SENT(R.layout.row_demoactivity_sentinfo_sent),
    ERROR(R.layout.row_demoactivity_error),
    INFO(R.layout.row_demoactivity_info),
    START_ETOLL(R.layout.row_demoactivity_info),
    START_SENT(R.layout.row_demoactivity_info)
}