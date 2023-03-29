package pl.gov.mf.etoll.base

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import pl.gov.mf.etoll.app.NkspoApplicationImpl
import pl.gov.mf.etoll.app.di.ApplicationComponentImpl
import pl.gov.mf.etoll.core.criticalmessages.CriticalMessagesObserver
import pl.gov.mf.etoll.core.watchdog.CoreWatchdogUC.AppGoesToBackgroundUseCaseWatchdog
import pl.gov.mf.etoll.core.watchdog.CoreWatchdogUC.AppGoesToForegroundUseCase
import pl.gov.mf.etoll.core.watchdog.ridefinish.RideFinishCallbacks
import pl.gov.mf.etoll.overlay.OverlayService
import pl.gov.mf.mobile.utils.disposeSafe
import javax.inject.Inject

abstract class BaseActivity<VIEWMODEL : BaseDatabindingViewModel, COMPONENT : BaseComponent<VIEWMODEL>> :
    CriticalMessagesObserver, RideFinishCallbacks, AppCompatActivity() {

    protected val applicationComponent: ApplicationComponentImpl
        get() = (applicationContext as NkspoApplicationImpl).component

    private var _component: COMPONENT? = null

    protected var compositeDisposable: CompositeDisposable? = null

    @Inject
    lateinit var viewModel: VIEWMODEL

    @Inject
    lateinit var appGoesToForegroundUseCase: AppGoesToForegroundUseCase

    @Inject
    lateinit var appGoesToBackgroundBackgroundUseCase: AppGoesToBackgroundUseCaseWatchdog

    @Suppress("UNCHECKED_CAST")
    internal val component: COMPONENT
        get() {
            if (_component == null)
                _component = createComponent()
            return _component as COMPONENT
        }

    /**
     * Create activity component
     */
    protected abstract fun createComponent(): COMPONENT

    override fun onStart() {
        super.onStart()
        compositeDisposable = CompositeDisposable()
        appGoesToForegroundUseCase.execute(
            this,
            this,
            this,
            getOverlayIntent()
        )
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.disposeSafe()
        compositeDisposable = null

        appGoesToBackgroundBackgroundUseCase.execute(
            this,
            getOverlayIntent()
        )
    }

    fun getOverlayIntent() = Intent(applicationContext, OverlayService::class.java)
}