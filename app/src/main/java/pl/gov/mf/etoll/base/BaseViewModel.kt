package pl.gov.mf.etoll.base

import androidx.lifecycle.*
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import pl.gov.mf.etoll.appmode.AppMode
import pl.gov.mf.etoll.appmode.AppModeManagerUC
import pl.gov.mf.etoll.security.checker.SecuritySanityCheckerUseCase
import pl.gov.mf.mobile.utils.addSafe
import pl.gov.mf.mobile.utils.disposeSafe
import javax.inject.Inject

abstract class BaseViewModel : ViewModel(), LifecycleObserver {

    protected var compositeDisposable: CompositeDisposable? = null
    val executionJob: Job = SupervisorJob()

    //TODO: remove?
    @Inject
    lateinit var validateAndroidComponentSigningUseCase: SecuritySanityCheckerUseCase.ValidateAndroidComponentSigningUseCase

    @Inject
    lateinit var getCurrentAppModeUseCase: AppModeManagerUC.GetCurrentAppModeUseCase

    protected val _appMode = MutableLiveData(AppModeWrapper())
    val appMode: LiveData<AppModeWrapper>
        get() = _appMode

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    open fun onPause() {
        compositeDisposable.disposeSafe()
        compositeDisposable = null
        executionJob.cancelChildren()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    open fun onResume() {
        compositeDisposable = CompositeDisposable()
        //TODO: etoll2 check if should be removed
        compositeDisposable.addSafe(getCurrentAppModeUseCase.executeObservation().subscribe {
            _appMode.value = AppModeWrapper(it)
        })
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    open fun onCreate() {
        getCurrentAppModeUseCase.execute().also {
            _appMode.value = AppModeWrapper(it)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    open fun onStart() {

    }

    open fun shouldCheckSecuritySanityOnThisView(): Boolean = true


    fun onMain(block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.Main + executionJob) { block() }
    }

    fun onIO(block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO + executionJob) { block() }
    }

    inner class AppModeWrapper(val appMode: AppMode? = null)
}