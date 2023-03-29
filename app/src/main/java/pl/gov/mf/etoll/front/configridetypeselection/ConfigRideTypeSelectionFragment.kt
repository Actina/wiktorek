package pl.gov.mf.etoll.front.configridetypeselection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.databinding.FragmentConfigRideTypeSelectionBinding
import pl.gov.mf.etoll.front.BaseDatabindingFragment
import pl.gov.mf.etoll.front.bottomNavigation.ds.BottomNavigationSelectedSection
import pl.gov.mf.etoll.front.bottomNavigation.ds.BottomNavigationState
import pl.gov.mf.etoll.ui.components.dialogs.NoConnectionToSentDialogFragment
import pl.gov.mf.etoll.ui.components.dialogs.ridedetails.SentNotAvailableForMotorcyclesDialogFragment
import pl.gov.mf.mobile.ui.components.dialogs.basic.BasicOneActionDialogFragment

class ConfigRideTypeSelectionFragment :
    BaseDatabindingFragment<ConfigRideTypeSelectionFragmentViewModel, ConfigRideTypeSelectionFragmentComponent>() {

    private lateinit var binding: FragmentConfigRideTypeSelectionBinding
    private lateinit var configRideTypeSelectionToolbar: MaterialToolbar
    private lateinit var configRideTypeSelectionBottomButton: MaterialButton
    private lateinit var configRideTypeSelectionSentArea: View
    private lateinit var configRideTypeSelectionTolledSwitch: SwitchMaterial
    private lateinit var configRideTypeSelectionSentSwitch: SwitchMaterial
    private lateinit var configRideTypeSelectionTolledArea: View
    private lateinit var configRideTypeSelectionTolledContent: TextView
    private lateinit var configRideTypeSelectionTolledHeader: TextView
    private lateinit var configRideTypeSelectionSentContent: TextView
    private lateinit var configRideTypeSelectionSentHeader: TextView

    override fun createComponent(): ConfigRideTypeSelectionFragmentComponent =
        activityComponent.plus(ConfigRideTypeSelectionFragmentModule(this, lifecycle))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        component.inject(viewModel)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_config_ride_type_selection,
            container, false
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        setupView(binding.root)

        return binding.root
    }

    private fun setupView(view: View) {
        configRideTypeSelectionToolbar = view.findViewById(R.id.config_ride_type_selection_toolbar)
        configRideTypeSelectionBottomButton =
            view.findViewById(R.id.config_ride_type_selection_bottom_button)
        configRideTypeSelectionSentArea =
            view.findViewById(R.id.config_ride_type_selection_sent_area)
        configRideTypeSelectionTolledSwitch =
            view.findViewById(R.id.config_ride_type_selection_tolled_switch)
        configRideTypeSelectionSentSwitch =
            view.findViewById(R.id.config_ride_type_selection_sent_switch)
        configRideTypeSelectionTolledArea =
            view.findViewById(R.id.config_ride_type_selection_tolled_area)
        configRideTypeSelectionTolledContent =
            view.findViewById(R.id.config_ride_type_selection_tolled_content)
        configRideTypeSelectionTolledHeader =
            view.findViewById(R.id.config_ride_type_selection_tolled_header)
        configRideTypeSelectionSentContent =
            view.findViewById(R.id.config_ride_type_selection_sent_content)
        configRideTypeSelectionSentHeader =
            view.findViewById(R.id.config_ride_type_selection_sent_header)
    }

    override fun getBindings(): ViewDataBinding? = binding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configRideTypeSelectionToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        configRideTypeSelectionBottomButton.setOnClickListener {
            if (viewModel.isMixRideBlocked()) {
                SentNotAvailableForMotorcyclesDialogFragment.createAndShow(childFragmentManager)
            } else {
                if (viewModel.onRideModeSelected()) {
                    disableViewEdition()
                }
            }
        }

        // observe changes
        viewModel.rideSelectionState.observe(viewLifecycleOwner) { state ->
            if (state.sentShouldBeVisible) showSentAndSyncSwitch(state) else hideSentAndDisableSwitch()
            if (state.tolledRideShouldBeVisible) showTolledAndSyncSwitch(state) else hideTolledAndDisableSwitch()
            // what if none - sent nor tolled ride is visible? - DECISION FROM 07.10.2020 - we do not allow user to disable sent in settings if no other ride mode is allowed atm.
            validateButtonState()
        }

        configRideTypeSelectionSentArea.setOnClickListener {
            viewModel.onRideSelectionChanged(
                configRideTypeSelectionTolledSwitch.isChecked,
                !configRideTypeSelectionSentSwitch.isChecked
            )
        }

        configRideTypeSelectionTolledArea.setOnClickListener {
            viewModel.onRideSelectionChanged(
                !configRideTypeSelectionTolledSwitch.isChecked,
                configRideTypeSelectionSentSwitch.isChecked
            )
        }

        viewModel.rideModeSelected.observe(viewLifecycleOwner) { selected ->
            if (selected != ConfigRideTypeNavTargets.IDLE)
                viewModel.resetNavigation()
            when (selected) {
                ConfigRideTypeNavTargets.NAVIGATE_FORWARD_VEHICLE_SELECTION -> {
                    if (viewModel.sentIsDoneInOfflineMode()) {
                        NoConnectionToSentDialogFragment.createAndShow(childFragmentManager)
                            ?.observe(viewLifecycleOwner) {
                                if (it == BasicOneActionDialogFragment.DialogResult.CONFIRMED)
                                    navigateToSelectVehicle()
                            }
                    } else {
                        navigateToSelectVehicle()
                    }
                }
                ConfigRideTypeNavTargets.NAVIGATE_FORWARD_MONITORING_DEVICE -> {
                    if (viewModel.sentIsDoneInOfflineMode()) {
                        NoConnectionToSentDialogFragment.createAndShow(childFragmentManager)
                            ?.observe(viewLifecycleOwner) {
                                if (it == BasicOneActionDialogFragment.DialogResult.CONFIRMED)
                                    navigateToSelectMonitoringDevice()
                            }
                    } else {
                        navigateToSelectMonitoringDevice()
                    }
                }
                ConfigRideTypeNavTargets.NAVIGATE_FORWARD_TRAILER -> {
                    if (viewModel.sentIsDoneInOfflineMode()) {
                        NoConnectionToSentDialogFragment.createAndShow(childFragmentManager)
                            ?.observe(viewLifecycleOwner) {
                                if (it == BasicOneActionDialogFragment.DialogResult.CONFIRMED)
                                    navigateToSelectTrailer()
                            }
                    } else {
                        navigateToSelectTrailer()
                    }
                }
                ConfigRideTypeNavTargets.FINISH_CONFIGURATION -> {
                    if (viewModel.sentIsDoneInOfflineMode()) {
                        NoConnectionToSentDialogFragment.createAndShow(childFragmentManager)
                            ?.observe(viewLifecycleOwner) {
                                if (it == BasicOneActionDialogFragment.DialogResult.CONFIRMED)
                                    navigateToDashboard()
                            }
                    } else {
                        navigateToDashboard()
                    }
                }
                ConfigRideTypeNavTargets.IDLE -> {
                    // do nothing
                }
                else -> {
                    throw RuntimeException("Unknown navigation path in config type selection")
                }
            }
        }
    }

    private fun navigateToSelectTrailer() {
        findNavController().navigate(
            ConfigRideTypeSelectionFragmentDirections.actionShowTrailerCategory(
                signComponentUseCase.executeCustomSign()
            )
        )
    }

    private fun navigateToDashboard() {
        findNavController().popBackStack(R.id.dashboardFragment, false)
    }

    private fun navigateToSelectMonitoringDevice() {
        findNavController().navigate(
            ConfigRideTypeSelectionFragmentDirections.actionShowConfigMonitoringDevice(
                signComponentUseCase.executeCustomSign()
            )
        )
    }

    private fun navigateToSelectVehicle() {
        findNavController().navigate(
            ConfigRideTypeSelectionFragmentDirections.actionSelectVehicle(
                signComponentUseCase.executeCustomSign()
            )
        )
    }

    private fun validateButtonState() {
        configRideTypeSelectionBottomButton.isEnabled =
            configRideTypeSelectionTolledSwitch.isChecked || configRideTypeSelectionSentSwitch.isChecked
    }

    private fun showSentAndSyncSwitch(state: RideSelectionState) {
        if (configRideTypeSelectionSentArea.visibility != View.VISIBLE) {
            configRideTypeSelectionSentArea.visibility = View.VISIBLE
            configRideTypeSelectionSentSwitch.visibility = View.VISIBLE
            configRideTypeSelectionSentContent.visibility = View.VISIBLE
            configRideTypeSelectionSentHeader.visibility = View.VISIBLE
        }
        configRideTypeSelectionSentSwitch.isChecked = state.sentRideIsEnabled
    }

    private fun hideSentAndDisableSwitch() {
        if (configRideTypeSelectionSentArea.visibility != View.GONE) {
            configRideTypeSelectionSentArea.visibility = View.GONE
            configRideTypeSelectionSentSwitch.visibility = View.GONE
            configRideTypeSelectionSentSwitch.isChecked = false
            configRideTypeSelectionSentContent.visibility = View.GONE
            configRideTypeSelectionSentHeader.visibility = View.GONE
        }
    }

    private fun showTolledAndSyncSwitch(state: RideSelectionState) {
        if (configRideTypeSelectionTolledArea.visibility == View.GONE) {
            configRideTypeSelectionTolledArea.visibility = View.VISIBLE
            configRideTypeSelectionTolledSwitch.visibility = View.VISIBLE
            configRideTypeSelectionTolledContent.visibility = View.VISIBLE
            configRideTypeSelectionTolledHeader.visibility = View.VISIBLE
        }
        configRideTypeSelectionTolledSwitch.isChecked = state.tolledRideIsEnabled
    }

    private fun hideTolledAndDisableSwitch() {
        if (configRideTypeSelectionTolledArea.visibility != View.GONE) {
            configRideTypeSelectionTolledArea.visibility = View.GONE
            configRideTypeSelectionTolledSwitch.visibility = View.GONE
            configRideTypeSelectionTolledSwitch.isChecked = false
            configRideTypeSelectionTolledContent.visibility = View.GONE
            configRideTypeSelectionTolledHeader.visibility = View.GONE
        }
    }

    private fun disableViewEdition() {
        configRideTypeSelectionBottomButton.isEnabled = false
        configRideTypeSelectionSentArea.isEnabled = false
        configRideTypeSelectionTolledArea.isEnabled = false
    }

    override fun getBottomNavigationState(): BottomNavigationState =
        BottomNavigationState(false, BottomNavigationSelectedSection.CENTER)
}