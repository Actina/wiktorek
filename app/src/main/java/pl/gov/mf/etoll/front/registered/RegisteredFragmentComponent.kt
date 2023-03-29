package pl.gov.mf.etoll.front.registered

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import javax.inject.Scope

@RegisteredFragmentScope
@Subcomponent(modules = [RegisteredFragmentModule::class])
interface RegisteredFragmentComponent : BaseComponent<RegisteredViewModel> {
    fun inject(target: RegisteredFragment)
}

@Module
class RegisteredFragmentModule(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle
) {
    @Provides
    @RegisteredFragmentScope
    fun providesViewModel(): RegisteredViewModel = ViewModelProvider(viewModelStoreOwner)
        .get(RegisteredViewModel::class.java).apply {
            lifecycle.addObserver(this)
        }
}


@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class RegisteredFragmentScope