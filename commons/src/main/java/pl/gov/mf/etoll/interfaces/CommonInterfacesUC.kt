package pl.gov.mf.etoll.interfaces

import io.reactivex.Single
import javax.inject.Inject

sealed class CommonInterfacesUC {
    class AddNotificationToHistoryUseCase @Inject constructor(private val ds: NotificationHistoryController) :
        CommonInterfacesUC() {
        fun execute(
            type: NotificationHistoryController.Type,
            titleResource: String,
            contentResource: String,
            iconResource: Int? = null,
            payloadJson: String? = null,
            contentExtraValue: String? = null,
            apiMessageId: String = ""
        ): Single<Boolean> = ds.addNewItemToHistory(
            type,
            titleResource,
            contentResource,
            iconResource,
            payloadJson,
            contentExtraValue,
            apiMessageId
        )
    }

    class CheckIfShouldAddNotificationToHistory @Inject constructor(private val ds: NotificationHistoryController) :
        CommonInterfacesUC() {
        fun execute(apiMessageId: String): Single<Boolean> =
            ds.shouldAddNotificationToHistory(apiMessageId)
    }
}