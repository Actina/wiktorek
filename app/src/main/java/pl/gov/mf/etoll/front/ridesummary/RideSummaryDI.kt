package pl.gov.mf.etoll.front.ridesummary

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import pl.gov.mf.etoll.front.shared.adapter.CardsAdapter
import javax.inject.Scope

@RideSummaryScope
@Subcomponent(modules = [RideSummaryFragmentModule::class])
interface RideSummaryFragmentComponent : BaseComponent<RideSummaryViewModel> {
    fun inject(target: RideSummaryFragment)
}

@Module
class RideSummaryFragmentModule(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle,
    private val lifecycleOwner: LifecycleOwner
) {

    @Provides
    @RideSummaryScope
    fun providesViewModel(): RideSummaryViewModel =
        ViewModelProvider(viewModelStoreOwner).get(RideSummaryViewModel::class.java)
            .apply {
                lifecycle.addObserver(this)
            }

    @Provides
    @RideSummaryScope
    fun providesAdapter(): CardsAdapter = CardsAdapter()
}

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class RideSummaryScope