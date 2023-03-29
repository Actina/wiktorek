package pl.gov.mf.etoll.front.notificationdetails

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import javax.inject.Scope

@NotificationDetailsFragmentScope
@Subcomponent(modules = [NotificationDetailsFragmentModule::class])
interface NotificationDetailsFragmentComponent :
    BaseComponent<NotificationDetailsFragmentViewModel> {
    fun inject(target: NotificationDetailsFragment)
}

@Module
class NotificationDetailsFragmentModule(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle
) {

    @Provides
    @NotificationDetailsFragmentScope
    fun providesViewModel(): NotificationDetailsFragmentViewModel =
        ViewModelProvider(viewModelStoreOwner).get(NotificationDetailsFragmentViewModel::class.java)
            .apply {
                lifecycle.addObserver(this)
            }
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class NotificationDetailsFragmentScope