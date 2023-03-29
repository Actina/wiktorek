package pl.gov.mf.etoll.front.settings.appsettings

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import javax.inject.Scope

@AppSettingsScope
@Subcomponent(modules = [AppSettingsFragmentModule::class])
interface AppSettingsFragmentComponent : BaseComponent<AppSettingsViewModel> {
    fun inject(target: AppSettingsFragment)
}

@Module
class AppSettingsFragmentModule(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle
) {

    @Provides
    @AppSettingsScope
    fun providesViewModel(): AppSettingsViewModel =
        ViewModelProvider(viewModelStoreOwner).get(AppSettingsViewModel::class.java)
            .apply {
                lifecycle.addObserver(this)
            }
}

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class AppSettingsScope