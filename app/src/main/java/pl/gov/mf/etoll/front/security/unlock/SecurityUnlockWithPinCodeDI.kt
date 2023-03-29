package pl.gov.mf.etoll.front.security.unlock

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import javax.inject.Scope

@SecurityUnlockWithPinCodeScope
@Subcomponent(modules = [SecurityUnlockWithPinCodeFragmentModule::class])
interface SecurityUnlockWithPinCodeFragmentComponent :
    BaseComponent<SecurityUnlockWithPinCodeViewModel> {
    fun inject(target: SecurityUnlockWithPinCodeFragment)
}

@Module
class SecurityUnlockWithPinCodeFragmentModule(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle
) {

    @Provides
    @SecurityUnlockWithPinCodeScope
    fun providesViewModel(): SecurityUnlockWithPinCodeViewModel =
        ViewModelProvider(viewModelStoreOwner).get(SecurityUnlockWithPinCodeViewModel::class.java)
            .apply {
                lifecycle.addObserver(this)
            }

    @Provides
    @SecurityUnlockWithPinCodeScope
    fun providesSecurityUnlockWithPinCodeUpdateUiUC(context: Context) =
        SecurityUnlockWithPinCodeUC.UpdateUiUseCase(context)
}

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class SecurityUnlockWithPinCodeScope