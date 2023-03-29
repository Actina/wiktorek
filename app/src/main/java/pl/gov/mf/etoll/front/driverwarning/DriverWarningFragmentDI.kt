package pl.gov.mf.etoll.front.driverwarning

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import javax.inject.Scope

@DriverWarningFragmentScope
@Subcomponent(modules = [DriverWarningFragmentModule::class])
interface DriverWarningFragmentComponent : BaseComponent<DriverWarningFragmentViewModel> {
    fun inject(target: DriverWarningFragment)
}

@Module
class DriverWarningFragmentModule(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle
) {
    @Provides
    @DriverWarningFragmentScope
    fun providesViewModel(): DriverWarningFragmentViewModel =
        ViewModelProvider(viewModelStoreOwner).get(DriverWarningFragmentViewModel::class.java)
            .apply {
                lifecycle.addObserver(this)
            }
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class DriverWarningFragmentScope
