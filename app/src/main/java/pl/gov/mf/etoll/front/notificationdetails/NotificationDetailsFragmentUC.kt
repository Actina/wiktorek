package pl.gov.mf.etoll.front.notificationdetails

import io.reactivex.Single
import pl.gov.mf.etoll.storage.database.notifications.NotificationsHistoryDatabase
import pl.gov.mf.etoll.storage.database.notifications.model.NotificationHistoryItemModel
import javax.inject.Inject

sealed class NotificationDetailsFragmentUC {
    class GetNotificationUseCase @Inject constructor(private val ds: NotificationsHistoryDatabase) :
        NotificationDetailsFragmentUC() {
        fun execute(id: Long): Single<NotificationHistoryItemModel> = ds.getById(id)
    }
}