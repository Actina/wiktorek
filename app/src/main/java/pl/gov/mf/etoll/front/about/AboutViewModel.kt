package pl.gov.mf.etoll.front.about

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pl.gov.mf.etoll.base.BaseDatabindingViewModel
import pl.gov.mf.etoll.core.devicecompatibility.DeviceCompatibilityUC
import pl.gov.mf.etoll.core.devicecompatibility.types.SystemInfoData
import pl.gov.mf.etoll.networking.NetworkingUC
import pl.gov.mf.mobile.utils.addSafe
import javax.inject.Inject

class AboutViewModel : BaseDatabindingViewModel() {

    private val _requestedNavigation = MutableLiveData<Boolean>()
    val requestedNavigation: LiveData<Boolean>
        get() = _requestedNavigation

    private val _businessNumber = MutableLiveData("")
    val businessNumber: LiveData<String>
        get() = _businessNumber

    private val _systemInfoData = MutableLiveData<SystemInfoData>()
    val systemInfoData: LiveData<SystemInfoData>
        get() = _systemInfoData

    @Inject
    lateinit var getBusinessNumberUseCase: NetworkingUC.GetBusinessNumberUseCase

    @Inject
    lateinit var getSystemInfoUseCase: DeviceCompatibilityUC.GetSystemInfoUseCase

    override fun onResume() {
        super.onResume()
        if (businessNumber.value.isNullOrEmpty()) {
            compositeDisposable.addSafe(getBusinessNumberUseCase.execute().subscribe({
                _businessNumber.postValue(it)
            }, {
                // error, ignore
            }))
        }
        if (systemInfoData.value == null) {
            _systemInfoData.value = getSystemInfoUseCase.execute()
        }
    }

    fun onToolbarBack() {
        _requestedNavigation.postValue(true)
    }

}