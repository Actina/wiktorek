package pl.gov.mf.etoll.front.tecs.amountSelection

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import javax.inject.Scope


@TecsAmountSelectionScope
@Subcomponent(modules = [TecsAmountSelectionFragmentModule::class])
interface TecsAmountSelectionFragmentComponent : BaseComponent<TecsAmountSelectionViewModel> {
    fun inject(target: TecsAmountSelectionFragment)
}

@Module
class TecsAmountSelectionFragmentModule(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle,
    private val lifecycleOwner: LifecycleOwner
) {

    @Provides
    @TecsAmountSelectionScope
    fun providesViewModel(): TecsAmountSelectionViewModel =
        ViewModelProvider(viewModelStoreOwner).get(TecsAmountSelectionViewModel::class.java)
            .apply {
                lifecycle.addObserver(this)
            }

    @Provides
    @TecsAmountSelectionScope
    fun providesAdapter(): TecsAmountSelectionAdapter = TecsAmountSelectionAdapter(lifecycleOwner)
}

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class TecsAmountSelectionScope