package pl.gov.mf.etoll.front.settings.darkmode

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import javax.inject.Scope

@DarkModeSettingsScope
@Subcomponent(modules = [DarkModeSettingsFragmentModule::class])
interface DarkModeSettingsFragmentComponent : BaseComponent<DarkModeSettingsViewModel> {
    fun inject(target: DarkModeSettingsFragment)
}

@Module
class DarkModeSettingsFragmentModule(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle,
) {

    @Provides
    @DarkModeSettingsScope
    fun providesViewModel(): DarkModeSettingsViewModel =
        ViewModelProvider(viewModelStoreOwner).get(DarkModeSettingsViewModel::class.java)
            .apply {
                lifecycle.addObserver(this)
            }
}

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class DarkModeSettingsScope