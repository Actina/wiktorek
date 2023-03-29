package pl.gov.mf.etoll.front.configsentridesselection.adapter

import androidx.recyclerview.widget.DiffUtil

class ItemDiffCallback : DiffUtil.ItemCallback<SentRideItem>() {
    override fun areItemsTheSame(oldItem: SentRideItem, newItem: SentRideItem): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: SentRideItem, newItem: SentRideItem): Boolean {
        return oldItem.compareContent(newItem)
    }
}