package pl.gov.mf.etoll.front.notificationhistory

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.gov.mf.etoll.base.BaseComponent
import pl.gov.mf.etoll.front.notificationhistory.list.NotificationHistoryAdapter
import javax.inject.Scope

@NotificationHistoryFragmentScope
@Subcomponent(modules = [NotificationHistoryFragmentModule::class])
interface NotificationHistoryFragmentComponent :
    BaseComponent<NotificationHistoryFragmentViewModel> {
    fun inject(target: NotificationHistoryFragment)
}

@Module
class NotificationHistoryFragmentModule(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle
) {

    @Provides
    @NotificationHistoryFragmentScope
    fun providesViewModel(): NotificationHistoryFragmentViewModel =
        ViewModelProvider(viewModelStoreOwner).get(NotificationHistoryFragmentViewModel::class.java)
            .apply {
                lifecycle.addObserver(this)
            }

    @Provides
    @NotificationHistoryFragmentScope
    fun providesAdapter(): NotificationHistoryAdapter {
        return NotificationHistoryAdapter()
    }
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class NotificationHistoryFragmentScope