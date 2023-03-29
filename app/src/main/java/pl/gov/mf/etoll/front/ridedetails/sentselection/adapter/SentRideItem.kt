package pl.gov.mf.etoll.front.ridedetails.sentselection.adapter

import android.content.Context
import android.graphics.Typeface
import android.text.Layout
import android.text.SpannableString
import android.text.style.AlignmentSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.ContextThemeWrapper
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.appmode.AppMode
import pl.gov.mf.etoll.commons.Constants
import pl.gov.mf.etoll.front.ridedetails.sentselection.RideDetailsSentData
import pl.gov.mf.mobile.utils.getAppMode
import pl.gov.mf.mobile.utils.toColorInMode
import pl.gov.mf.mobile.utils.translate


sealed class SentRideItem(val layoutId: Int) {
    data class ActiveSentContent(
        val data: RideDetailsSentData,
    ) : SentRideItem(R.layout.item_ride_details_sent_list_active) {
        var listener: OnActiveSentItemListener? = null
        var duringRide: Boolean = false

        override fun areItemTheSame(item: SentRideItem): Boolean =
            item is ActiveSentContent && item.data.item.sentNumber == this.data.item.sentNumber

        private fun MenuItem.format(context: Context, color: Int) {
            title = SpannableString((title as String).translate(context)).apply {
                setSpan(ForegroundColorSpan(color), 0, length, 0)
                setSpan(StyleSpan(Typeface.BOLD), 0, length, 0)
                setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, length, 0)
            }
        }

        private fun onMenuItemClickListener(itemId: Int) {
            listener?.run {
                when (itemId) {
                    R.id.active_sent_item_action_details -> onActiveItemDetailsClick(data)
                    R.id.active_sent_item_action_cancel -> onActiveItemCancelClick(data)
                    R.id.active_sent_item_action_finish -> onActiveItemFinishClick(data)
                }
            }
        }

        fun onItemDetailsClick(view: View) {
            val context = view.context

            val wrapper = if (getAppMode(context) == AppMode.DARK_MODE)
                ContextThemeWrapper(context, R.style.PopupMenu_Dark_InMode)
            else
                ContextThemeWrapper(context, R.style.PopupMenu_Light_InMode)

            PopupMenu(wrapper, view).apply {
                menuInflater.inflate(R.menu.active_sent_item_actions, menu)
                menu.getItem(0).format(context, "activeSentItemActionBlue".toColorInMode(context))
                menu.getItem(1)
                    .format(context, "activeSentItemActionOrange".toColorInMode(context))
                menu.getItem(2).apply {
                    if (duringRide)
                        format(context, "activeSentItemActionRed".toColorInMode(context))
                    else
                        isVisible = false
                }
                setOnMenuItemClickListener {
                    onMenuItemClickListener(it.itemId)
                    true
                }
                show()
            }
        }
    }

    data class AvailableSentContent(
        val data: RideDetailsSentData,
    ) : SentRideItem(R.layout.item_ride_details_sent_list_available) {
        var listener: OnAvailableSentItemListener? = null
        val viewModel = ViewModel()

        override fun areItemTheSame(item: SentRideItem): Boolean =
            item is AvailableSentContent && item.data.item.sentNumber == this.data.item.sentNumber

        fun select() {
            data.checked = !data.checked
            viewModel.select(data.checked)
        }

        inner class ViewModel {
            private val _itemChecked = MutableLiveData<Boolean>()
            val itemChecked: LiveData<Boolean> = _itemChecked
            private val _itemEnabled = MutableLiveData<Boolean>()
            val itemEnabled: LiveData<Boolean> = _itemEnabled

            init {
                _itemEnabled.value = data.enabled
                _itemChecked.value = data.checked
            }

            fun select(value: Boolean) {
                _itemChecked.postValue(value)
            }

            fun onItemClick() {
                listener?.onSentItemClick(this@AvailableSentContent)
            }

            fun onItemDetailsClick() {
                listener?.onAvailableItemDetailsClick(this@AvailableSentContent)
            }
        }
    }

    data class SentHeader(val name: String) :
        SentRideItem(R.layout.item_ride_details_sent_list_header) {

        override fun areItemTheSame(item: SentRideItem): Boolean =
            item is SentHeader && item.name == name
    }

    data class SentGroup(val group: String) :
        SentRideItem(R.layout.item_ride_details_sent_list_group) {

        val isOther: Boolean = group == Constants.SENT_GROUP_OTHER

        val untranslatedGroupHeader: String =
            if (isOther)
                "config_sent_rides_selection_section_other"
            else
                "config_sent_rides_selection_section_monitoring"

        val groupNameVisible: Boolean = !isOther

        override fun areItemTheSame(item: SentRideItem): Boolean =
            item is SentGroup && item.group == group
    }

    object EmptyActiveGroup : SentRideItem(R.layout.item_ride_details_sent_list_active_empty)

    open fun areItemTheSame(item: SentRideItem): Boolean = false
}