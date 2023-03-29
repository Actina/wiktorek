package pl.gov.mf.etoll.storage.database.notifications

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import pl.gov.mf.etoll.storage.database.notifications.model.NotificationHistoryItemModel
import pl.gov.mf.etoll.storage.database.notifications.model.NotificationHistoryItemType

interface NotificationsHistoryDatabase {
    fun getAllSorted(): Single<List<NotificationHistoryItemModel>>

    fun remove(id: Long): Single<Boolean>

    fun addNew(
        type: NotificationHistoryItemType,
        titleRes: String,
        contentRes: String,
        iconRes: Int? = null,
        payloadJson: String? = null,
        contentExtraValue: String? = null,
        apiMessageId: String = "",
    ): Single<Long>

    fun getById(id: Long): Single<NotificationHistoryItemModel>

    fun getByApiMessageId(apiMessageId: String): Single<NotificationHistoryItemModel>

    fun clear(): Completable

    fun remove(ids: Array<Long>): Completable
}