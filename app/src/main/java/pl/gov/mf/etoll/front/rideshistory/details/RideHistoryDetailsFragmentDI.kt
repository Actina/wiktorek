package pl.gov.mf.etoll.front.rideshistory.details

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import pl.gov.mf.etoll.front.rideshistory.details.adapter.RideHistoryDetailsAdapter
import javax.inject.Scope

@RideHistoryDetailsFragmentScope
@Subcomponent(modules = [RideHistoryDetailsFragmentModule::class])
interface RideHistoryDetailsFragmentComponent :
    BaseComponent<RideHistoryDetailsFragmentViewModel> {
    fun inject(target: RideHistoryDetailsFragment)
}

@Module
class RideHistoryDetailsFragmentModule(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle
) {

    @Provides
    @RideHistoryDetailsFragmentScope
    fun providesViewModel(): RideHistoryDetailsFragmentViewModel =
        ViewModelProvider(viewModelStoreOwner).get(RideHistoryDetailsFragmentViewModel::class.java)
            .apply {
                lifecycle.addObserver(this)
            }

    @Provides
    @RideHistoryDetailsFragmentScope
    fun providesAdapter(): RideHistoryDetailsAdapter = RideHistoryDetailsAdapter()
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class RideHistoryDetailsFragmentScope