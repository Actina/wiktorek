package pl.gov.mf.etoll.front.help

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pl.gov.mf.etoll.base.BaseDatabindingViewModel
import pl.gov.mf.etoll.core.CoreComposedUC
import pl.gov.mf.etoll.storage.settings.SettingsUC
import javax.inject.Inject

class HelpViewModel : BaseDatabindingViewModel() {

    private val _shouldFinish = MutableLiveData(false)
    val shouldFinish: LiveData<Boolean> = _shouldFinish

    private val _helpConfiguration = MutableLiveData<HelpConfiguration>()
    val helpConfiguration: LiveData<HelpConfiguration> = _helpConfiguration

    @Inject
    lateinit var readSettingsUseCase: SettingsUC.ReadSettingsUseCase

    @Inject
    lateinit var getCoreStatusUseCase: CoreComposedUC.GetCoreStatusUseCase

    fun onToolbarBack() {
        _shouldFinish.postValue(true)
    }

    override fun onResume() {
        super.onResume()
        if (helpConfiguration.value == null) {
            // calculate value, we prevent it from going to "empty state", so we at least have tolled ride possible
            _helpConfiguration.postValue(HelpConfiguration.BOTH)
        }
    }

    enum class HelpConfiguration {
        TOLLED, SENT, BOTH
    }
}