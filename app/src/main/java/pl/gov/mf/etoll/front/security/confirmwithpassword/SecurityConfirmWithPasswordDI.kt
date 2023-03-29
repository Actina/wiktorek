package pl.gov.mf.etoll.front.security.confirmwithpassword

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import javax.inject.Scope

@SecurityConfirmWithPasswordScope
@Subcomponent(modules = [SecurityConfirmWithPasswordFragmentModule::class])
interface SecurityConfirmWithPasswordFragmentComponent :
    BaseComponent<SecurityConfirmWithPasswordViewModel> {
    fun inject(target: SecurityConfirmWithPasswordFragment)
}

@Module
class SecurityConfirmWithPasswordFragmentModule(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle
) {

    @Provides
    @SecurityConfirmWithPasswordScope
    fun providesViewModel(): SecurityConfirmWithPasswordViewModel =
        ViewModelProvider(viewModelStoreOwner).get(SecurityConfirmWithPasswordViewModel::class.java)
            .apply {
                lifecycle.addObserver(this)
            }
}

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class SecurityConfirmWithPasswordScope