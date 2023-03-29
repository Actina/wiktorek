package pl.gov.mf.etoll.front.ridedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pl.gov.mf.etoll.base.BaseDatabindingViewModel
import pl.gov.mf.etoll.core.model.CoreAccountTypes
import pl.gov.mf.etoll.core.model.CoreVehicle
import pl.gov.mf.etoll.core.model.CoreVehicleCategory
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorV3
import javax.inject.Inject

class RideDataViewModel : BaseDatabindingViewModel() {
    @Inject
    lateinit var rideCoordinatorV3: RideCoordinatorV3

    private var _vehicle: MutableLiveData<CoreVehicle> = MutableLiveData()
    val vehicle: LiveData<CoreVehicle> = _vehicle

    private var _vehicleCategory = MutableLiveData<String>()
    val vehicleCategory: LiveData<String> = _vehicleCategory

    private val _businessNumber: MutableLiveData<String> = MutableLiveData()
    val businessNumber: LiveData<String> = _businessNumber

    private val _trailerWeightExceeds: MutableLiveData<Boolean> = MutableLiveData(false)
    val trailerWeightExceeds: LiveData<Boolean> = _trailerWeightExceeds

    private val _trailerShouldBeVisible =
        MutableLiveData(false)
    val trailerShouldBeVisible: LiveData<Boolean> = _trailerShouldBeVisible

    private val _untranslatedAccountType: MutableLiveData<String> = MutableLiveData()
    val untranslatedAccountType: LiveData<String> = _untranslatedAccountType

    override fun onResume() {
        super.onResume()
        rideCoordinatorV3.getConfiguration()?.let { configuration ->
            _trailerShouldBeVisible.postValue(configuration.tolledConfiguration != null)
            if (configuration.tolledConfiguration != null) {
                configuration.tolledConfiguration?.vehicle?.let {
                    _vehicle.value = it
                    _vehicleCategory.value = CoreVehicleCategory.fromInt(it.category).uiLiteral
                    _untranslatedAccountType.value =
                        CoreAccountTypes.toUiLiteral(it.accountInfo.type)
                }
                _trailerWeightExceeds.postValue(configuration.tolledConfiguration!!.trailerUsedAndCategoryWillBeIncreased)
            }
        }

    }
}