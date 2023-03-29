package pl.gov.mf.etoll.front.security.resettounlock

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import javax.inject.Scope

@SecurityResetPinCodeToUnlockScope
@Subcomponent(modules = [SecurityResetPinCodeToUnlockFragmentModule::class])
interface SecurityResetPinCodeToUnlockFragmentComponent :
    BaseComponent<SecurityResetPinCodeToUnlockViewModel> {
    fun inject(target: SecurityResetPinCodeToUnlockFragment)
}

@Module
class SecurityResetPinCodeToUnlockFragmentModule(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle
) {

    @Provides
    @SecurityResetPinCodeToUnlockScope
    fun providesViewModel(): SecurityResetPinCodeToUnlockViewModel =
        ViewModelProvider(viewModelStoreOwner).get(SecurityResetPinCodeToUnlockViewModel::class.java)
            .apply {
                lifecycle.addObserver(this)
            }

    @Provides
    @SecurityResetPinCodeToUnlockScope
    fun providesSecurityResetPinCodeToUnlockUCUpdateUiUC(context: Context) =
        SecurityResetPinCodeToUnlockUC.UpdateUiUseCase(context)
}

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class SecurityResetPinCodeToUnlockScope