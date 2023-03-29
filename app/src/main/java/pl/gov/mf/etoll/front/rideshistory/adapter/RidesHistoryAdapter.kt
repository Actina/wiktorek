package pl.gov.mf.etoll.front.rideshistory.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ListAdapter

class RidesHistoryAdapter :
    ListAdapter<RideHistoryCellUiItem, RidesHistoryViewHolder<RideHistoryCellUiItem>>(
        ItemDiffCallback()) {
    lateinit var lifecycleOwner: LifecycleOwner
    private var invalidateBindings: Boolean = false

    override fun getItemViewType(position: Int): Int {
        return getItem(position).layoutId
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RidesHistoryViewHolder<RideHistoryCellUiItem> {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ViewDataBinding = DataBindingUtil.inflate(inflater, viewType, parent, false)
        binding.lifecycleOwner = lifecycleOwner
        return RidesHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: RidesHistoryViewHolder<RideHistoryCellUiItem>,
        position: Int,
    ) {
        holder.bind(getItem(position))
        invalidateBindings(holder, position)
    }

    private fun invalidateBindings(
        holder: RidesHistoryViewHolder<RideHistoryCellUiItem>,
        position: Int,
    ) {
        if (invalidateBindings) {
            holder.binding.invalidateAll()
            if (position == itemCount - 1) {
                invalidateBindings = false
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun notifyBindingsChanged() {
        invalidateBindings = true
        notifyDataSetChanged()
    }
}