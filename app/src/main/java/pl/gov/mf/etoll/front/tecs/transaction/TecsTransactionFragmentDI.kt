package pl.gov.mf.etoll.front.tecs.transaction

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.front.tecs.transaction.counter.TecsCounterImpl
import pl.gov.mf.etoll.base.BaseComponent
import pl.gov.mf.etoll.front.tecs.transaction.counter.TecsCounter
import javax.inject.Scope


@TecsTransactionScope
@Subcomponent(modules = [TecsTransactionFragmentModule::class])
interface TecsTransactionFragmentComponent : BaseComponent<TecsTransactionFragmentViewModel> {
    fun inject(target: TecsTransactionFragment)
}

@Module
class TecsTransactionFragmentModule(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle
) {

    @Provides
    @TecsTransactionScope
    fun providesViewModel(): TecsTransactionFragmentViewModel =
        ViewModelProvider(viewModelStoreOwner).get(TecsTransactionFragmentViewModel::class.java)
            .apply {
                lifecycle.addObserver(this)
            }

    @Provides
    @TecsTransactionScope
    fun providesTecsCounter(): TecsCounter = TecsCounterImpl()
}

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class TecsTransactionScope