package pl.gov.mf.etoll.front.configvehicleselection.adapteritem

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.core.model.CoreVehicle
import pl.gov.mf.etoll.utils.DynamicBindingAdapter

class ConfigVehicleSelectionVehicleItem(
    coreVehicle: CoreVehicle,
    selected: Boolean = false,
    enabled: Boolean,
    onBodyClick: (CoreVehicle) -> Unit,
    onInfoIconClick: (CoreVehicle) -> Unit
) :
    DynamicBindingAdapter.Item(
        R.layout.item_config_vehicle_selection_vehicle,
        ViewModel(coreVehicle, selected, enabled, onBodyClick, onInfoIconClick)
    ) {

    data class ViewModel(
        val coreVehicle: CoreVehicle,
        private val selected: Boolean,
        private val enabled: Boolean,
        private val onClick: (CoreVehicle) -> Unit,
        private val onInfoIconClick: (CoreVehicle) -> Unit
    ) {
        private var _radioButtonChecked = MutableLiveData(false)
        val radioButtonChecked: LiveData<Boolean> get() = _radioButtonChecked

        private var _viewEnabled = MutableLiveData(true)
        val viewEnabled: LiveData<Boolean> get() = _viewEnabled

        init {
            _radioButtonChecked.value = selected
            _viewEnabled.value = enabled
        }

        fun onItemBodySelected() {
            _radioButtonChecked.value = true
            onClick.invoke(coreVehicle)
        }

        fun onItemInfoSelected() {
            onInfoIconClick.invoke(coreVehicle)
        }
    }
}