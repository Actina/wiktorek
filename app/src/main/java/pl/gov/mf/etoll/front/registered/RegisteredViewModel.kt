package pl.gov.mf.etoll.front.registered

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.gov.mf.etoll.BuildConfig
import pl.gov.mf.etoll.base.BaseComposeViewModel
import pl.gov.mf.etoll.core.CoreComposedUC
import pl.gov.mf.etoll.networking.NetworkingUC
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC
import pl.gov.mf.etoll.translations.TranslationKeys
import pl.gov.mf.etoll.translations.TranslationsContainer
import pl.gov.mf.mobile.utils.addSafe
import javax.inject.Inject

class RegisteredViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : BaseComposeViewModel(savedStateHandle) {

    @Inject
    lateinit var writeSettingsUseCase: SettingsUC.WriteSettingsUseCase

    @Inject
    lateinit var readSettingsUseCase: SettingsUC.ReadSettingsUseCase

    @Inject
    lateinit var getBusinessNumberUseCase: NetworkingUC.GetBusinessNumberUseCase

    @Inject
    lateinit var showInstanceIssueUseCase: CoreComposedUC.ShowInstanceIssueUseCase

    private val _navigationSource: MutableLiveData<NavTarget> = MutableLiveData(NavTarget.NONE)
    val navigationSource: LiveData<NavTarget>
        get() = _navigationSource

    private val _businessNumber = MutableLiveData("")
    val businessNumber: LiveData<String>
        get() = _businessNumber

    override fun onResume() {
        super.onResume()
        if (readSettingsUseCase.executeForBoolean(Settings.NETWORK_COMMUNICATION_LOCKED)) {
            showInstanceIssueUseCase.execute()
        }
        if (businessNumber.value.isNullOrEmpty())
            compositeDisposable.addSafe(
                getBusinessNumberUseCase.execute()
                    .subscribe({
                        _businessNumber.postValue(it)
                    }, {
                        // TODO: revert registration, smth went wrong
                        if (BuildConfig.DEBUG)
                            _businessNumber.postValue("Dummy value")
                    })
            )
    }

    override fun generateTranslations(): TranslationsContainer = TranslationsContainer(
        keys = mutableListOf(
            TranslationKeys.REGISTERED_MODE_HEADER,
            TranslationKeys.REGISTERED_MODE_CONTINUE_BUTTON,
            TranslationKeys.REGISTERED_MODE_BUSINESS_IDENTIFIER_CONTENT,
            TranslationKeys.REGISTERED_MODE_ASSIGN_VEHICLE_TITLE,
            TranslationKeys.REGISTERED_MODE_ASSIGN_VEHICLE_CONTENT,
            TranslationKeys.REGISTERED_MODE_ASSIGN_VEHICLE_LINK,
            TranslationKeys.REGISTERED_MODE_ASSIGN_SENT_CONTENT,
            TranslationKeys.REGISTERED_MODE_ASSIGN_SENT_LINK,
        )
    )

    fun onContinueClicked() {
        _navigationSource.postValue(NavTarget.DRIVER_WARNING)
    }

    fun resetNavigationTarget() {
        _navigationSource.postValue(NavTarget.NONE)
    }

    fun markViewStepAsShown(function: () -> Unit) {
        onIO {
            writeSettingsUseCase.executeV2(Settings.ACTIVATION_TRANS_SUCCEED, true)
            viewModelScope.launch(Dispatchers.Main) {
                function()
            }
        }
    }

    enum class NavTarget {
        NONE, DRIVER_WARNING
    }
}