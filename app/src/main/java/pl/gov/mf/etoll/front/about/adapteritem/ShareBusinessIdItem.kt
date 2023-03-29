package pl.gov.mf.etoll.front.about.adapteritem

import androidx.lifecycle.LiveData
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.utils.DynamicBindingAdapter

class ShareBusinessIdItem(businessNumber: LiveData<String>, shareClicked: (String) -> Unit) :
    DynamicBindingAdapter.Item(
        R.layout.item_about_share_business_id,
        ViewModel(businessNumber, shareClicked)
    ) {
    data class ViewModel(val businessNumber: LiveData<String>, val shareClicked: (String) -> Unit) {
        fun onShareClicked() {
            businessNumber.value?.let {
                shareClicked(it)
            }
        }
    }
}
