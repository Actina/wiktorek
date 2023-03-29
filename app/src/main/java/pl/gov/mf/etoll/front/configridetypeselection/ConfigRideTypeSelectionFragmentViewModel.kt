package pl.gov.mf.etoll.front.configridetypeselection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pl.gov.mf.etoll.base.BaseDatabindingViewModel
import pl.gov.mf.etoll.core.model.CoreVehicleCategory
import pl.gov.mf.etoll.front.config.RideConfigurationCoordinator
import javax.inject.Inject

class ConfigRideTypeSelectionFragmentViewModel : BaseDatabindingViewModel() {

    private val _continueShouldBeAvailable: MutableLiveData<Boolean> = MutableLiveData(false)
    val continueShouldBeAvailable: LiveData<Boolean> = _continueShouldBeAvailable

    @Inject
    lateinit var rideConfigurationCoordinator: RideConfigurationCoordinator

    private val _rideTypeSelected: MutableLiveData<ConfigRideTypeNavTargets> =
        MutableLiveData(ConfigRideTypeNavTargets.IDLE)
    val rideModeSelected: LiveData<ConfigRideTypeNavTargets>
        get() = _rideTypeSelected

    private val _rideSelectionState: MutableLiveData<RideSelectionState> = MutableLiveData()
    val rideSelectionState: LiveData<RideSelectionState>
        get() = _rideSelectionState

    override fun onResume() {
        super.onResume()
        rideConfigurationCoordinator.onViewShowing(RideConfigurationCoordinator.RideConfigurationDestination.RIDE_MODE_SELECTION)
        _rideSelectionState.value =
                // FYI: commented code require checks if values are cleared in correct places in app - it allows preselection when returning from other pages
            rideConfigurationCoordinator.generateViewDataForRideSelection()
    }

    fun isMixRideBlocked(): Boolean {
        // For motorcycles mix ride is not available
        val vehiclesList = rideConfigurationCoordinator.generateMotorcycleStepVehiclesList()
        return isMixRide() && vehiclesList.isNotEmpty() && vehiclesList.all { it.category == CoreVehicleCategory.MOTORCYCLE.value }
    }

    private fun isMixRide() = _rideSelectionState.value?.sentRideIsEnabled == true &&
            _rideSelectionState.value?.tolledRideIsEnabled == true

    fun onRideSelectionChanged(tolledChecked: Boolean, sentChecked: Boolean) {
        rideSelectionState.value?.let { currentValue ->
            _rideSelectionState.value =
                RideSelectionState(
                    tolledChecked,
                    currentValue.tolledRideShouldBeVisible,
                    sentChecked,
                    currentValue.sentShouldBeVisible
                )
        }
    }

    fun resetNavigation() {
        _rideTypeSelected.postValue(ConfigRideTypeNavTargets.IDLE)
    }

    fun onRideModeSelected(): Boolean {
        rideSelectionState.value?.let {
            rideConfigurationCoordinator.onRideModeSelected(
                tolled = it.tolledRideIsEnabled,
                sent = it.sentRideIsEnabled
            )
            _rideTypeSelected.postValue(
                rideConfigurationCoordinator.getNextStep().mapNavigation()
            )
        }
        return true
    }

    fun sentIsDoneInOfflineMode(): Boolean =
        rideConfigurationCoordinator.sentConfigurationIsDoneOffline()
}

private fun RideConfigurationCoordinator.RideConfigurationDestination.mapNavigation(): ConfigRideTypeNavTargets {
    return when (this) {
        RideConfigurationCoordinator.RideConfigurationDestination.VEHICLE_SELECTION -> {
            pl.gov.mf.etoll.front.configridetypeselection.ConfigRideTypeNavTargets.NAVIGATE_FORWARD_VEHICLE_SELECTION
        }
        RideConfigurationCoordinator.RideConfigurationDestination.TRAILER -> {
            pl.gov.mf.etoll.front.configridetypeselection.ConfigRideTypeNavTargets.NAVIGATE_FORWARD_TRAILER
        }
        RideConfigurationCoordinator.RideConfigurationDestination.MONITORING_DEVICE -> {
            pl.gov.mf.etoll.front.configridetypeselection.ConfigRideTypeNavTargets.NAVIGATE_FORWARD_MONITORING_DEVICE
        }
        RideConfigurationCoordinator.RideConfigurationDestination.SENT_LIST -> {
            // not possible atm
            pl.gov.mf.etoll.front.configridetypeselection.ConfigRideTypeNavTargets.IDLE
        }
        RideConfigurationCoordinator.RideConfigurationDestination.SAVE_AND_FINISH -> {
            pl.gov.mf.etoll.front.configridetypeselection.ConfigRideTypeNavTargets.FINISH_CONFIGURATION
        }
        RideConfigurationCoordinator.RideConfigurationDestination.RIDE_MODE_SELECTION -> {
            // do nothing
            pl.gov.mf.etoll.front.configridetypeselection.ConfigRideTypeNavTargets.IDLE
        }
    }
}

data class RideSelectionState(
    val tolledRideIsEnabled: Boolean,
    val tolledRideShouldBeVisible: Boolean,
    val sentRideIsEnabled: Boolean,
    val sentShouldBeVisible: Boolean
)

enum class ConfigRideTypeNavTargets {
    IDLE, NAVIGATE_FORWARD_VEHICLE_SELECTION, NAVIGATE_FORWARD_MONITORING_DEVICE, NAVIGATE_FORWARD_TRAILER, FINISH_CONFIGURATION
}