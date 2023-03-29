package pl.gov.mf.etoll.front.bottomNavigation.ds

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

class BottomNavigationRepository @Inject constructor() : BottomNavigationDataSource {

    private val ds: BehaviorSubject<BottomNavigationState> = BehaviorSubject.createDefault(
        BottomNavigationState(
            false,
            BottomNavigationSelectedSection.CENTER
        )
    )

    override fun observeChanges(): Observable<BottomNavigationState> = ds

    override fun pushNewState(state: BottomNavigationState) = ds.onNext(state)

}