package pl.gov.mf.etoll.base

import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import pl.gov.mf.etoll.MainNavgraphDirections
import pl.gov.mf.etoll.front.MainActivity
import pl.gov.mf.etoll.front.MainActivityComponent
import pl.gov.mf.etoll.front.bottomNavigation.ds.BottomNavigationState
import pl.gov.mf.etoll.front.critical.CriticalErrorFragment
import pl.gov.mf.etoll.front.topBars.TopBars
import pl.gov.mf.etoll.security.checker.SecuritySanityCheckerUseCase
import javax.inject.Inject

abstract class BaseMVVMFragment<VIEWMODEL : BaseViewModel, COMPONENT> :
    BaseFragment<MainActivityComponent>() {

    private var _component: COMPONENT? = null

    @Inject
    lateinit var validateAndroidComponentSigningUseCase: SecuritySanityCheckerUseCase.ValidateAndroidComponentSigningUseCase

    @Inject
    lateinit var signComponentUseCase: SecuritySanityCheckerUseCase.SignAndroidComponentUseCase

    @Suppress("UNCHECKED_CAST")
    internal val component: COMPONENT
        get() {
            if (_component == null)
                _component = createComponent()
            return _component as COMPONENT
        }

    @Inject
    lateinit var viewModel: VIEWMODEL

    /**
     * Create fragment component
     */
    protected abstract fun createComponent(): COMPONENT

    override fun onResume() {
        super.onResume()
        if (viewModel.shouldCheckSecuritySanityOnThisView() && (arguments == null
                    || !validateAndroidComponentSigningUseCase.execute(requireArguments()))
        ) {
            findNavController().navigate(
                MainNavgraphDirections.actionCriticalError(
                    CriticalErrorFragment.TYPE_SECURITY_ISSUE
                )
            )
        }

        activityComponent.changeBottomNavigationStateUseCase().execute(getBottomNavigationState())
        activityComponent.setStatusBarColorUseCase().execute(requireActivity(), getStatusBarColor())
        closeDrawer()
        if (shouldSecureThisFragment()) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        hideBlur()
    }

    protected fun lockBackPress(onBackPressedExtraAction: () -> Unit = {}) {
        requireActivity().onBackPressedDispatcher.addCallback(this, object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressedExtraAction()
            }
        })
    }

    protected fun minimizeOnBackPress() {
        lockBackPress { closeApp() }
    }

    //---
    //Set default color, but allow to customize it in each fragment
    open fun getStatusBarColor(): Int = TopBars.STATUS_BAR.defaultColor

    open fun getBottomNavigationState(): BottomNavigationState = BottomNavigationState(false)

    fun closeDrawer(): Boolean = (requireActivity() as MainActivity).closeDrawer()

    fun showLoading() = (requireActivity() as MainActivity).showLoading()

    fun hideLoading() = (requireActivity() as MainActivity).hideLoading()

    fun hideBlur() = (requireActivity() as MainActivity).hideBlur()

    open fun shouldSecureThisFragment(): Boolean = false

}