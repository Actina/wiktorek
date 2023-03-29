package pl.gov.mf.etoll.front.ridedetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pl.gov.mf.etoll.base.BaseDatabindingViewModel
import pl.gov.mf.etoll.core.devicecompatibility.DeviceCompatibilityUC
import pl.gov.mf.etoll.core.model.CoreVehicle
import pl.gov.mf.etoll.core.model.CoreVehicleCategory
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorV3
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.TolledConfiguration
import pl.gov.mf.etoll.core.vehiclesdisplaymanagement.VehiclesDisplayManagementUC
import pl.gov.mf.etoll.front.config.RideConfigurationCoordinator
import pl.gov.mf.mobile.utils.addSafe
import javax.inject.Inject

class RideDetailsViewModel : BaseDatabindingViewModel() {
    @Inject
    lateinit var rideCoordinatorV3: RideCoordinatorV3

    @Inject
    lateinit var getVehiclesDividedIntoRecentAndOtherUseCase: VehiclesDisplayManagementUC.GetVehiclesDividedIntoRecentAndOtherUseCase

    @Inject
    lateinit var checkGpsLocationServiceAvailable: DeviceCompatibilityUC.CheckGpsLocationServiceAvailable

    @Inject
    lateinit var rideConfigurationCoordinator: RideConfigurationCoordinator

    private val _navigate: MutableLiveData<RideDetailsNavigation> =
        MutableLiveData(RideDetailsNavigation.IDLE)
    val navigate: LiveData<RideDetailsNavigation>
        get() = _navigate

    private val _data = MutableLiveData<RideDetailsModelData>()
    val data: LiveData<RideDetailsModelData>
        get() = _data

    private val _isMixBlocked = MutableLiveData(false)
    private var vehiclesList: List<CoreVehicle> = emptyList()

    val shouldShowNoGpsDialog: Boolean
        get() = !checkGpsLocationServiceAvailable.execute() &&
                rideCoordinatorV3.getConfiguration()?.monitoringDeviceConfiguration?.monitoringByApp == false

    fun resetNavigation() {
        _navigate.postValue(RideDetailsNavigation.IDLE)
    }

    override fun onResume() {
        super.onResume()
        updateConfig()
        updateVehiclesList()
    }

    private fun updateVehiclesList() {
        // Mix during sent if user have only motorcycle (one or more) is not available
        compositeDisposable.addSafe(
            getVehiclesDividedIntoRecentAndOtherUseCase.execute().subscribe({
                vehiclesList = it.let {
                    it.first.toList() + it.second.toList()
                }
                _isMixBlocked.value = rideCoordinatorV3.getConfiguration()?.duringSent == true &&
                        vehiclesList.isNotEmpty() && vehiclesList.all { coreVehicle ->
                    coreVehicle.category == CoreVehicleCategory.MOTORCYCLE.value
                }
            }, {
                _isMixBlocked.value = false
            })
        )
    }

    private fun updateConfig() {
        val rideConfiguration = rideCoordinatorV3.getConfiguration()!!
        var updateData = _data.value
        if (updateData == null)
            updateData = RideDetailsModelData(
                vehicle = rideConfiguration.tolledConfiguration?.vehicle
                    ?: rideConfiguration.disabledTolledConfiguration?.vehicle,
                duringRide = rideConfiguration.duringRide,
                tolledIsPossible = rideConfiguration.tolledIsPossibleToBeEnabled,
                //we're not during sent, or we're during sent and we've downloaded non-empy sent list and we haven't selected any sent package from "other" group
                changeMonitoringDeviceShouldBePossible = rideConfiguration.monitoringDeviceCanBeChanged
            )
        _data.postValue(
            updateData.apply {
                tolledIsEnabled = rideConfiguration.duringTolled
                sentIsPossible = rideConfiguration.duringSent
                monitoringByApp = rideConfiguration.monitoringDeviceConfiguration!!.monitoringByApp
                if (rideConfiguration.duringTolled) {
                    rideConfiguration.tolledConfiguration?.let { tolledConfig ->
                        trailerWeightExceeded = tolledConfig.trailerUsedAndCategoryWillBeIncreased
                        trailerCouldBeModified =
                            tolledConfig.vehicle?.categoryCanBeIncreased ?: false
                    }
                } else if (rideConfiguration.disabledTolledConfiguration != null) {
                    rideConfiguration.disabledTolledConfiguration?.let { tolledConfig ->
                        trailerWeightExceeded = tolledConfig.trailerUsedAndCategoryWillBeIncreased
                        trailerCouldBeModified =
                            tolledConfig.vehicle?.categoryCanBeIncreased ?: false
                    }
                }
                changeMonitoringDeviceShouldBePossible =
                    rideConfiguration.monitoringDeviceCanBeChanged
            }
        )
    }

