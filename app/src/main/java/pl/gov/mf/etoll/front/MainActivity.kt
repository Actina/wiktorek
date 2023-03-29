package pl.gov.mf.etoll.front

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.*
import org.joda.time.DateTime
import pl.gov.mf.etoll.MainNavgraphDirections
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.base.BaseActivity
import pl.gov.mf.etoll.core.crmmessages.CrmMessagesManager
import pl.gov.mf.etoll.core.inactivity.InactivityController
import pl.gov.mf.etoll.core.inactivity.InactivityState
import pl.gov.mf.etoll.core.model.CoreMessage
import pl.gov.mf.etoll.core.watchdog.fakegps.FakeGpsCollector
import pl.gov.mf.etoll.core.watchdog.ridefinish.RideFinishController
import pl.gov.mf.etoll.databinding.ActivityMainBinding
import pl.gov.mf.etoll.front.bottomNavigation.view.BottomNavigationAction.*
import pl.gov.mf.etoll.front.bottomNavigation.view.BottomNavigationView
import pl.gov.mf.etoll.front.bottomNavigation.view.BottomNavigationViewObserver
import pl.gov.mf.etoll.front.critical.CriticalErrorFragment
import pl.gov.mf.etoll.front.errors.gpsissues.GpsIssuesModel
import pl.gov.mf.etoll.interfaces.CriticalMessageState
import pl.gov.mf.etoll.security.checker.SecuritySanityCheckerUseCase
import pl.gov.mf.etoll.ui.components.dialogs.CriticalMessageDialogFragment
import pl.gov.mf.etoll.ui.components.dialogs.FakeGpsDetectedDialogFragment
import pl.gov.mf.etoll.ui.components.dialogs.RideCantBeContinuedDialogFragment
import pl.gov.mf.etoll.ui.components.dialogs.crm.CrmDialogFragment
import pl.gov.mf.etoll.utils.navigate
import pl.gov.mf.etoll.utils.popBackStack
import pl.gov.mf.mobile.ui.components.dialogs.DialogsHelper
import pl.gov.mf.mobile.ui.components.utils.BlurDelegate
import pl.gov.mf.mobile.utils.addSafe
import pl.gov.mf.mobile.utils.toDp
import javax.inject.Inject


