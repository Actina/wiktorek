package pl.gov.mf.etoll.front.errors.gpsissues

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import javax.inject.Scope


@GpsIssuesFragmentScope
@Subcomponent(modules = [GpsIssuesFragmentModule::class])
interface GpsIssuesFragmentComponent : BaseComponent<GpsIssuesViewModel> {
    fun inject(target: GpsIssuesFragment)
}

@Module
class GpsIssuesFragmentModule(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle
) {

    @Provides
    @GpsIssuesFragmentScope
    fun providesViewModel(): GpsIssuesViewModel =
        ViewModelProvider(viewModelStoreOwner).get(GpsIssuesViewModel::class.java)
            .apply {
                lifecycle.addObserver(this)
            }
}

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class GpsIssuesFragmentScope