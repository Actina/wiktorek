package pl.gov.mf.etoll.front.bottomNavigation.ds

import io.reactivex.Observable

interface BottomNavigationDataSource {
    fun observeChanges(): Observable<BottomNavigationState>
    fun pushNewState(state: BottomNavigationState)
}