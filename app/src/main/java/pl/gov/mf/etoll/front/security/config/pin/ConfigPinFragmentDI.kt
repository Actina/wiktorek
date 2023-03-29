package pl.gov.mf.etoll.front.security.config.pin

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import javax.inject.Scope

@ConfigPinFragmentScope
@Subcomponent(modules = [ConfigPinFragmentModule::class])
interface ConfigPinFragmentComponent : BaseComponent<ConfigPinViewModel> {
    fun inject(target: ConfigPinFragment)
}

@Module
class ConfigPinFragmentModule(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle
) {

    @Provides
    @ConfigPinFragmentScope
    fun providesViewModel(): ConfigPinViewModel =
        ViewModelProvider(viewModelStoreOwner).get(ConfigPinViewModel::class.java)
            .apply {
                lifecycle.addObserver(this)
            }
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfigPinFragmentScope