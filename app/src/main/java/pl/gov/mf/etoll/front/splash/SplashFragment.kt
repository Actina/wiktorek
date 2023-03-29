package pl.gov.mf.etoll.front.splash

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import pl.gov.mf.etoll.MainNavgraphDirections
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.databinding.FragmentSplashBinding
import pl.gov.mf.etoll.front.BaseDatabindingFragmentWithPermissionsSupport
import pl.gov.mf.etoll.front.MainActivity
import pl.gov.mf.etoll.front.critical.CriticalErrorFragment
import pl.gov.mf.etoll.front.splash.SplashFragmentViewModel.NavigationTarget
import pl.gov.mf.etoll.front.splash.SplashFragmentViewModel.NavigationTarget.*
import pl.gov.mf.etoll.front.splash.SplashFragmentViewModel.SplashMode
import pl.gov.mf.etoll.ui.components.dialogs.GenericOneButtonDialog

class SplashFragment :
    BaseDatabindingFragmentWithPermissionsSupport<SplashFragmentViewModel, StartupActivitySplashFragmentComponent>() {

    lateinit var binding: FragmentSplashBinding

    override fun createComponent(): StartupActivitySplashFragmentComponent =
        (context as MainActivity).component.plus(
            StartupActivitySplashFragmentModule(this, lifecycle)
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        component.inject(viewModel)

        lockBackPress()

        viewModel.splashState.observe(this) { mode ->
            Log.d(SplashFragment::class.simpleName, "State: ${mode.name}")
            when (mode) {
                SplashMode.SPLASH_IDLE -> Unit
                SplashMode.SPLASH_LOADING -> viewModel.loadSystems()
                SplashMode.SPLASH_SECURITY -> viewModel.checkSecurity()
                SplashMode.SPLASH_LOADING_ERROR -> showLoadingErrorDialog(
                    mode
                )

                SplashMode.SPLASH_SECURITY_ERROR -> {
                    // full screen critical error
                    val action =
                        MainNavgraphDirections.actionCriticalError(CriticalErrorFragment.TYPE_SECURITY_ISSUE)
                    signComponentUseCase.execute(action.arguments)
                    findNavController().navigate(action)
                    viewModel.resetNavigation()
                }

                SplashMode.SPLASH_DEVICE_COMPATIBILITY -> viewModel.checkCompatibility()
                SplashMode.SPLASH_DEVICE_COMPATIBILITY_ERROR -> {
                    // full screen critical error
                    val action =
                        MainNavgraphDirections.actionCriticalError(CriticalErrorFragment.TYPE_DEVICE_INCOMPATIBLE)
                    signComponentUseCase.execute(action.arguments)
                    findNavController().navigate(action)
                    viewModel.resetNavigation()
                }

                SplashMode.SPLASH_ALL_LOADED -> {
                    viewModel.notifyActivityStarted(requireActivity() as MainActivity)
                    viewModel.getNavigationTarget { target ->
                        findNavController().navigate(
                            when (target) {
                                LANGUAGE -> SplashFragmentDirections.actionShowLanguageSettingsV2(
                                    signComponentUseCase.executeCustomSign()
                                )

                                REGULATIONS -> SplashFragmentDirections.actionRegistrationRegulations(
                                    signComponentUseCase.executeCustomSign()
                                )

                                DRIVER_WARNING -> SplashFragmentDirections.actionDriverWarning(
                                    signComponentUseCase.executeCustomSign()
                                )

                                DASHBOARD -> SplashFragmentDirections.actionShowDashboard(
                                    signComponentUseCase.executeCustomSign()
                                )

                                BUSINESS_NUMBER -> SplashFragmentDirections.actionRegistered(
                                    signComponentUseCase.executeCustomSign()
                                )
                            }
                        )
                    }
                }

                SplashMode.SPLASH_CODE_ERROR -> {
                    // full screen critical error
                    val action =
                        MainNavgraphDirections.actionCriticalError(CriticalErrorFragment.TYPE_STARTUP_ISSUE)
                    signComponentUseCase.execute(action.arguments)
                    findNavController().navigate(action)
                    viewModel.resetNavigation()
                }

                SplashMode.SPLASH_ASK_OPTIMIZATION -> {
                    viewModel.resetNavigation()
                    askForBatteryOptimization()
                }

                SplashMode.SPLASH_ASK_PERMISSIONS -> {
                    viewModel.resetNavigation()
                    askForGpsPermissions()
                }

                else -> {
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_splash,
            container, false
        )
        return binding.root
    }

    private fun showLoadingErrorDialog(mode: SplashMode) =
        GenericOneButtonDialog.createAndShow(
            parentFragmentManager,
            "loading_dialog_title_android",
            "loading_dialog_error_loading_text_android",
            "dialog_retry_android"
        )?.observe(viewLifecycleOwner) {
            // continue regardless if it was disposed or clicked ok
            viewModel.retry(mode)
        }

    override fun onGpsPermissionsGranted() {
        viewModel.onGpsPermissionGranted()
    }

    override fun onBatteryOptimizationDisabled() {
        viewModel.onBatteryOptimizationDisabled()
    }

    override fun onBatteryOptimizationDenied() {
        // we should stop ride and continue
        viewModel.onUserRequestedRideStop()
    }

    override fun onGpsPermissionsDenied() {
        // we should stop ride and continue
        viewModel.onUserRequestedRideStop()
    }

    override fun getBatteryOptimizationRejectedDialogText(): Triple<String, String, String> =
        Triple(
            "battery_optimization_rejected_title_android",
            "battery_optimization_rejected_body_splash_android",
            "battery_optimization_rejected_button_android"
        )

    override fun getGpsPermissionsRejectedDialogText(): Triple<String, String, String> = Triple(
        "gps_permission_rejected_title_android",
        "gps_permission_rejected_body_splash_android",
        "gps_permission_rejected_button_android"
    )
}
