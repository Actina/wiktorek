package pl.gov.mf.etoll.front.ridedetails.sentselection

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import pl.gov.mf.etoll.front.ridedetails.sentselection.adapter.SentRidesAdapter
import javax.inject.Scope

@RideDetailsSentSelectionFragmentScope
@Subcomponent(modules = [RideDetailsSentSelectionFragmentModule::class])
interface RideDetailsSentSelectionFragmentComponent :
    BaseComponent<RideDetailsSentSelectionFragmentViewModel> {
    fun inject(target: RideDetailsSentSelectionFragment)
}

@Module
class RideDetailsSentSelectionFragmentModule(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle
) {

    @Provides
    @RideDetailsSentSelectionFragmentScope
    fun providesViewModel(): RideDetailsSentSelectionFragmentViewModel =
        ViewModelProvider(viewModelStoreOwner).get(RideDetailsSentSelectionFragmentViewModel::class.java)
            .apply {
                lifecycle.addObserver(this)
            }

    @Provides
    @RideDetailsSentSelectionFragmentScope
    fun providesAdapter(): SentRidesAdapter {
        return SentRidesAdapter()
    }
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class RideDetailsSentSelectionFragmentScope