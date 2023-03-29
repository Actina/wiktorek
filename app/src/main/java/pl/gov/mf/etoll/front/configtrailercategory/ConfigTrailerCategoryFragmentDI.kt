package pl.gov.mf.etoll.front.configtrailercategory

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import javax.inject.Scope

@ConfigTrailerCategoryFragmentScope
@Subcomponent(modules = [ConfigTrailerCategoryFragmentModule::class])
interface ConfigTrailerCategoryFragmentComponent :
    BaseComponent<ConfigTrailerCategoryFragmentViewModel> {
    fun inject(target: ConfigTrailerCategoryFragment)
}

@Module
class ConfigTrailerCategoryFragmentModule(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle
) {

    @Provides
    @ConfigTrailerCategoryFragmentScope
    fun providesViewModel(): ConfigTrailerCategoryFragmentViewModel =
        ViewModelProvider(viewModelStoreOwner).get(ConfigTrailerCategoryFragmentViewModel::class.java)
            .apply {
                lifecycle.addObserver(this)
            }
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfigTrailerCategoryFragmentScope