package pl.gov.mf.etoll.front.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.core.watchdog.ridefinish.RideFinishController
import pl.gov.mf.etoll.databinding.FragmentDashboardBinding
import pl.gov.mf.etoll.front.BaseDatabindingFragmentWithPermissionsSupport
import pl.gov.mf.etoll.front.bottomNavigation.ds.BottomNavigationSelectedSection
import pl.gov.mf.etoll.front.bottomNavigation.ds.BottomNavigationState
import pl.gov.mf.etoll.interfaces.BatteryStateInterfaceController
import pl.gov.mf.etoll.interfaces.DataSenderStateInterfaceController
import pl.gov.mf.etoll.interfaces.GpsStateInterfaceController
import pl.gov.mf.etoll.ui.components.dialogs.*
import pl.gov.mf.mobile.ui.components.dialogs.basic.BasicOneActionDialogFragment
import pl.gov.mf.mobile.ui.components.dialogs.basic.BasicTwoActionsDialogFragment
import pl.gov.mf.mobile.ui.components.views.DashboardStatusIconBattery
import pl.gov.mf.mobile.ui.components.views.DashboardStatusIconGps
import pl.gov.mf.mobile.ui.components.views.DashboardStatusIconInternet
import pl.gov.mf.mobile.ui.components.views.DataSyncStatusView
import javax.inject.Inject

