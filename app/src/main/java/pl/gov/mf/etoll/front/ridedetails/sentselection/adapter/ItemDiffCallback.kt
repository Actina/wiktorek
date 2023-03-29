package pl.gov.mf.etoll.front.ridedetails.sentselection.adapter

import androidx.recyclerview.widget.DiffUtil

class ItemDiffCallback : DiffUtil.ItemCallback<SentRideItem>() {
    override fun areItemsTheSame(oldItem: SentRideItem, newItem: SentRideItem): Boolean {
        return oldItem.areItemTheSame(newItem)
    }

    override fun areContentsTheSame(oldItem: SentRideItem, newItem: SentRideItem): Boolean {
        return oldItem == newItem
    }
}