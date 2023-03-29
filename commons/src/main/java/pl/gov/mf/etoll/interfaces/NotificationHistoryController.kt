package pl.gov.mf.etoll.interfaces

import io.reactivex.Single

interface NotificationHistoryController {

    fun addNewItemToHistory(
        type: Type,
        titleResource: String,
        contentResource: String,
        iconResource: Int? = null,
        payloadJson: String? = null,
        contentExtraValue: String? = null,
        apiMessageId: String = ""
    ): Single<Boolean>

    fun shouldAddNotificationToHistory(apiMessageId: String): Single<Boolean>

    enum class Type {
        INFO,
        CRITICAL,
        GOOD,
        BAD,
        CRM
    }
}