class DashboardFragment :
    BaseDatabindingFragmentWithPermissionsSupport<DashboardViewModel, DashboardFragmentComponent>() {

    companion object {
        private const val CALLBACKS_TAG = "dashboard"
    }

    @Inject
    lateinit var batteryStateInterfaceController: BatteryStateInterfaceController

    @Inject
    lateinit var gpsStateInterfaceController: GpsStateInterfaceController

    @Inject
    lateinit var dataSenderStateInterfaceController: DataSenderStateInterfaceController

    @Inject
    lateinit var rideFinishController: RideFinishController

    @Inject
    lateinit var dashboardFlavourDelegate: DashboardFlavourDelegate

    private lateinit var binding: FragmentDashboardBinding

    private lateinit var dashboardTimerStartButton: MaterialButton
    private lateinit var dashboardTimerCancelButton: MaterialButton
    private lateinit var dashboardTimerStopButton: MaterialButton
    private lateinit var dashboardSyncToast: DataSyncStatusView
    private lateinit var dashboardIconBattery: DashboardStatusIconBattery
    private lateinit var dashboardIconInternet: DashboardStatusIconInternet
    private lateinit var dashboardIconGps: DashboardStatusIconGps

    override fun createComponent(): DashboardFragmentComponent =
        activityComponent.plus(DashboardFragmentModule(this, lifecycle))

    override fun getBottomNavigationState(): BottomNavigationState =
        BottomNavigationState(true, BottomNavigationSelectedSection.CENTER)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        component.inject(viewModel)
        minimizeOnBackPress()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_dashboard,
            container, false
        )
        setupView(binding.root)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    private fun setupView(view: View) {
        dashboardTimerStartButton = view.findViewById(R.id.dashboard_timer_startButton)
        dashboardTimerCancelButton = view.findViewById(R.id.dashboard_timer_cancelButton)
        dashboardTimerStopButton = view.findViewById(R.id.dashboard_timer_stopButton)
        dashboardSyncToast = view.findViewById(R.id.dashboard_sync_toast)
        dashboardIconBattery = view.findViewById(R.id.dashboard_icon_battery)
        dashboardIconInternet = view.findViewById(R.id.dashboard_icon_internet)
        dashboardIconGps = view.findViewById(R.id.dashboard_icon_gps)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<MaterialButton>(R.id.dashboard_configuration_bottom_button)
            .setOnClickListener {
                // call network to grab newest status and sent, in case of error - do not cancel
                viewModel.tryToUpdateStatusAndSent()
            }

        view.findViewById<View>(R.id.dashboard_ride_details_area).setOnClickListener {
            viewModel.onRideDetailsClicked()
        }

        viewModel.navigationTarget.observe(viewLifecycleOwner) { target ->
            when (target) {
                NavigationTarget.NONE -> {
                    // do nothing
                }
                NavigationTarget.CONFIGURATION_START -> {
                    viewModel.resetNavigationTarget()
                    findNavController().navigate(
                        DashboardFragmentDirections.actionShowRideTypeSelection(
                            signComponentUseCase.executeCustomSign()
                        )
                    )
                }
                NavigationTarget.RIDE_DETAILS -> {
                    viewModel.resetNavigationTarget()
                    findNavController().navigate(
                        DashboardFragmentDirections.actionShowRideDetails(
                            signComponentUseCase.executeCustomSign()
                        )
                    )
                }
                NavigationTarget.NO_CONFIGURATION_POSSIBLE -> {
                    viewModel.resetNavigationTarget()
                    // no configuration is possible to continue, edge case
                    NoConditionsToStartConfigurationFragment.createAndShow(childFragmentManager)
                    // TODO: here should be message about issue to user with selected level of details about error source
                }
                NavigationTarget.RIDE_CANT_BE_CONTINUED -> {
                    viewModel.resetNavigationTarget()
                    // first of all, we should finish current ride
                    dashboardTimerStartButton.isEnabled = false
                    dashboardTimerCancelButton.isEnabled = false
                    dashboardTimerStopButton.isEnabled = false
                    viewModel.stopRide()
                    // we can't proceed with ride, we should show message and end ride
                    RideCantBeContinuedDialogFragment.createAndShow(childFragmentManager)?.observe(
                        viewLifecycleOwner
                    ) {
                        // ping watchdog to show summary screen immidiatelly after this one gets closed
                        rideFinishController.intervalCheck()
                    }
                    // we do not reset nav target here
                }
                NavigationTarget.RIDE_CANT_BE_STARTED_GPS -> {
                    viewModel.resetNavigationTarget()
                    // we can't proceed with ride, we should show message and end ride
                    RideCantBeStartedDialogFragment.createAndShow(childFragmentManager)?.observe(
                        viewLifecycleOwner
                    ) {
                        // finish ride
                        dashboardTimerStartButton.isEnabled = false
                        dashboardTimerCancelButton.isEnabled = false
                        dashboardTimerStopButton.isEnabled = false
                    }
                }
                NavigationTarget.CONTINUE_SENT_CONFIGURATION -> {
                    viewModel.resetNavigationTarget()
                    SentConfigurationContinuationDialogFragment.createAndShow(
                        childFragmentManager,
                        false
                    )
                        ?.observe(viewLifecycleOwner) {
                            if (it == BasicOneActionDialogFragment.DialogResult.CONFIRMED) {
                                viewModel.prepareConfigurationForResumeOnSent()
                                findNavController().navigate(
                                    DashboardFragmentDirections.actionShowSentRidesSelection(
                                        signComponentUseCase.executeCustomSign()
                                    )
                                )
                            }
                        }
                }
                NavigationTarget.CONTINUE_SENT_CONFIGURATION_NO_SENT -> {
                    viewModel.resetNavigationTarget()
                    SentConfigurationContinuationDialogFragment.createAndShow(
                        childFragmentManager,
                        true
                    )
                        ?.observe(viewLifecycleOwner) {
                            // do nothing
                        }
                }
                NavigationTarget.ACCOUNT_TOP_UP -> {
                    viewModel.resetNavigationTarget()
                    findNavController().navigate(
                        DashboardFragmentDirections.actionTopUpAccount(
                            signComponentUseCase.executeCustomSign()
                        )
                    )
                }
                NavigationTarget.ACCOUNT_TOP_UP_NOT_INITIALIZED -> {
                    viewModel.resetNavigationTarget()
                    BillingAccountNotInitializedDialogFragment.createAndShow(childFragmentManager)
                        ?.observe(viewLifecycleOwner, {
                            // do nothing
                        })
                }
                NavigationTarget.RESET_LOADER -> {
                    viewModel.resetNavigationTarget()
                    hideLoading()
                    dashboardTimerStartButton.isEnabled = true
                    dashboardTimerCancelButton.isEnabled = true
                    dashboardTimerStopButton.isEnabled = true
                }
                else -> {
                    // fallback for null
                    viewModel.resetNavigationTarget()
                }
            }
        }
        viewModel.startingRideConfiguration.observe(viewLifecycleOwner) { starting ->
            if (starting)
                showLoading()
            else
                hideLoading()
        }
        dashboardTimerCancelButton.setOnClickListener {
            CancelRideConfigurationDialogFragment.createAndShow(childFragmentManager)
                ?.observe(viewLifecycleOwner) { output ->
                    if (output == BasicTwoActionsDialogFragment.DialogResult.CONFIRMED) {
                        viewModel.dropConfiguration()
                    }
                }
        }

        dashboardTimerStartButton.setOnClickListener {
            dashboardTimerStartButton.isEnabled = false
            dashboardTimerCancelButton.isEnabled = false
            dashboardTimerStopButton.isEnabled = false
            if (checkAndShowNoGpsDialog()) return@setOnClickListener
            askForBatteryOptimization()
        }
        dashboardTimerStopButton.setOnClickListener {
            if (!dashboardTimerStopButton.isEnabled) return@setOnClickListener

            dashboardTimerStopButton.isEnabled = false
            ConfirmRideFinishDialogFragment.createAndShow(
                childFragmentManager,
                viewModel.isTolled(),
                viewModel.isSent()
            )?.observe(
                viewLifecycleOwner
            ) { data ->
                if (data == BasicTwoActionsDialogFragment.DialogResult.CONFIRMED) {
                    it.isEnabled = false
                    dashboardTimerStartButton.isEnabled = false
                    dashboardTimerCancelButton.isEnabled = false
                    dashboardTimerStopButton.isEnabled = false
                    viewModel.stopRide()
                }
                dashboardTimerStopButton.isEnabled = true
            }
        }

//        if (dashboardFlavourDelegate.shouldShowGpsChangeSwitch()) {
//            debug_gpsMode.setOnCheckedChangeListener { buttonView, isChecked ->
//                LocationServicesProviderSelector.selectedModeGps = isChecked
//            }
//            debug_gpsMode.visibility = View.VISIBLE
//        } else {
//            debug_gpsMode.visibility = View.GONE
//        }

        dashboardFlavourDelegate.initializeView(binding, findNavController())

        viewModel.dataSyncInProgress.observe(viewLifecycleOwner) { syncing ->
            if (syncing)
                dashboardSyncToast.startSync()
            else
                dashboardSyncToast.finishSync()
        }

        viewModel.overlayFunctionInfoShouldBeShown.observe(viewLifecycleOwner) { shouldBeShown ->
            if (shouldBeShown) {
                NewFeatureOverlayInfoDialog.createAndShow(childFragmentManager)
                    ?.observe(viewLifecycleOwner) {
                        if (it == BasicOneActionDialogFragment.DialogResult.CONFIRMED) {
                            viewModel.markOverlayInfoAsShown()
                        }
                    }
            }
        }
    }

    private fun checkAndShowNoGpsDialog(): Boolean =
        if (viewModel.shouldShowNoGpsDialog) {
            RideCantBeStartedGpsNotExistsDialogFragment
                .createAndShow(childFragmentManager)
                ?.observe(viewLifecycleOwner) {}
            true
        } else
            false

    override fun getBindings(): ViewDataBinding? = binding

    override fun invalidateViewAfterModeChange() {
        super.invalidateViewAfterModeChange()
        dashboardIconBattery?.invalidateBindings()
        dashboardIconInternet?.invalidateBindings()
        dashboardIconGps?.invalidateBindings()
    }

    override fun onResume() {
        super.onResume()
        // reset values
        viewModel.resetSyncState()
//        if (dashboardFlavourDelegate.shouldShowGpsChangeSwitch())
//            debug_gpsMode.isChecked = LocationServicesProviderSelector.selectedModeGps
        batteryStateInterfaceController.setCallback(dashboardIconBattery, CALLBACKS_TAG)
        gpsStateInterfaceController.setCallback(dashboardIconGps, CALLBACKS_TAG)
        dataSenderStateInterfaceController.setCallback(dashboardIconInternet, CALLBACKS_TAG)
    }

    override fun onPause() {
        super.onPause()
        batteryStateInterfaceController.removeCallback(CALLBACKS_TAG)
        gpsStateInterfaceController.removeCallback(CALLBACKS_TAG)
        dataSenderStateInterfaceController.removeCallback(CALLBACKS_TAG)
    }

    override fun onStop() {
        super.onStop()
        dashboardSyncToast.onStop()
    }

    override fun onStart() {
        super.onStart()
        dashboardSyncToast.onStart()
    }

    override fun onBatteryOptimizationDisabled() {
        if (viewModel.isGpsRequiredForRide())
            askForGpsPermissions()
        else
            viewModel.startRide(requireContext())
    }

    override fun onGpsPermissionsGranted() {
        viewModel.startRide(requireContext())
    }
}