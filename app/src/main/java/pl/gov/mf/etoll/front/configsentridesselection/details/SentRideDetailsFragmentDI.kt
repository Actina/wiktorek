package pl.gov.mf.etoll.front.configsentridesselection.details

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import javax.inject.Scope

@SentRideDetailsFragmentScope
@Subcomponent(modules = [SentRideDetailsFragmentModule::class])
interface SentRideDetailsFragmentComponent :
    BaseComponent<SentRideDetailsFragmentViewModel> {
    fun inject(target: SentRideDetailsFragment)
}

@Module
class SentRideDetailsFragmentModule(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle
) {

    @Provides
    @SentRideDetailsFragmentScope
    fun providesViewModel(): SentRideDetailsFragmentViewModel =
        ViewModelProvider(viewModelStoreOwner).get(SentRideDetailsFragmentViewModel::class.java)
            .apply {
                lifecycle.addObserver(this)
            }
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class SentRideDetailsFragmentScope