    fun onEnableTollRideClick() {
        val tolledShouldBeEnabled = !data.value!!.tolledIsEnabled

        if (_isMixBlocked.value == true) {
            // mix for motorcycles is not available
            _navigate.postValue(RideDetailsNavigation.MIX_NOT_AVAILABLE_FOR_MOTORCYCLES)
        } else if (tolledShouldBeEnabled) {
            if (rideCoordinatorV3.resumeDisabledRide()) {
                // do nothing, we enabled old config
                _data.postValue(data.value!!.apply {
                    tolledIsEnabled = true
                })
            } else {
                // there was nothing to enable
                // if user have only one vehicle, start tolled immediately
                if (vehiclesList.size == 1) {
                    // update ui
                    rideConfigurationCoordinator.onVehicleSelected(vehiclesList.first())
                    _data.postValue(data.value!!.apply {
                        tolledIsEnabled = true
                        vehicle = vehiclesList.first()
                    })
                    // set configuration
                    rideCoordinatorV3.getConfiguration()?.let {
                        rideCoordinatorV3.onConfigurationUpdated(it.copy(
                            tolledConfiguration = TolledConfiguration(
                                vehicle = vehiclesList.first(),
                            ),
                            tolledIsPossibleToBeEnabled = true,
                        ))
                    }
                } else {
                    _navigate.postValue(RideDetailsNavigation.ADD_TOLLED)
                }
            }
        } else {
            // TODO: add dialog yes/no for this action
            rideCoordinatorV3.pauseTolledRide()
            _data.postValue(data.value!!.apply {
                tolledIsEnabled = false
            })
        }
    }

    fun shouldRideDetailsTrailerBeVisible(): Boolean =
        rideCoordinatorV3.getConfiguration()!!.tolledConfiguration?.vehicle?.category != CoreVehicleCategory.MOTORCYCLE.value

    fun onRideDetailsSentSelectionClick() {
        _navigate.value = RideDetailsNavigation.RIDE_DETAILS_SENT_SELECTION
    }

    fun onTrailerConfigurationClick() {
        if (data.value!!.trailerCouldBeModified)
            _navigate.value = RideDetailsNavigation.TRAILER_CONFIGURATION
    }

    fun onChangeDeviceClick() {
        if (data.value!!.changeMonitoringDeviceShouldBePossible)
            _navigate.value = RideDetailsNavigation.MONITORING_DEVICE
    }

    fun onRideDataClick() {
        _navigate.value = RideDetailsNavigation.RIDE_DATA
    }

    fun onShowZslMapClick() {
        _navigate.value = RideDetailsNavigation.SHOW_MAP_WITH_RIDE_DETAILS
    }

    // we should be able to change device without permissions check when outside ride, and with checks during ride
    fun isGpsRequiredForAlternativeConfiguration(): Boolean =
        rideCoordinatorV3.getConfiguration()?.duringRide == true &&
                rideCoordinatorV3.getConfiguration()?.monitoringDeviceConfiguration?.monitoringByApp != true

    enum class RideDetailsNavigation {
        IDLE,
        MONITORING_DEVICE,
        RIDE_DATA,
        TRAILER_CONFIGURATION,
        SHOW_MAP_WITH_RIDE_DETAILS,
        ADD_TOLLED,
        RIDE_DETAILS_SENT_SELECTION,
        MIX_NOT_AVAILABLE_FOR_MOTORCYCLES
    }

    data class RideDetailsModelData(
        var vehicle: CoreVehicle?,
        var duringRide: Boolean,
        var monitoringByApp: Boolean = false,
        var trailerWeightExceeded: Boolean = false,
        var tolledIsEnabled: Boolean = false,
        var sentIsPossible: Boolean = false,
        var tolledIsPossible: Boolean,
        var trailerCouldBeModified: Boolean = false,
        var changeMonitoringDeviceShouldBePossible: Boolean,
    ) {
        val showLastLocationOnMapVisibility: Boolean
            get() = duringRide
        val showEnableDisableTolledSwitch: Boolean
            get() = tolledIsPossible && sentIsPossible
    }

}
