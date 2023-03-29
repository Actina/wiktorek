package pl.gov.mf.etoll.front.configvehicleselection

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import javax.inject.Scope

@ConfigVehicleSelectionFragmentScope
@Subcomponent(modules = [ConfigVehicleSelectionFragmentModule::class])
interface ConfigVehicleSelectionFragmentComponent :
    BaseComponent<ConfigVehicleSelectionFragmentViewModel> {
    fun inject(target: ConfigVehicleSelectionFragment)
}

@Module
class ConfigVehicleSelectionFragmentModule(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle
) {

    @Provides
    @ConfigVehicleSelectionFragmentScope
    fun providesViewModel(): ConfigVehicleSelectionFragmentViewModel =
        ViewModelProvider(viewModelStoreOwner).get(
            ConfigVehicleSelectionFragmentViewModel::class.java
        )
            .apply {
                lifecycle.addObserver(this)
            }
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfigVehicleSelectionFragmentScope