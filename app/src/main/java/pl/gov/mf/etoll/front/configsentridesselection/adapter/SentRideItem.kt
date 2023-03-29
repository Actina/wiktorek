package pl.gov.mf.etoll.front.configsentridesselection.adapter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.commons.Constants
import pl.gov.mf.etoll.front.configsentridesselection.SentRideData

sealed class SentRideItem(val layoutId: Int) {
    data class SentRideContent(
        val data: SentRideData,
        val group: String,
        val listener: OnSentRideItemListener,
    ) : SentRideItem(R.layout.item_config_sent_rides_content) {
        val viewModel = ViewModel()

        override fun compareContent(item: SentRideItem): Boolean {
            if (item is SentRideContent)
                return item.group == group && item.data == data
            return false
        }

        inner class ViewModel {
            private val _itemEnabled = MutableLiveData<Boolean>()
            val itemEnabled: LiveData<Boolean>
                get() = _itemEnabled
            private val _itemChecked = MutableLiveData(false)
            val itemChecked: LiveData<Boolean>
                get() = _itemChecked

            init {
                _itemEnabled.value = data.enabled
            }

            fun sentDates(): String {
                val start = data.formattedStartDate()
                val end = data.formattedEndDate()

                return "$start - $end"
            }

            fun updateEnabledState(value: Boolean) {
                _itemEnabled.value = value
                data.enabled = value
            }

            fun onItemClick() {
                val newValue = !_itemChecked.value!!
                _itemChecked.value = newValue
                data.checked = newValue
                listener.onSentItemClick(this@SentRideContent)
            }

            fun onItemDetailsClick() {
                listener.onSentItemInfoClick(this@SentRideContent)
            }

            fun updateCheckedState(value: Boolean) {
                _itemChecked.value = value
                data.checked = value
            }
        }
    }

    data class SentRideHeader(val group: String) :
        SentRideItem(R.layout.item_config_sent_rides_header) {

        val isOther: Boolean = group == Constants.SENT_GROUP_OTHER

        val untranslatedGroupHeader: String =
            if (isOther)
                "config_sent_rides_selection_section_other"
            else
                "config_sent_rides_selection_section_monitoring"

        val groupNameVisible: Boolean = !isOther

        override fun compareContent(item: SentRideItem): Boolean {
            if (item is SentRideHeader)
                return item.group == group
            return false
        }
    }

    open fun compareContent(item: SentRideItem): Boolean {
        return false
    }
}