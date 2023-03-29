package pl.gov.mf.etoll.front.configsentridesselection

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import pl.gov.mf.etoll.front.configsentridesselection.adapter.SentRidesAdapter
import javax.inject.Scope

@SentRidesSelectionFragmentScope
@Subcomponent(modules = [SentRidesSelectionFragmentModule::class])
interface SentRidesSelectionFragmentComponent :
    BaseComponent<ConfigSentRidesSelectionFragmentViewModel> {
    fun inject(target: ConfigSentRidesSelectionFragment)
}

@Module
class SentRidesSelectionFragmentModule(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle
) {

    @Provides
    @SentRidesSelectionFragmentScope
    fun providesViewModel(): ConfigSentRidesSelectionFragmentViewModel =
        ViewModelProvider(viewModelStoreOwner).get(ConfigSentRidesSelectionFragmentViewModel::class.java)
            .apply {
                lifecycle.addObserver(this)
            }

    @Provides
    @SentRidesSelectionFragmentScope
    fun providesAdapter(): SentRidesAdapter {
        return SentRidesAdapter()
    }
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class SentRidesSelectionFragmentScope