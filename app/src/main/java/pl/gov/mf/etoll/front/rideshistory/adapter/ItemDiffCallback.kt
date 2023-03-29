package pl.gov.mf.etoll.front.rideshistory.adapter

import androidx.recyclerview.widget.DiffUtil

class ItemDiffCallback : DiffUtil.ItemCallback<RideHistoryCellUiItem>() {
    override fun areItemsTheSame(oldItem: RideHistoryCellUiItem, newItem: RideHistoryCellUiItem): Boolean =
        oldItem == newItem

    override fun areContentsTheSame(oldItem: RideHistoryCellUiItem, newItem: RideHistoryCellUiItem): Boolean =
        oldItem.compareContent(newItem)
}