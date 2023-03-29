package pl.gov.mf.etoll.front.errors.instanceissues

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import javax.inject.Scope


@InstanceIssuesFragmentScope
@Subcomponent(modules = [InstanceIssuesFragmentModule::class])
interface InstanceIssuesFragmentComponent : BaseComponent<InstanceIssuesViewModel> {
    fun inject(target: InstanceIssuesFragment)
}

@Module
class InstanceIssuesFragmentModule(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle
) {

    @Provides
    @InstanceIssuesFragmentScope
    fun providesViewModel(): InstanceIssuesViewModel =
        ViewModelProvider(viewModelStoreOwner).get(InstanceIssuesViewModel::class.java)
            .apply {
                lifecycle.addObserver(this)
            }
}

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class InstanceIssuesFragmentScope