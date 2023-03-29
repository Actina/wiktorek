package pl.gov.mf.etoll.front.ridedetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.databinding.FragmentRideDetailsBinding
import pl.gov.mf.etoll.front.BaseDatabindingFragmentWithPermissionsSupport
import pl.gov.mf.etoll.front.config.RideConfigurationCoordinator
import pl.gov.mf.etoll.front.ridedetails.RideDetailsViewModel.RideDetailsNavigation.*
import pl.gov.mf.etoll.ui.components.dialogs.MonitoringDeviceCantBeChangedGpsNotExistsDialogFragment
import pl.gov.mf.etoll.ui.components.dialogs.ridedetails.SentNotAvailableForMotorcyclesDialogFragment
import javax.inject.Inject

class RideDetailsFragment :
    BaseDatabindingFragmentWithPermissionsSupport<RideDetailsViewModel, RideDetailsFragmentComponent>() {

    @Inject
    lateinit var configurationCoordinator: RideConfigurationCoordinator

    private lateinit var binding: FragmentRideDetailsBinding

    override fun createComponent(): RideDetailsFragmentComponent = activityComponent.plus(
        RideDetailsFragmentModule(this, lifecycle)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        component.inject(viewModel)
    }

    override fun getBindings(): ViewDataBinding? = binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_ride_details,
            container, false
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<MaterialToolbar>(R.id.ride_details_toolbar).setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.navigate.observe(viewLifecycleOwner) { navigation ->
            when (navigation) {
                ADD_TOLLED -> {
                    configurationCoordinator.setDataBasedOnExistingConfiguration(
                        RideConfigurationCoordinator.RideConfigurationDestination.VEHICLE_SELECTION
                    )
                    findNavController().navigate(
                        RideDetailsFragmentDirections.actionShowTolledConfig(
                            signComponentUseCase.executeCustomSign()
                        )
                    )
                    viewModel.resetNavigation()
                }
                SHOW_MAP_WITH_RIDE_DETAILS -> {
                    findNavController().navigate(
                        RideDetailsFragmentDirections.actionShowRideDetailsMap(
                            signComponentUseCase.executeCustomSign()
                        )
                    )
                    viewModel.resetNavigation()
                }
                TRAILER_CONFIGURATION -> {
                    configurationCoordinator.setDataBasedOnExistingConfiguration(
                        RideConfigurationCoordinator.RideConfigurationDestination.TRAILER
                    )
                    findNavController().navigate(
                        RideDetailsFragmentDirections.actionShowTrailerCategory(
                            signComponentUseCase.executeCustomSign()
                        )
                    )
                    viewModel.resetNavigation()
                }
                MONITORING_DEVICE -> {
                    viewModel.resetNavigation()
                    // check permissions
                    if (viewModel.shouldShowNoGpsDialog) {
                        MonitoringDeviceCantBeChangedGpsNotExistsDialogFragment
                            .createAndShow(childFragmentManager)
                            ?.observe(viewLifecycleOwner) {}
                    } else if (viewModel.isGpsRequiredForAlternativeConfiguration() && !checkGpsPermissionsUseCase.execute()) {
                        askForGpsPermissions()
                    } else {
                        configurationCoordinator.setDataBasedOnExistingConfiguration(
                            RideConfigurationCoordinator.RideConfigurationDestination.MONITORING_DEVICE
                        )
                        findNavController().navigate(
                            RideDetailsFragmentDirections.actionShowConfigMonitoringDevice(
                                signComponentUseCase.executeCustomSign()
                            )
                        )
                    }
                }
                RIDE_DATA -> {
                    viewModel.resetNavigation()
                    findNavController().navigate(
                        RideDetailsFragmentDirections.actionRideDataFragment(
                            signComponentUseCase.executeCustomSign()
                        )
                    )
                }
                RIDE_DETAILS_SENT_SELECTION -> {
                    configurationCoordinator.setDataBasedOnExistingConfiguration(
                        RideConfigurationCoordinator.RideConfigurationDestination.SENT_LIST
                    )
                    findNavController().navigate(
                        RideDetailsFragmentDirections.actionRideDetailsSentSelectionFragment(
                            signComponentUseCase.executeCustomSign()
                        )
                    )
                    viewModel.resetNavigation()
                }
                IDLE -> {
                }
                MIX_NOT_AVAILABLE_FOR_MOTORCYCLES -> {
                    SentNotAvailableForMotorcyclesDialogFragment.createAndShow(childFragmentManager)
                }
                else -> {
                }
            }
        }
    }

    override fun onGpsPermissionsGranted() {
        viewModel.onChangeDeviceClick()
    }


    override fun onBatteryOptimizationDisabled() {
        // should not be required in this fragment
    }

    override fun getGpsPermissionsRequestDialogText(): Triple<String, String, String> = Triple(
        "gps_permission_request_title_android",
        "gps_permission_request_body_ride_details_android",
        "gps_permission_request_button_android"
    )

    override fun getGpsPermissionsRequestDialogTextForManualAction(): Triple<String, String, String> =
        Triple(
            "gps_permission_request_title_android",
            "gps_permission_request_body_ride_details_system_above_10_android",
            "gps_permission_request_button_android"
        )
}