package pl.gov.mf.etoll.front.regulationsv2

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import javax.inject.Scope


@RegulationsFragmentScope
@Subcomponent(modules = [RegulationsFragmentV2Module::class])
interface RegulationsFragmentV2Component :
    BaseComponent<RegulationsFragmentV2ViewModel> {
    fun inject(target: RegulationsFragmentV2)
}

@Module
class RegulationsFragmentV2Module(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle,
) {

    @Provides
    @RegulationsFragmentScope
    fun providesViewModel(): RegulationsFragmentV2ViewModel =
        ViewModelProvider(viewModelStoreOwner).get(RegulationsFragmentV2ViewModel::class.java)
            .apply {
                lifecycle.addObserver(this)
            }
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class RegulationsFragmentScope