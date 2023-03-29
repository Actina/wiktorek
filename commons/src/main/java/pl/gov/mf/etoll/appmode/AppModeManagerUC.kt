package pl.gov.mf.etoll.appmode

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

sealed class AppModeManagerUC {
    class GetCurrentAppModeUseCase(private val ds: AppModeManager) {

        fun executeForConfigurationValue() = ds.getCurrentAppConfMode()

        fun execute() = ds.getCurrentAppMode()

        fun executeObservation(): Observable<AppMode> = ds.observeModeChanges()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    class SetCurrentAppModeUseCase(private val ds: AppModeManager) {
        fun execute(appMode: AppMode, followSystem: Boolean) = ds.setAppMode(appMode, followSystem)
    }
}