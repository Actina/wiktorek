package pl.gov.mf.etoll.front.security.settings

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import javax.inject.Scope

@SecuritySettingsScope
@Subcomponent(modules = [SecuritySettingsFragmentModule::class])
interface SecuritySettingsFragmentComponent : BaseComponent<SecuritySettingsViewModel> {
    fun inject(target: SecuritySettingsFragment)
}

@Module
class SecuritySettingsFragmentModule(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle
) {

    @Provides
    @SecuritySettingsScope
    fun providesViewModel(): SecuritySettingsViewModel =
        ViewModelProvider(viewModelStoreOwner).get(SecuritySettingsViewModel::class.java)
            .apply {
                lifecycle.addObserver(this)
            }
}

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class SecuritySettingsScope