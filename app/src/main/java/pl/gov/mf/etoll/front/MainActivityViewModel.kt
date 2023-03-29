package pl.gov.mf.etoll.front

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pl.gov.mf.etoll.appmode.AppMode
import pl.gov.mf.etoll.appmode.AppModeManagerUC
import pl.gov.mf.etoll.base.BaseDatabindingViewModel
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorUC
import pl.gov.mf.etoll.front.bottomNavigation.ds.BottomNavigationState
import pl.gov.mf.etoll.front.bottomNavigation.ds.BottomNavigationUC
import pl.gov.mf.etoll.storage.settings.SettingsUC
import pl.gov.mf.mobile.utils.addSafe
import javax.inject.Inject

class MainActivityViewModel : BaseDatabindingViewModel() {

    @Inject
    lateinit var readSettingsUseCase: SettingsUC.ReadSettingsUseCase

    @Inject
    lateinit var observeBottomNavigationChangesUseCase: BottomNavigationUC.ObserveBottomNavigationChangesUseCase

    @Inject
    lateinit var checkIfRideIsInProgressUseCase: RideCoordinatorUC.CheckIfRideIsInProgressUseCase

    @Inject
    lateinit var setCurrentAppModeUseCase: AppModeManagerUC.SetCurrentAppModeUseCase

    private val _currentBottomNavigationState = MutableLiveData<BottomNavigationState>()
    val currentBottomNavigationState: LiveData<BottomNavigationState>
        get() = _currentBottomNavigationState

    override fun onResume() {
        super.onResume()
        compositeDisposable.addSafe(
            observeBottomNavigationChangesUseCase.execute().subscribe { state ->
                if (currentBottomNavigationState.value == null || !currentBottomNavigationState.equals(
                        state
                    )
                )
                    _currentBottomNavigationState.postValue(state)
            })
    }

    fun enableLightMode() {
        compositeDisposable.addSafe(setCurrentAppModeUseCase.execute(AppMode.LIGHT_MODE, false)
            .subscribe {
            })
    }

    fun enableDarkMode() {
        compositeDisposable.addSafe(setCurrentAppModeUseCase.execute(AppMode.DARK_MODE, false)
            .subscribe {
            })
    }
}