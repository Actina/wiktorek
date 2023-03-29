package pl.gov.mf.etoll.front.rideshistory

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import pl.gov.mf.etoll.front.rideshistory.adapter.RidesHistoryAdapter
import javax.inject.Scope

@RideHistoryFragmentScope
@Subcomponent(modules = [RideHistoryFragmentModule::class])
interface RideHistoryFragmentComponent :
    BaseComponent<RideHistoryFragmentViewModel> {
    fun inject(target: RideHistoryFragment)
}

@Module
class RideHistoryFragmentModule(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle
) {

    @Provides
    @RideHistoryFragmentScope
    fun providesViewModel(): RideHistoryFragmentViewModel =
        ViewModelProvider(viewModelStoreOwner).get(RideHistoryFragmentViewModel::class.java)
            .apply {
                lifecycle.addObserver(this)
            }

    @Provides
    @RideHistoryFragmentScope
    fun providesAdapter(): RidesHistoryAdapter = RidesHistoryAdapter()
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class RideHistoryFragmentScope