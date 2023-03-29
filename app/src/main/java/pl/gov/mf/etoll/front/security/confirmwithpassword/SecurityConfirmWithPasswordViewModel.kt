package pl.gov.mf.etoll.front.security.confirmwithpassword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import pl.gov.mf.etoll.base.BaseDatabindingViewModel
import pl.gov.mf.etoll.core.security.SecurityConfigUC
import pl.gov.mf.etoll.front.security.confirmwithpassword.models.ConfirmWithPasswordData
import pl.gov.mf.etoll.front.security.confirmwithpassword.types.ConfirmWithPasswordOperationType
import pl.gov.mf.mobile.utils.addSafe
import javax.inject.Inject

class SecurityConfirmWithPasswordViewModel : BaseDatabindingViewModel() {

    @Inject
    lateinit var checkPasswordUseCase: SecurityConfigUC.CheckPasswordUseCase

    @Inject
    lateinit var clearSecurityConfigUseCase: SecurityConfigUC.ClearSecurityConfigUseCase

    private var _confirmationType = MutableLiveData(ConfirmWithPasswordOperationType.NONE)

    private val _passwordConfirmation = MutableLiveData(ConfirmWithPasswordData())
    val passwordConfirmation: LiveData<ConfirmWithPasswordData>
        get() = _passwordConfirmation

    private val _navDirections = MutableLiveData(NavigationTarget.NONE)
    val navDirections: LiveData<NavigationTarget>
        get() = _navDirections

    val untranslatedConfirmPasswordContent: LiveData<String> =
        Transformations.map(_confirmationType) {
            if (it == ConfirmWithPasswordOperationType.SECURITY_SWITCHING_OFF)
                "security_confirm_with_password_content_on_close"
            else "security_confirm_with_password_content"
        }

    fun checkPassword() {
        val password = _passwordConfirmation.value?.password ?: run {
            _navDirections.postValue(NavigationTarget.INVALID_PASSWORD)
            return
        }

        compositeDisposable.addSafe(
            checkPasswordUseCase.execute(password)
                .subscribe({ isValid ->
                    if (isValid) handleFurtherNavigation()
                    else _navDirections.value = NavigationTarget.INVALID_PASSWORD
                }, {})
        )
    }

    fun clearNavigation() {
        _navDirections.value = NavigationTarget.NONE
    }

    fun setConfirmationType(confirmationType: ConfirmWithPasswordOperationType) {
        _confirmationType.value = confirmationType
    }

    private fun handleFurtherNavigation() {
        when (_confirmationType.value) {
            ConfirmWithPasswordOperationType.PIN_RESET -> _navDirections.postValue(NavigationTarget.SETTINGS)
            ConfirmWithPasswordOperationType.SECURITY_SWITCHING_OFF -> clearSecurityConfig()
            else -> Unit
        }
    }

    private fun clearSecurityConfig() {
        compositeDisposable.addSafe(
            clearSecurityConfigUseCase.execute()
                .subscribe({
                    _navDirections.value = NavigationTarget.BACK
                }, {})
        )
    }

    enum class NavigationTarget {
        NONE, INVALID_PASSWORD, BACK, SETTINGS,
    }
}