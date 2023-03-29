package pl.gov.mf.etoll.front.configmonitoringdevice

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import javax.inject.Scope

@ConfigMonitoringDeviceFragmentScope
@Subcomponent(modules = [ConfigMonitoringDeviceFragmentModule::class])
interface ConfigMonitoringDeviceFragmentComponent :
    BaseComponent<ConfigMonitoringDeviceFragmentViewModel> {
    fun inject(target: ConfigMonitoringDeviceFragment)
}

@Module
class ConfigMonitoringDeviceFragmentModule(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle
) {

    @Provides
    @ConfigMonitoringDeviceFragmentScope
    fun providesViewModel(): ConfigMonitoringDeviceFragmentViewModel =
        ViewModelProvider(viewModelStoreOwner).get(ConfigMonitoringDeviceFragmentViewModel::class.java)
            .apply {
                lifecycle.addObserver(this)
            }
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfigMonitoringDeviceFragmentScope