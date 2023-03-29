package pl.gov.mf.etoll.front.rideshistory.details.adapter

import androidx.recyclerview.widget.DiffUtil

class ItemDiffCallback : DiffUtil.ItemCallback<RideHistoryCellItem>() {
    override fun areItemsTheSame(oldItem: RideHistoryCellItem, newItem: RideHistoryCellItem): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: RideHistoryCellItem, newItem: RideHistoryCellItem): Boolean {
        return oldItem == newItem
    }
}