package pl.gov.mf.etoll.front.errors.timeissues

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import javax.inject.Scope


@TimeIssuesFragmentScope
@Subcomponent(modules = [TimeIssuesFragmentModule::class])
interface TimeIssuesFragmentComponent : BaseComponent<TimeIssuesViewModel> {
    fun inject(target: TimeIssuesFragment)
}

@Module
class TimeIssuesFragmentModule(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle
) {

    @Provides
    @TimeIssuesFragmentScope
    fun providesViewModel(): TimeIssuesViewModel =
        ViewModelProvider(viewModelStoreOwner).get(TimeIssuesViewModel::class.java)
            .apply {
                // FYI: No need to care about removing observer: https://github.com/googlecodelabs/android-lifecycles/issues/5
                lifecycle.addObserver(this)
            }
}

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class TimeIssuesFragmentScope