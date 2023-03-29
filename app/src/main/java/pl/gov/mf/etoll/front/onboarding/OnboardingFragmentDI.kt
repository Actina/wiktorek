package pl.gov.mf.etoll.front.onboarding

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import pl.gov.mf.etoll.front.onboarding.viewpager.OnboardingViewPagerAdapter
import javax.inject.Scope

@OnboardingScope
@Subcomponent(modules = [OnboardingFragmentModule::class])
interface OnboardingFragmentComponent : BaseComponent<OnboardingViewModel> {
    fun inject(target: OnboardingFragment)
}

@Module
class OnboardingFragmentModule(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle
) {

    @Provides
    @OnboardingScope
    fun providesViewModel(): OnboardingViewModel =
        ViewModelProvider(viewModelStoreOwner).get(OnboardingViewModel::class.java)
            .apply {
                lifecycle.addObserver(this)
            }

    @Provides
    @OnboardingScope
    fun providesAdapter(): OnboardingViewPagerAdapter = OnboardingViewPagerAdapter()
}

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class OnboardingScope