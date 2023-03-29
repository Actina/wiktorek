package pl.gov.mf.etoll.core.uiinterfaces

import android.content.res.Resources
import io.reactivex.Single
import pl.gov.mf.etoll.interfaces.NotificationHistoryController
import pl.gov.mf.etoll.storage.database.notifications.NotificationsHistoryDatabase
import pl.gov.mf.etoll.storage.database.notifications.model.NotificationHistoryItemType
import javax.inject.Inject

class NotificationHistoryControllerImpl @Inject constructor(private val db: NotificationsHistoryDatabase) :
        NotificationHistoryController {
    override fun addNewItemToHistory(
            type: NotificationHistoryController.Type,
            titleResource: String,
            contentResource: String,
            iconResource: Int?,
            payloadJson: String?,
            contentExtraValue: String?,
            apiMessageId: String
    ): Single<Boolean> =
            db.addNew(
                    type.toDbType(), titleResource, contentResource, iconResource,
                    payloadJson, contentExtraValue, apiMessageId
            ).map { it >= 0 }

    override fun shouldAddNotificationToHistory(apiMessageId: String): Single<Boolean> =
        db.getByApiMessageId(apiMessageId)
            .map {
                val messageInDbValid = it.id != -1L && it.apiMessageId.isNotEmpty()
                !messageInDbValid
            }.onErrorResumeNext {
                if (it is IndexOutOfBoundsException || it is Resources.NotFoundException)
                    Single.just(true) //No message in db - we should add new one
                else
                    Single.error(it)    //Unknown error during check
            }
}

private fun NotificationHistoryController.Type.toDbType(): NotificationHistoryItemType {
    return when (this) {
        NotificationHistoryController.Type.INFO -> NotificationHistoryItemType.INFO
        NotificationHistoryController.Type.CRITICAL -> NotificationHistoryItemType.CRITICAL
        NotificationHistoryController.Type.GOOD -> NotificationHistoryItemType.GOOD
        NotificationHistoryController.Type.BAD -> NotificationHistoryItemType.BAD
        NotificationHistoryController.Type.CRM -> NotificationHistoryItemType.CRM
    }
}
