package pl.gov.mf.etoll.front.configmonitoringdevice

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import pl.gov.mf.etoll.base.BaseDatabindingViewModel
import pl.gov.mf.etoll.front.config.RideConfigurationCoordinator
import javax.inject.Inject


class ConfigMonitoringDeviceFragmentViewModel : BaseDatabindingViewModel() {

    @Inject
    lateinit var configurationCoordinator: RideConfigurationCoordinator

    private val _viewData: MutableLiveData<SelectedMonitoringDevice> =
        MutableLiveData(SelectedMonitoringDevice.NONE)
    val viewData: LiveData<SelectedMonitoringDevice>
        get() = _viewData
    val confirmButtonEnabled: LiveData<Boolean> = Transformations.map(viewData) {
        viewData.value != SelectedMonitoringDevice.NONE
    }

    private val _navigate: MutableLiveData<MonitoringNavigationLocation> =
        MutableLiveData(MonitoringNavigationLocation.IDLE)
    val navigate: LiveData<MonitoringNavigationLocation>
        get() = _navigate

    override fun onCreate() {
        super.onCreate()
        when (configurationCoordinator.generateViewDataForMonitoringDevice()) {
            SelectedMonitoringDevice.PHONE -> _viewData.postValue(SelectedMonitoringDevice.PHONE)
            SelectedMonitoringDevice.OBE -> _viewData.postValue(SelectedMonitoringDevice.OBE)
            else -> {
                // do nothing
            }
        }
    }

    override fun onResume() {
        super.onResume()
        configurationCoordinator.onViewShowing(RideConfigurationCoordinator.RideConfigurationDestination.MONITORING_DEVICE)
    }

    fun onPhoneClicked() {
        _viewData.value = SelectedMonitoringDevice.PHONE
    }

    fun onObeClicked() {
        _viewData.value = SelectedMonitoringDevice.OBE
    }

    fun onConfirm() {
        viewData.value?.takeIf { it != SelectedMonitoringDevice.NONE }?.let { device ->
            configurationCoordinator.onMonitoringDeviceSelected(device == SelectedMonitoringDevice.PHONE)
            _navigate.postValue(configurationCoordinator.getNextStep().map())
        } ?: _navigate.postValue(MonitoringNavigationLocation.ERROR)
    }

    fun resetNavigation() {
        _navigate.postValue(MonitoringNavigationLocation.IDLE)
    }

    enum class MonitoringNavigationLocation {
        IDLE,
        SENT_LIST,
        BACK_TO_RIDE_DETAILS,
        ERROR,
        FINISH
    }
}

private fun RideConfigurationCoordinator.RideConfigurationDestination.map(): ConfigMonitoringDeviceFragmentViewModel.MonitoringNavigationLocation =
    when (this) {
        RideConfigurationCoordinator.RideConfigurationDestination.SENT_LIST -> {
            pl.gov.mf.etoll.front.configmonitoringdevice.ConfigMonitoringDeviceFragmentViewModel.MonitoringNavigationLocation.SENT_LIST
        }
        RideConfigurationCoordinator.RideConfigurationDestination.SAVE_AND_FINISH -> {
            pl.gov.mf.etoll.front.configmonitoringdevice.ConfigMonitoringDeviceFragmentViewModel.MonitoringNavigationLocation.FINISH
        }
        else -> {
            pl.gov.mf.etoll.front.configmonitoringdevice.ConfigMonitoringDeviceFragmentViewModel.MonitoringNavigationLocation.IDLE
        }
    }