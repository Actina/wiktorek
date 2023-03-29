package pl.gov.mf.etoll.front.help

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import javax.inject.Scope

@HelpScope
@Subcomponent(modules = [HelpFragmentModule::class])
interface HelpFragmentComponent : BaseComponent<HelpViewModel> {
    fun inject(target: HelpFragment)
}

@Module
class HelpFragmentModule(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle
) {

    @Provides
    @HelpScope
    fun providesViewModel(): HelpViewModel =
        ViewModelProvider(viewModelStoreOwner).get(HelpViewModel::class.java)
            .apply {
                lifecycle.addObserver(this)
            }

    @Provides
    @HelpScope
    fun providesAdapter(): HelpFragmentViewPagerAdapter = HelpFragmentViewPagerAdapter()
}

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class HelpScope