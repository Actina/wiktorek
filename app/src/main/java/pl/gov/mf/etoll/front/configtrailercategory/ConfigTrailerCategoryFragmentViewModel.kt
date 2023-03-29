package pl.gov.mf.etoll.front.configtrailercategory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pl.gov.mf.etoll.base.BaseDatabindingViewModel
import pl.gov.mf.etoll.front.config.RideConfigurationCoordinator
import javax.inject.Inject

class ConfigTrailerCategoryFragmentViewModel : BaseDatabindingViewModel() {

    @Inject
    lateinit var configurationCoordinator: RideConfigurationCoordinator

    private val _navigate: MutableLiveData<TrailerCategoryNavigation> =
        MutableLiveData(TrailerCategoryNavigation.IDLE)
    val navigate: LiveData<TrailerCategoryNavigation>
        get() = _navigate

    private val _weightExceeded = MutableLiveData(false)
    val weightExceeded: LiveData<Boolean>
        get() = _weightExceeded

    override fun onResume() {
        super.onResume()
        configurationCoordinator.onViewShowing(RideConfigurationCoordinator.RideConfigurationDestination.TRAILER)
    }

    fun onContinueClick() {
        configurationCoordinator.onTrailerSelected(
            increaseFlagDeclared = weightExceeded.value!!
        )
        _navigate.postValue(configurationCoordinator.getNextStep().map())

    }

    fun onWeightExceededClick() {
        _weightExceeded.postValue(!weightExceeded.value!!)
    }

    fun init(entryScreenId: Int) {
        _weightExceeded.postValue(configurationCoordinator.generateViewDataForTrailerInfo())
    }

    enum class TrailerCategoryNavigation {
        IDLE,
        MONITORING_DEVICE,
        DASHBOARD,
        FINISH
    }

    fun resetNavigation() {
        _navigate.postValue(TrailerCategoryNavigation.IDLE)
    }
}

private fun RideConfigurationCoordinator.RideConfigurationDestination.map(): ConfigTrailerCategoryFragmentViewModel.TrailerCategoryNavigation {
    return when (this) {
        RideConfigurationCoordinator.RideConfigurationDestination.MONITORING_DEVICE -> ConfigTrailerCategoryFragmentViewModel.TrailerCategoryNavigation.MONITORING_DEVICE
        RideConfigurationCoordinator.RideConfigurationDestination.SAVE_AND_FINISH -> ConfigTrailerCategoryFragmentViewModel.TrailerCategoryNavigation.FINISH
        else -> {
            ConfigTrailerCategoryFragmentViewModel.TrailerCategoryNavigation.IDLE
        }
    }
}

