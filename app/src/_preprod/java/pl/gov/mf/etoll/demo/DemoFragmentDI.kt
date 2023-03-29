package pl.gov.mf.etoll.demo

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import javax.inject.Scope

@DemoScope
@Subcomponent(modules = [DemoFragmentModule::class])
interface DemoFragmentComponent : BaseComponent<DemoFragmentViewModel> {
    fun inject(target: DemoFragment)
}

@Module
class DemoFragmentModule(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle,
) {

    @Provides
    @DemoScope
    fun providesViewModel(): DemoFragmentViewModel =
        ViewModelProvider(viewModelStoreOwner).get(DemoFragmentViewModel::class.java)
            .apply {
                lifecycle.addObserver(this)
            }

}

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class DemoScope