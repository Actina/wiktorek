package pl.gov.mf.etoll.front.notificationhistory.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.joda.time.DateTime
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.commons.TimeUtils
import pl.gov.mf.etoll.storage.database.notifications.model.NotificationHistoryItemModel
import pl.gov.mf.etoll.storage.database.notifications.model.NotificationHistoryItemType
import pl.gov.mf.etoll.ui.components.dialogs.crm.CrmDialogFragment

sealed class NotificationHistoryItem(val layoutId: Int) {

    class HeaderItem(val date: String) :
        NotificationHistoryItem(R.layout.item_notification_history_header) {

        override fun areItemTheSame(item: NotificationHistoryItem): Boolean =
            item is HeaderItem && date == date

    }

    class DataItem(
        val data: NotificationHistoryItemModel,
        val listener: NotificationHistoryItemOnClickListener,
    ) : NotificationHistoryItem(R.layout.item_notification_history) {

        val viewModel = ViewModel(data)

        fun enableSelectMode(enabled: Boolean) {
            viewModel.setSelectModeEnabled(enabled)
        }

        fun setSelected(selected: Boolean) {
            viewModel.selected.value = selected
        }

        fun isSelected(): Boolean = viewModel.selected.value == true

        override fun areItemTheSame(item: NotificationHistoryItem): Boolean =
            item is DataItem && this.viewModel.data.id == item.viewModel.data.id

        inner class ViewModel(val data: NotificationHistoryItemModel) {
            private val _selectMode = MutableLiveData(false)
            val selectMode: LiveData<Boolean> = _selectMode

            val selected = MutableLiveData(false)

            val contentValue: Pair<String, String?>
                get() = data.run { Pair(content, contentExtraValue) }

            fun setSelectModeEnabled(enabled: Boolean) {
                _selectMode.value = enabled
            }

            fun onItemClick() {
                if (selectMode.value == true)
                    selected.value = selected.value == false
                else
                    listener.onItemClicked(data)
            }

            fun calculatedTimeDiffToNow(): String {
                // as for now we will just show time
                return TimeUtils.DefaultDateFormatterForNotificationsHistoryHours.print(
                    DateTime(data.timestamp)
                )
            }

            val tileColor: String = when (data.notificationHistoryItemType) {
                NotificationHistoryItemType.INFO -> "bg_leftrounded_notif_default"
                NotificationHistoryItemType.CRITICAL -> "bg_leftrounded_notif_critical"
                NotificationHistoryItemType.GOOD -> "bg_leftrounded_notif_good"
                NotificationHistoryItemType.BAD -> "bg_leftrounded_notif_bad"
                NotificationHistoryItemType.CRM -> data.payload?.backofficeMessageType?.let {
                    when (CrmDialogFragment.MessageType.from(it)) {
                        CrmDialogFragment.MessageType.INFO -> "bg_leftrounded_notif_crm_blue"
                        CrmDialogFragment.MessageType.WARNING -> "bg_leftrounded_notif_crm_warning"
                        CrmDialogFragment.MessageType.CRITICAL -> "bg_leftrounded_notif_bad"
                    }
                } ?: "bg_leftrounded_notif_crm_blue"
            }
        }
    }

    open fun areItemTheSame(item: NotificationHistoryItem): Boolean = false
}