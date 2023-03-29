package pl.gov.mf.etoll.storage.database.notifications.model

import io.realm.RealmObject

open class NotificationHistoryItemRealmModel(
    var id: Long? = -1,
    var type: Int = NotificationHistoryItemType.INFO.toInt(),
    var titleResource: String = "",
    var contentResource: String = "",
    var timestamp: Long = 0L,
    var iconResource: Int? = null,
    var payloadJson: String? = null,
    var contentExtraValue: String? = null,
    var messageId: String="",
) : RealmObject()