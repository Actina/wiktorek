package pl.gov.mf.etoll.front.notificationhistory.list

import pl.gov.mf.etoll.storage.database.notifications.model.NotificationHistoryItemModel

interface NotificationHistoryItemOnClickListener {
    fun onItemClicked(item: NotificationHistoryItemModel)
}