package pl.gov.mf.etoll.front.ridedetailsmap

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import javax.inject.Scope

@RideDetailsMapFragmentScope
@Subcomponent(modules = [RideDetailsMapFragmentModule::class])
interface RideDetailsMapFragmentComponent :
    BaseComponent<RideDetailsMapFragmentViewModel> {
    fun inject(target: RideDetailsMapFragment)
}

@Module
class RideDetailsMapFragmentModule(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle
) {

    @Provides
    @RideDetailsMapFragmentScope
    fun providesViewModel(): RideDetailsMapFragmentViewModel =
        ViewModelProvider(viewModelStoreOwner).get(RideDetailsMapFragmentViewModel::class.java)
            .apply {
                lifecycle.addObserver(this)
            }
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class RideDetailsMapFragmentScope