package pl.gov.mf.etoll.front.settings.languagesettingsv2

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import javax.inject.Scope

@LanguageSettingsFragmentScope
@Subcomponent(modules = [LanguageSettingsFragmentV2Module::class])
interface LanguageSettingsFragmentV2Component : BaseComponent<LanguageSettingsViewModelV2> {
    fun inject(target: LanguageSettingsFragmentV2)
}

@Module
class LanguageSettingsFragmentV2Module(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle
) {
    @Provides
    @LanguageSettingsFragmentScope
    fun providesViewModel(): LanguageSettingsViewModelV2 =
        ViewModelProvider(viewModelStoreOwner).get(LanguageSettingsViewModelV2::class.java)
            .apply {
                lifecycle.addObserver(this)
            }
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class LanguageSettingsFragmentScope
