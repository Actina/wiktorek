package pl.gov.mf.etoll.front.about

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import javax.inject.Scope

@AboutScope
@Subcomponent(modules = [AboutFragmentModule::class])
interface AboutFragmentComponent : BaseComponent<AboutViewModel> {
    fun inject(target: AboutFragment)
}

@Module
class AboutFragmentModule(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle
) {

    @Provides
    @AboutScope
    fun providesViewModel(): AboutViewModel =
        ViewModelProvider(viewModelStoreOwner).get(AboutViewModel::class.java)
            .apply {
                lifecycle.addObserver(this)
            }
}

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class AboutScope