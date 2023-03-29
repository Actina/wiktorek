package pl.gov.mf.etoll.front.ridedetails.sentselection.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ListAdapter

class SentRidesAdapter :
    ListAdapter<SentRideItem, SentRideViewHolder<SentRideItem>>(ItemDiffCallback()) {
    lateinit var lifecycleOwner: LifecycleOwner

    private var invalidateBindings: Boolean = false

    override fun getItemViewType(position: Int): Int {
        return getItem(position).layoutId
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): SentRideViewHolder<SentRideItem> {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ViewDataBinding = DataBindingUtil.inflate(inflater, viewType, parent, false)
        binding.lifecycleOwner = lifecycleOwner
        return SentRideViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SentRideViewHolder<SentRideItem>, position: Int) {
        holder.bind(getItem(position))
        invalidateBindings(holder, position)
    }

    private fun invalidateBindings(
        holder: SentRideViewHolder<SentRideItem>,
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