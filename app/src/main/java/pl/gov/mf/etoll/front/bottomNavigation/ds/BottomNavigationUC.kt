package pl.gov.mf.etoll.front.bottomNavigation.ds

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

sealed class BottomNavigationUC {
    class ObserveBottomNavigationChangesUseCase(private val ds: BottomNavigationDataSource) :
        BottomNavigationUC() {
        fun execute() = ds.observeChanges().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    class RequestBottomNavigationStateUseCase(private val ds: BottomNavigationDataSource) :
        BottomNavigationUC() {
        fun execute(requested: BottomNavigationState) = ds.pushNewState(requested)
    }
}