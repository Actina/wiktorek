package pl.gov.mf.etoll.front.errors.instanceissues

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pl.gov.mf.etoll.base.BaseDatabindingViewModel

class InstanceIssuesViewModel : BaseDatabindingViewModel() {

    private val _isDismissable = MutableLiveData(true)
    val isDismissable: LiveData<Boolean> = _isDismissable

    private val _shouldClose = MutableLiveData(false)
    val shouldClose: LiveData<Boolean> = _shouldClose

    private val _shouldShowShop = MutableLiveData(false)
    val shouldShowShop: LiveData<Boolean> = _shouldShowShop

    fun onOkClick() {
        if (isDismissable.value == true)
            _shouldClose.postValue(true)
    }

    fun onShowShopClick() {
        _shouldShowShop.postValue(true)
    }

    fun setDismissableParam(dismissable: Boolean) {
        _isDismissable.value = dismissable
    }

    fun onShopShowed() {
        _shouldShowShop.value = false
    }
}