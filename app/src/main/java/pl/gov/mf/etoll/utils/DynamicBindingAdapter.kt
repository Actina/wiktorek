package pl.gov.mf.etoll.utils

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import pl.gov.mf.etoll.BR
import pl.gov.mf.mobile.ui.components.DataBindingViewHolder

class DynamicBindingAdapter(private val lifecycleOwner: LifecycleOwner) :
    ListAdapter<DynamicBindingAdapter.Item, DataBindingViewHolder>(ItemDiff) {

    private var invalidateBindingExpressions: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBindingViewHolder =
        DataBindingViewHolder(
            DataBindingUtil.inflate<ViewDataBinding>(
                LayoutInflater.from(parent.context),
                viewType,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: DataBindingViewHolder, position: Int) {
        getItem(position).bindToHolder(holder)
        if (invalidateBindingExpressions) {
            holder.binding.invalidateAll()
            if (position == itemCount - 1) {
                invalidateBindingExpressions = false
            }
        }
        holder.binding.lifecycleOwner = lifecycleOwner
        holder.binding.executePendingBindings()
    }

    fun notifyBindingsExpressionsChanged() {
        invalidateBindingExpressions = true
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).layoutId
    }

    open class Item(@LayoutRes val layoutId: Int, val bindItem: Any = Unit) {
        open fun bindToHolder(holder: DataBindingViewHolder) {
            holder.binding.setVariable(BR.item, bindItem)
        }
    }

    object ItemDiff : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean =
            newItem.layoutId == oldItem.layoutId

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean =
            newItem.bindItem == oldItem.bindItem
    }
}
