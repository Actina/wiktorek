package pl.gov.mf.etoll.front.dashboard

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import javax.inject.Scope


@DashboardScope
@Subcomponent(modules = [DashboardFragmentModule::class])
interface DashboardFragmentComponent : BaseComponent<DashboardViewModel> {
    fun inject(target: DashboardFragment)
}

@Module
class DashboardFragmentModule(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle
) {

    @Provides
    @DashboardScope
    fun providesViewModel(): DashboardViewModel =
        ViewModelProvider(viewModelStoreOwner).get(DashboardViewModel::class.java)
            .apply {
                lifecycle.addObserver(this)
            }

    @Provides
    @DashboardScope
    fun providesFlavourDelegate(impl: DashboardFlavourDelegateImpl): DashboardFlavourDelegate = impl
}

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class DashboardScope