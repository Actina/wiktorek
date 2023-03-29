package pl.gov.mf.etoll.front.notificationhistory

import io.reactivex.Completable
import io.reactivex.Single
import pl.gov.mf.etoll.core.crmmessages.CrmMessagesManager
import pl.gov.mf.etoll.storage.database.notifications.NotificationsHistoryDatabase
import pl.gov.mf.etoll.storage.database.notifications.model.NotificationHistoryItemModel
import javax.inject.Inject

sealed class NotificationHistoryFragmentUC {

    class GetNotificationsHistoryUseCase @Inject constructor(private val ds: NotificationsHistoryDatabase) :
        NotificationHistoryFragmentUC() {

        fun execute(): Single<List<NotificationHistoryItemModel>> = ds.getAllSorted()
    }

    class ClearNotificationsHistoryUseCase @Inject constructor(private val ds: NotificationsHistoryDatabase) :
        NotificationHistoryFragmentUC() {

        fun execute(): Completable = ds.clear()
    }

    class DeleteNotificationsUseCase @Inject constructor(private val ds: NotificationsHistoryDatabase) :
        NotificationHistoryFragmentUC() {

        fun execute(data: List<NotificationHistoryItemModel>): Completable =
            ds.remove(data.map { it.id!! }.toTypedArray())
    }

    class DeleteNotificationUseCase @Inject constructor(private val ds: NotificationsHistoryDatabase) :
        NotificationHistoryFragmentUC() {

        fun execute(id: Long): Single<Boolean> = ds.remove(id)
    }

    class ObserveNewNotificationsUseCase @Inject constructor(private val ds: CrmMessagesManager) :
        NotificationHistoryFragmentUC() {
        fun startObservation(callbacks: CrmMessagesManager.Callbacks) =
            ds.setCallbacks(callbacks, TAG)

        fun stopObservation() =
            ds.removeCallbacks(TAG)

        companion object {
            private const val TAG = "notificationHist"
        }
    }
}