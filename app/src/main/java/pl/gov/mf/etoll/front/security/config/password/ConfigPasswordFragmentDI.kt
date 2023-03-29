package pl.gov.mf.etoll.front.security.config.password

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import javax.inject.Scope

@ConfigPasswordFragmentScope
@Subcomponent(modules = [ConfigPasswordFragmentModule::class])
interface ConfigPasswordFragmentComponent : BaseComponent<ConfigPasswordViewModel> {
    fun inject(target: ConfigPasswordFragment)
}

@Module
class ConfigPasswordFragmentModule(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle
) {

    @Provides
    @ConfigPasswordFragmentScope
    fun providesViewModel(): ConfigPasswordViewModel =
        ViewModelProvider(viewModelStoreOwner).get(ConfigPasswordViewModel::class.java)
            .apply {
                lifecycle.addObserver(this)
            }
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfigPasswordFragmentScope