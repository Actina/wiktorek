package pl.gov.mf.etoll.front.configvehicleselection

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Transformations
import pl.gov.mf.etoll.base.BaseDatabindingViewModel
import pl.gov.mf.etoll.core.model.CoreVehicle
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorV3
import pl.gov.mf.etoll.core.vehiclesdisplaymanagement.VehiclesDisplayManagementUC
import pl.gov.mf.etoll.front.config.RideConfigurationCoordinator
import pl.gov.mf.mobile.utils.addSafe
import pl.gov.mf.mobile.utils.deepCopy
import pl.gov.mf.mobile.utils.toObject
import javax.inject.Inject

class ConfigVehicleSelectionFragmentViewModel(private val savedStateHandle: SavedStateHandle) :
    BaseDatabindingViewModel() {

    @Inject
    lateinit var getVehiclesDividedIntoRecentAndOtherUseCase: VehiclesDisplayManagementUC.GetVehiclesDividedIntoRecentAndOtherUseCase

    @Inject
    lateinit var addRecentVehiclesUseCase: VehiclesDisplayManagementUC.AddRecentVehiclesUseCase

    @Inject
    lateinit var rideConfigurationCoordinator: RideConfigurationCoordinator

    @Inject
    lateinit var rideCoordinatorV3: RideCoordinatorV3

    private val _recentOthersSelected =
        MutableLiveData<Triple<List<CoreVehicle>, List<CoreVehicle>, CoreVehicle?>>()
    val recentOthersSelected: LiveData<Triple<List<CoreVehicle>, List<CoreVehicle>, CoreVehicle?>>
        get() = _recentOthersSelected

    private val _navigate: MutableLiveData<ConfigVehicleSelectionNavTargets> =
        MutableLiveData(ConfigVehicleSelectionNavTargets.IDLE)
    val navigate: LiveData<ConfigVehicleSelectionNavTargets>
        get() = _navigate

    private var preSelectedVehicle: CoreVehicle? = null
    private var selectedVehicle: CoreVehicle? = null
    private var dialogShown: Boolean = false

    val confirmButtonEnabled: LiveData<Boolean> = Transformations.map(recentOthersSelected) {
        it.third != null
    }

    fun restoreModel() {
        selectedVehicle = savedStateHandle.get<String>(SELECTED_VEHICLE)?.toObject()
        dialogShown = savedStateHandle.get<Boolean>(DIALOG_CREATED) ?: false
    }

    fun onArgumentsSet(arguments: Bundle?) {
        arguments?.let {
            preSelectedVehicle = it.getString(PRE_SELECTED_VEHICLE)?.toObject()
            dialogShown = it.getBoolean(DIALOG_CREATED)
        }
    }

    override fun onResume() {
        super.onResume()
        rideConfigurationCoordinator.onViewShowing(RideConfigurationCoordinator.RideConfigurationDestination.VEHICLE_SELECTION)
        selectedVehicle = rideConfigurationCoordinator.generateViewDataForVehicleSelection()
        preSelectedVehicle = rideConfigurationCoordinator.generateViewDataForVehicleSelection()
        // this need to be called every time, as otherwise it would be outdated when returning from later steps, ie selected vehicle would not be on list 'recently selected'
        // this probably is better option as list of recently used was already altered... maybe we should do lazy change here after finishing full configuration?
        compositeDisposable.addSafe(
            getVehiclesDividedIntoRecentAndOtherUseCase.execute()
                .subscribe({
                    if (it.first.contains(preSelectedVehicle) ||
                        it.second.contains(preSelectedVehicle)
                    ) {
                        selectedVehicle = preSelectedVehicle?.deepCopy()
                        preSelectedVehicle = null
                    }
                    _recentOthersSelected.value = Triple(it.first, it.second, selectedVehicle)
                }, {})
        )
    }

    fun isSentActive(): Boolean =
        rideCoordinatorV3.getConfiguration()?.duringSent ?: false || rideConfigurationCoordinator.isSentRideSelected()

    override fun onPause() {
        super.onPause()
        savedStateHandle.set(SELECTED_VEHICLE, selectedVehicle?.toJSON())
        savedStateHandle.set(DIALOG_CREATED, dialogShown)
    }

    fun onVehicleSelected(coreVehicle: CoreVehicle) {
        selectedVehicle = coreVehicle
        rideConfigurationCoordinator.onVehicleSelected(coreVehicle)
        recentOthersSelected.value?.let {
            _recentOthersSelected.value = Triple(it.first, it.second, selectedVehicle)
        }
    }

    fun resetNavigation() {
        _navigate.postValue(ConfigVehicleSelectionNavTargets.IDLE)
    }

    fun onConfirmClick() {
        selectedVehicle?.let { vehicle ->
            compositeDisposable.addSafe(
                addRecentVehiclesUseCase.executeAsync(vehicle.id).subscribe({
                    _navigate.postValue(rideConfigurationCoordinator.getNextStep().map())
                }, {
                    // error?
                    _navigate.postValue(ConfigVehicleSelectionNavTargets.ERROR)
                })
            )
        }
    }

    fun isDialogShown() = dialogShown

    fun notifyDialogShown() {
        dialogShown = true
    }

    fun notifyDialogHidden() {
        dialogShown = false
    }

    companion object {
        private const val PRE_SELECTED_VEHICLE = "preSelectedVehicle"
        private const val SELECTED_VEHICLE = "selectedVehicle"
        private const val DIALOG_CREATED = "dialogCreated"
    }

}

private fun RideConfigurationCoordinator.RideConfigurationDestination.map(): ConfigVehicleSelectionNavTargets {
    return when (this) {
        RideConfigurationCoordinator.RideConfigurationDestination.TRAILER -> ConfigVehicleSelectionNavTargets.TRAILER
        RideConfigurationCoordinator.RideConfigurationDestination.MONITORING_DEVICE -> ConfigVehicleSelectionNavTargets.MONITORING_DEVICE
        RideConfigurationCoordinator.RideConfigurationDestination.SENT_LIST -> ConfigVehicleSelectionNavTargets.SENT_LIST
        RideConfigurationCoordinator.RideConfigurationDestination.SAVE_AND_FINISH -> ConfigVehicleSelectionNavTargets.FINISH
        RideConfigurationCoordinator.RideConfigurationDestination.VEHICLE_SELECTION,
        RideConfigurationCoordinator.RideConfigurationDestination.RIDE_MODE_SELECTION -> {
            ConfigVehicleSelectionNavTargets.IDLE
        }
    }
}

enum class ConfigVehicleSelectionNavTargets {
    IDLE, TRAILER, MONITORING_DEVICE, ERROR, FINISH, SENT_LIST
}