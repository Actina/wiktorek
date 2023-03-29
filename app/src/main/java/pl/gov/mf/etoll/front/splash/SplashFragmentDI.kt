package pl.gov.mf.etoll.front.splash

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import javax.inject.Scope

@StartupActivitySplashFragmentScope
@Subcomponent(modules = [StartupActivitySplashFragmentModule::class])
interface StartupActivitySplashFragmentComponent : BaseComponent<SplashFragmentViewModel> {
    fun inject(target: SplashFragment)
}

@Module
class StartupActivitySplashFragmentModule(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle
) {

    @Provides
    @StartupActivitySplashFragmentScope
    fun providesViewModel(): SplashFragmentViewModel =
        ViewModelProvider(viewModelStoreOwner).get(SplashFragmentViewModel::class.java)
            .apply {
                // FYI: No need to care about removing observer: https://github.com/googlecodelabs/android-lifecycles/issues/5
                lifecycle.addObserver(this)
            }
}

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class StartupActivitySplashFragmentScope