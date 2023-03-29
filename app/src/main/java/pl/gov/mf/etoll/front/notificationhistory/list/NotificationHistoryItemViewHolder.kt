package pl.gov.mf.etoll.front.notificationhistory.list

import androidx.databinding.ViewDataBinding
import pl.gov.mf.etoll.BR
import pl.gov.mf.mobile.ui.components.DataBindingViewHolder

class NotificationHistoryItemViewHolder<T>(binding: ViewDataBinding) :
    DataBindingViewHolder(binding) {
    fun bind(item: T) {
        binding.setVariable(BR.item, item)
        if (binding.hasPendingBindings()) {
            binding.executePendingBindings()
        }
    }
}