package pl.gov.mf.etoll.front.about.adapteritem

import androidx.lifecycle.LiveData
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.core.devicecompatibility.types.SystemInfoData
import pl.gov.mf.etoll.utils.DynamicBindingAdapter

class SystemInfoItem(
    systemInfoItem: LiveData<SystemInfoData>
) :
    DynamicBindingAdapter.Item(
        R.layout.item_about_system_info_item,
        ViewModel(systemInfoItem)
    ) {

    data class ViewModel(
        val systemInfoViewData: LiveData<SystemInfoData>
    )

}