@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
class MainActivity : BaseActivity<MainActivityViewModel, MainActivityComponent>(),
    BlurDelegate, CrmMessagesManager.Callbacks, BottomNavigationViewObserver {

    companion object {
        private val fragmentsWhenNotificationsAndMessagesCanBeShown = arrayOf(
            R.id.dashboardFragment,
            R.id.rideHistoryFragment,
            R.id.rideDetailsFragment,
            R.id.notificationHistoryFragment,
            R.id.helpFragment,
            R.id.aboutFragment,
            R.id.appSettingsFragment,
            R.id.registeredFragment,
            R.id.securityConfigPinFragment,
            R.id.tecsAmountSelectionFragment
        )

        private val fragmentsWithNoCriticalMsg = arrayOf(
            R.id.splashFragment,
            R.id.appLanguageSettingsV2
        )
        private const val TAG = "mainAct"
    }

    @Inject
    lateinit var signAndroidComponentUseCase: SecuritySanityCheckerUseCase.SignAndroidComponentUseCase

    @Inject
    lateinit var dialogsHelper: DialogsHelper

    @Inject
    lateinit var crmMessagesManager: CrmMessagesManager

    @Inject
    lateinit var rideFinishController: RideFinishController

    @Inject
    lateinit var inactivityController: InactivityController

    @Inject
    lateinit var fakeGpsCollector: FakeGpsCollector

    lateinit var bindings: ActivityMainBinding

    private lateinit var customBottomNavigation: BottomNavigationView
    private lateinit var loadingOverlay: LinearLayout
    private lateinit var mainLoadingArea: ConstraintLayout
    private lateinit var startupNavHostFragment: FragmentContainerView

    private val navigationJob: Job = SupervisorJob()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("NKSPO_APP", "STARTING ACTIVITY! ${DateTime.now()}")
        // wake lock for screen
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        bindings = DataBindingUtil.setContentView(
            this, R.layout.activity_main
        )
        component.inject(this)
        component.inject(viewModel)
        setupView()

        customBottomNavigation.initialize(this, startupNavHostFragment)
        lifecycle.addObserver(customBottomNavigation)
        viewModel.currentBottomNavigationState.observe(this) { state ->
            customBottomNavigation.onNewState(state)
        }

        viewModel.invalidateBindings.observe(this) {
            bindings.customBottomNavigation.invalidateBottomMenuInMode()
            bindings.invalidateAll()
        }

        customBottomNavigation.observeNavigationActions().observe(this) { action ->
            // exit condition
            if (action == NONE) return@observe
            // consume event
            when (action) {
                ACTIVITY -> {
                    if (findNavController().currentDestination!!.id != R.id.notificationHistoryFragment)
                        findNavController().navigate(lifecycleScope, navigationJob,
                            MainNavgraphDirections.actionShowNotificationHistory(
                                signAndroidComponentUseCase.executeCustomSign()
                            )
                        )
                }
                RIDE -> {
                    if (findNavController().currentDestination!!.id != R.id.dashboardFragment) {
                        findNavController().popBackStack(lifecycleScope, navigationJob,
                            R.id.dashboardFragment, false)
                    }
                }
                HELP -> {
                    val destination =
                        MainNavgraphDirections.actionShowHelp(signAndroidComponentUseCase.executeCustomSign())
                    findNavController().navigate(lifecycleScope, navigationJob, destination)
                }
                ABOUT -> {
                    val destination =
                        MainNavgraphDirections.actionShowAbout(signAndroidComponentUseCase.executeCustomSign())
                    findNavController().navigate(lifecycleScope, navigationJob, destination)
                }
                SETTINGS -> {

                    val destination =
                        MainNavgraphDirections.actionShowSettings(signAndroidComponentUseCase.executeCustomSign())
                    findNavController().navigate(lifecycleScope, navigationJob, destination)
                }
                SECURITY_SETTINGS -> {
                    val destination =
                        MainNavgraphDirections.actionShowSecuritySettings(
                            signAndroidComponentUseCase.executeCustomSign()
                        )
                    findNavController().navigate(lifecycleScope, navigationJob, destination)
                }
                RIDES_HISTORY -> {
                    findNavController().navigate(lifecycleScope, navigationJob,
                        MainNavgraphDirections.actionShowRideHistoryFragment(
                            signAndroidComponentUseCase.executeCustomSign()
                        )
                    )
                }
                else -> {
                }
            }

        }

        mainLoadingArea.setOnClickListener {
            // catch clicks
        }

//        //TODO: why this listener doesn't work as binding in activity layout?
//        enable_light_mode.setOnClickListener {
//            viewModel.enableLightMode()
//        }
//
//        enable_dark_mode.setOnClickListener {
//            viewModel.enableDarkMode()
//        }

        findNavController().addOnDestinationChangedListener { _, _, _ ->
            if (!shouldCheckInactivity()) return@addOnDestinationChangedListener

            compositeDisposable.addSafe(
                inactivityController.onCheckRequested()
                    .subscribe(
                        ::handleInactivityStateResult, {}
                    )
            )
        }
    }

    private fun setupView() {
        customBottomNavigation = findViewById(R.id.customBottomNavigation)
        loadingOverlay = findViewById(R.id.loading_overlay)
        mainLoadingArea = findViewById(R.id.main_loading_area)
        startupNavHostFragment = findViewById(R.id.startup_NavHostFragment)
    }

    fun showLoading() {
        showBlur()
        loadingOverlay.visibility = View.VISIBLE
    }

    fun hideLoading() {
        hideBlur()
    }


    override fun showBlur() {
        mainLoadingArea.visibility = View.VISIBLE
        loadingOverlay.visibility = View.INVISIBLE
    }

    override fun hideBlur() {
        mainLoadingArea.visibility = View.GONE
    }

    override fun onMenuIsVisible() {
        // show padding in content
        startupNavHostFragment.setPadding(
            startupNavHostFragment.paddingLeft,
            startupNavHostFragment.paddingTop,
            startupNavHostFragment.paddingRight,
            48.toDp(resources).toInt()
        )
    }

    override fun onMenuIsHidden() {
        // remove padding in content
        startupNavHostFragment.setPadding(
            startupNavHostFragment.paddingLeft,
            startupNavHostFragment.paddingTop,
            startupNavHostFragment.paddingRight,
            0
        )
    }

    fun closeDrawer(): Boolean {
        if (customBottomNavigation.isMenuOpened()) {
            customBottomNavigation.closeDrawer()
            return true
        }
        return false
    }

    override fun createComponent(): MainActivityComponent =
        applicationComponent.plus(MainActivityModule(this, lifecycle))

    private fun findNavController(): NavController {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.startup_NavHostFragment) as NavHostFragment
        return navHostFragment.navController
    }

    override fun showCriticalMessageDialog(criticalMessageType: CriticalMessageState): Boolean {
        if (isShowingNextCriticalMessageBlocked()) return false
        // in other cases, we show it as dialog
        CriticalMessageDialogFragment.createAndShow(
            supportFragmentManager,
            criticalMessageType
        )
        return true
    }

    private fun isShowingNextCriticalMessageBlocked(): Boolean {
        if (!fragmentsWhenNotificationsAndMessagesCanBeShown.contains(findNavController().currentDestination?.id)
            || isAnyImportantMessageCurrentlyShown()
        )
            return true
        val currentlyInRide =
            applicationComponent.rideCoordinator().getConfiguration()?.duringRide ?: false
        return !currentlyInRide
    }

    override fun blockAppNoGpsPermissions() {
        if (findNavController().currentDestination?.id in fragmentsWithNoCriticalMsg)
            return
        if (findNavController().currentDestination?.id != R.id.criticalErrorFragment)
            Toast.makeText(this, "Nie zaimplementowano", Toast.LENGTH_SHORT).show()
//            findNavController().navigateOnMainThread(
//                MainNavgraphDirections.actionCriticalError(
//                    CriticalErrorFragment.TYPE_NO_GPS_PERMISSION
//                )
//            )

    }

    override fun blockAppNoGpsAccessible() {
        if (findNavController().currentDestination?.id in fragmentsWithNoCriticalMsg)
            return
        if (findNavController().currentDestination?.id != R.id.criticalErrorFragment)
            findNavController().navigate(
                lifecycleScope, navigationJob,
                MainNavgraphDirections.actionCriticalError(
                    CriticalErrorFragment.TYPE_GPS_DISABLED
                )
            )
    }

    override fun blockAppAirplaneModeIsOn() {
        if (findNavController().currentDestination?.id in fragmentsWithNoCriticalMsg)
            return
        if (findNavController().currentDestination?.id != R.id.criticalErrorFragment)
            findNavController().navigate(
                lifecycleScope, navigationJob,
                MainNavgraphDirections.actionCriticalError(
                    CriticalErrorFragment.TYPE_AIRPLANE_MODE
                )
            )
    }

    private fun showLogin() {
        val navController = findNavController()

        if (navController.currentDestination?.id == R.id.splashFragment ||
            navController.currentDestination?.id == R.id.criticalErrorFragment ||
            navController.currentDestination?.id == R.id.securityResetPinCodeToUnlockFragment ||
            navController.currentDestination?.id == R.id.securityUnlockWithPinCodeFragment
        ) return

        navController.navigate(lifecycleScope, navigationJob,
            MainNavgraphDirections.actionShowUnlockWithPinCode(
                signAndroidComponentUseCase.executeCustomSign()
            )
        )
    }

    private fun showSecurityReset() {
        val navController = findNavController()

        if (navController.currentDestination?.id == R.id.splashFragment ||
            navController.currentDestination?.id == R.id.criticalErrorFragment ||
            navController.currentDestination?.id == R.id.securityResetPinCodeToUnlockFragment
        ) return

        navController.navigate(lifecycleScope, navigationJob,
            MainNavgraphDirections.actionShowResetPinCodeToUnlock(
                signAndroidComponentUseCase.executeCustomSign()
            )
        )
    }


    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK && customBottomNavigation.isMenuOpened()) {
            customBottomNavigation.closeDrawer()
            true
        } else
            super.onKeyUp(keyCode, event)
    }

    override fun onResume() {
        super.onResume()
        crmMessagesManager.setCallbacks(this, TAG)
        inactivityController.onAppGoesToForeground()

        if (!shouldCheckInactivity()) return

        compositeDisposable.addSafe(
            inactivityController.onCheckRequested()
                .subscribe(
                    ::handleInactivityStateResult, {}
                )
        )
        customBottomNavigation.invalidateView()
    }

    override fun onPause() {
        super.onPause()
        crmMessagesManager.removeCallbacks(TAG)
        inactivityController.onAppGoesToBackground()
        navigationJob.cancelChildren()
    }

    @SuppressLint("CheckResult")
    override fun onNewMessageToShow(message: CoreMessage) {
        // check if we're in fragment that would allow us to show CRM messages
        if (fragmentsWhenNotificationsAndMessagesCanBeShown.contains(findNavController().currentDestination?.id) && !isAnyImportantMessageCurrentlyShown()) {
            // if no other dialog is shown, then show ours
            // this function has own flow control, so no need to check if dialog is not shown
            CrmDialogFragment.create(message.toDialogModel(), supportFragmentManager)
                ?.subscribe({
                    crmMessagesManager.confirmMessageWasSeen(message.messageId)
                }, {})
        }
    }

    override fun showTimeIssues(duringRide: Boolean) {
        if (fragmentsWhenNotificationsAndMessagesCanBeShown.contains(findNavController().currentDestination?.id) && !isAnyImportantMessageCurrentlyShown()) {
            findNavController().navigate(lifecycleScope, navigationJob,
                MainNavgraphDirections.actionShowTimeIssues(
                    signAndroidComponentUseCase.executeCustomSign(), duringRide
                )
            )
        }
    }

    override fun showInstanceIssuesError(): Boolean {
        // if we're on any screen related to ride (topup, ride details etc) we should cancel and go back to dashboard
        val fragmentsWhichShouldNotBeRenavigated = arrayOf(
            R.id.dashboardFragment,
            R.id.splashFragment,
            R.id.driverWarningFragment,
            R.id.registeredFragment,
            R.id.rideSummaryFragment
        ) // TODO: add other fragments here
        if (!fragmentsWhichShouldNotBeRenavigated.contains(findNavController().currentDestination?.id) && !isAnyImportantMessageCurrentlyShown()) {
            // navigate to dashboard
            findNavController().navigate(lifecycleScope, navigationJob,
                MainNavgraphDirections.actionShowDashboard(
                    signAndroidComponentUseCase.executeCustomSign()
                )
            )
            // now return to give 1s chance to finish navigation
            return false
        }
        // case for registration
        if (findNavController().currentDestination?.id == R.id.registeredFragment) {
            // show fragment without option to go back
            findNavController().navigate(lifecycleScope, navigationJob,
                MainNavgraphDirections.actionShowInstanceIssues(
                    signAndroidComponentUseCase.executeCustomSign(), false
                )
            )
            return true
        }

        // show fragment with option to go back
        findNavController().navigate(lifecycleScope, navigationJob,
            MainNavgraphDirections.actionShowInstanceIssues(
                signAndroidComponentUseCase.executeCustomSign(), true
            )
        )

        return true
    }

    override fun blockAppVersionTooOld() {
        findNavController().currentDestination?.let { currentDestination ->
            if (currentDestination.id != R.id.criticalErrorFragment
                && currentDestination.id != R.id.splashFragment
            ) {
                findNavController().navigate(lifecycleScope, navigationJob,
                    MainNavgraphDirections.actionCriticalError(
                        CriticalErrorFragment.TYPE_APP_OUTDATED
                    )
                )
            }
        }
    }

    override fun showGpsIssuesDialog(issues: List<Int>): Boolean =
        if (fragmentsWhenNotificationsAndMessagesCanBeShown.contains(findNavController().currentDestination?.id) && !isAnyImportantMessageCurrentlyShown()) {
            findNavController().navigate(lifecycleScope, navigationJob,
                MainNavgraphDirections.actionShowGpsIssues(
                    signAndroidComponentUseCase.executeCustomSign(),
                    GpsIssuesModel(issues).toJSON()
                )
            )
            true
        } else false


    override fun showFakeGpsDetectedDialog(): Boolean =
        if (fragmentsWhenNotificationsAndMessagesCanBeShown.contains(findNavController().currentDestination?.id) && !isAnyImportantMessageCurrentlyShown()) {
            Completable.create {
                FakeGpsDetectedDialogFragment.createAndShow(
                    supportFragmentManager
                )?.observe(this) {
                    findNavController().navigate(lifecycleScope, navigationJob,
                        MainNavgraphDirections.actionShowRideSummary(
                            signAndroidComponentUseCase.executeCustomSign()
                        )
                    )
                }
            }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe()
            true
        } else false

    override fun onShowRideFinishedRequested() {
        if (isSummaryInStack()) return
        if (!isAnyImportantMessageCurrentlyShown() && fakeGpsCollector.shouldFakeGpsDialogBeShown()) {
            showFakeGpsDetectedDialog()
        } else
            if (!isAnyImportantMessageCurrentlyShown() && inPossibleForRideFinishFragment()) {
                findNavController().navigate(lifecycleScope, navigationJob,
                    MainNavgraphDirections.actionShowRideSummary(
                        signAndroidComponentUseCase.executeCustomSign()
                    )
                )
            }
    }

    private fun isSummaryInStack(): Boolean =
        try {
            findNavController().getBackStackEntry(R.id.rideSummaryFragment)
            true
        } catch (e: Exception) {
            false
        }

    private fun inPossibleForRideFinishFragment(): Boolean {
        val location = findNavController().currentDestination?.id ?: -1
        // put here all fragments for which we allow showing ride finishing dialog - so this excludes tecs transaction, security, startup etc.
        return location == R.id.dashboardFragment || location == R.id.aboutFragment || location == R.id.rideDetailsFragment || location == R.id.rideDataFragment || location == R.id.configVehicleSelectionFragment
                || location == R.id.sentRidesSelectionFragment || location == R.id.configMonitoringDeviceFragment || location == R.id.configTrailerCategoryFragment || location == R.id.sentRideDetailsFragment
                || location == R.id.notificationHistoryFragment || location == R.id.rideHistoryFragment || location == R.id.demoFragment || location == R.id.helpFragment || location == R.id.appSecuritySettingsFragment
                || location == R.id.rideDetailsSentSelectionFragment || location == R.id.rideDetailsMapFragment
    }

    private fun isAnyImportantMessageCurrentlyShown(): Boolean {
        val fm =
            supportFragmentManager.fragments[0]?.childFragmentManager?.fragments?.get(0)?.childFragmentManager
                ?: supportFragmentManager
        return (supportFragmentManager.findFragmentByTag(CrmDialogFragment.TAG) != null ||
                supportFragmentManager.findFragmentByTag(FakeGpsDetectedDialogFragment.TAG) != null ||
                supportFragmentManager.findFragmentByTag(CriticalMessageDialogFragment.TAG) != null ||
                supportFragmentManager.findFragmentByTag(RideCantBeContinuedDialogFragment.TAG) != null ||
                fm.findFragmentByTag(CrmDialogFragment.TAG) != null ||
                fm.findFragmentByTag(FakeGpsDetectedDialogFragment.TAG) != null ||
                fm.findFragmentByTag(CriticalMessageDialogFragment.TAG) != null
                || fm.findFragmentByTag(RideCantBeContinuedDialogFragment.TAG) != null)
    }


    private fun shouldCheckInactivity(): Boolean {
        val navController = findNavController()

        return navController.currentDestination?.id != R.id.splashFragment &&
                navController.currentDestination?.id != R.id.criticalErrorFragment
    }

    private fun handleInactivityStateResult(inactivityState: InactivityState) {
        when (inactivityState) {
            InactivityState.UNLOCKED -> Unit
            InactivityState.SHOW_PIN_RESET -> showSecurityReset()
            InactivityState.SHOW_PIN_LOGIN -> showLogin()
        }
    }
}

private fun CoreMessage.toDialogModel(): CrmDialogFragment.CrmMessageModel =
    CrmDialogFragment.CrmMessageModel(
        contents,
        headers,
        sendDateTimestamp,
        CrmDialogFragment.MessageType.from(type),
        apiMessageId = messageId
    )