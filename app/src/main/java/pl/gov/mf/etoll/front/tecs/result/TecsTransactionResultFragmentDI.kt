package pl.gov.mf.etoll.front.tecs.result

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import javax.inject.Scope

@TecsTransactionResultScope
@Subcomponent(modules = [TecsTransactionResultFragmentModule::class])
interface TecsTransactionResultFragmentComponent : BaseComponent<TecsTransactionResultViewModel> {
    fun inject(target: TecsTransactionResultFragment)
}

@Module
class TecsTransactionResultFragmentModule(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle
) {

    @Provides
    @TecsTransactionResultScope
    fun providesViewModel(): TecsTransactionResultViewModel =
        ViewModelProvider(viewModelStoreOwner).get(TecsTransactionResultViewModel::class.java)
            .apply {
                lifecycle.addObserver(this)
            }
}

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class TecsTransactionResultScope