package pl.gov.mf.etoll.front.ridedata

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import javax.inject.Scope

@RideDataScope
@Subcomponent(modules = [RideDataFragmentModule::class])
interface RideDataFragmentComponent : BaseComponent<RideDataViewModel> {
    fun inject(target: RideDataFragment)
}

@Module
class RideDataFragmentModule(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle
) {

    @Provides
    @RideDataScope
    fun providesViewModel(): RideDataViewModel =
        ViewModelProvider(viewModelStoreOwner).get(RideDataViewModel::class.java)
            .apply {
                lifecycle.addObserver(this)
            }
}

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class RideDataScope