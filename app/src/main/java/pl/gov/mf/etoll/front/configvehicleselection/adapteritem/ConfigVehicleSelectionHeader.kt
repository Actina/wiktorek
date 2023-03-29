package pl.gov.mf.etoll.front.configvehicleselection.adapteritem

import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.utils.DynamicBindingAdapter

data class ConfigVehicleSelectionHeader(val empty: Unit = Unit) : DynamicBindingAdapter.Item(
    R.layout.item_config_vehicle_selection_header,
    ViewModel(empty)
) {
    data class ViewModel(val empty: Unit)
}