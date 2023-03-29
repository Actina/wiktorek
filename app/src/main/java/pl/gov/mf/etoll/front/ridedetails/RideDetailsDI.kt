package pl.gov.mf.etoll.front.ridedetails

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import javax.inject.Scope

@RideDetailsScope
@Subcomponent(modules = [RideDetailsFragmentModule::class])
interface RideDetailsFragmentComponent : BaseComponent<RideDetailsViewModel> {
    fun inject(target: RideDetailsFragment)
}

@Module
class RideDetailsFragmentModule(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle
) {

    @Provides
    @RideDetailsScope
    fun providesViewModel(): RideDetailsViewModel =
        ViewModelProvider(viewModelStoreOwner).get(RideDetailsViewModel::class.java)
            .apply {
                lifecycle.addObserver(this)
            }
}

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class RideDetailsScope