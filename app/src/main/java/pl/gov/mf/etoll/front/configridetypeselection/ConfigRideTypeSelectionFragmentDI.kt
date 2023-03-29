package pl.gov.mf.etoll.front.configridetypeselection

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import javax.inject.Scope

@ConfigRideTypeSelectionFragmentScope
@Subcomponent(modules = [ConfigRideTypeSelectionFragmentModule::class])
interface ConfigRideTypeSelectionFragmentComponent :
    BaseComponent<ConfigRideTypeSelectionFragmentViewModel> {
    fun inject(target: ConfigRideTypeSelectionFragment)
}

@Module
class ConfigRideTypeSelectionFragmentModule(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle
) {

    @Provides
    @ConfigRideTypeSelectionFragmentScope
    fun providesViewModel(): ConfigRideTypeSelectionFragmentViewModel =
        ViewModelProvider(viewModelStoreOwner).get(ConfigRideTypeSelectionFragmentViewModel::class.java)
            .apply {
                lifecycle.addObserver(this)
            }
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfigRideTypeSelectionFragmentScope