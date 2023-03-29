package pl.gov.mf.etoll.front.notificationhistory.list

import androidx.recyclerview.widget.DiffUtil

class ItemDiffCallback : DiffUtil.ItemCallback<NotificationHistoryItem>() {
    override fun areItemsTheSame(
        oldItem: NotificationHistoryItem,
        newItem: NotificationHistoryItem
    ): Boolean {
        return oldItem.areItemTheSame(newItem)
    }

    override fun areContentsTheSame(
        oldItem: NotificationHistoryItem,
        newItem: NotificationHistoryItem
    ): Boolean {
        return oldItem == newItem
    }
}