package pl.gov.mf.etoll.core.ridehistory

import io.reactivex.Completable
import io.reactivex.Single
import org.joda.time.LocalDate
import pl.gov.mf.etoll.core.ridehistory.model.HistoryItemFrameType
import pl.gov.mf.etoll.core.ridehistory.model.HistoryItemType
import pl.gov.mf.etoll.core.ridehistory.model.RideHistoryDataItem
import pl.gov.mf.etoll.storage.database.ridehistory.RideHistoryDatabaseManager
import pl.gov.mf.etoll.storage.database.ridehistory.model.RideHistoryModel
import java.util.*
import javax.inject.Inject

class RideHistoryUC {
    class ReadHistoryUseCase @Inject constructor(private val manager: RideHistoryDatabaseManager) {
        fun execute(date: LocalDate? = null): Single<List<RideHistoryDataItem>> {
            return if (date == null)
                all()
            else {
                val from = date.toInterval().start
                val to = date.toInterval().end
                period(from.millis, to.millis)
            }
        }

        private fun all(): Single<List<RideHistoryDataItem>> =
            manager.getAll()
                .flattenAsObservable { it }
                .map { it.toModel() }
                .toList()

        private fun period(from: Long, to: Long): Single<List<RideHistoryDataItem>> {
            return manager.getForPeriod(from, to)
                .flattenAsObservable { it }
                .map { it.toModel() }
                .toList()
        }
    }

    class ReadHistoryDetailsUseCase @Inject constructor(private val manager: RideHistoryDatabaseManager) {

        fun execute(id: Int): Single<RideHistoryDataItem> =
            manager.getById(id).map { it.toModel() }
    }

    class WriteHistoryUseCase @Inject constructor(private val manager: RideHistoryDatabaseManager) {
        fun execute(activity: RideHistoryDataItem): Completable =
            manager.addData(
                RideHistoryModel(
                    timestamp = activity.timestamp.time,
                    frameType = activity.historyItemFrameType.ordinal,
                    type = activity.historyItemType.ordinal,
                    additionalData = activity.additionalData
                )
            )
    }

    class DeleteObsoleteUseCase @Inject constructor(private val manager: RideHistoryDatabaseManager) {
        fun execute() = manager.deleteObsolete()
    }
}

private fun RideHistoryModel.toModel(): RideHistoryDataItem {
    // for compatibility issues with older test versions
    val maxSize = HistoryItemType.values().size - 1
    return RideHistoryDataItem(
        id = id,
        historyItemType = HistoryItemType.values()[if (type > maxSize) maxSize else type],
        historyItemFrameType = HistoryItemFrameType.values()[frameType],
        timestamp = Date(timestamp),
        additionalData = additionalData
    )
}
