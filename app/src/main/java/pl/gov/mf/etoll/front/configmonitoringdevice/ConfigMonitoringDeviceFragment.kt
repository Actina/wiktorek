package pl.gov.mf.etoll.front.configmonitoringdevice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.databinding.FragmentConfigMonitoringDeviceBinding
import pl.gov.mf.etoll.front.MainActivity
import pl.gov.mf.etoll.front.BaseDatabindingFragment
import pl.gov.mf.etoll.front.configmonitoringdevice.ConfigMonitoringDeviceFragmentViewModel.MonitoringNavigationLocation.*
import pl.gov.mf.mobile.ui.components.dialogs.DialogsHelper
import javax.inject.Inject

class ConfigMonitoringDeviceFragment :
    BaseDatabindingFragment<ConfigMonitoringDeviceFragmentViewModel, ConfigMonitoringDeviceFragmentComponent>() {

    @Inject
    lateinit var dialogsHelper: DialogsHelper

    private lateinit var binding: FragmentConfigMonitoringDeviceBinding

    override fun createComponent(): ConfigMonitoringDeviceFragmentComponent =
        (context as MainActivity).component.plus(
            ConfigMonitoringDeviceFragmentModule(this, lifecycle)
        )

    override fun getBindings(): ViewDataBinding? = binding

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
            R.layout.fragment_config_monitoring_device,
            container,
            false
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<MaterialToolbar>(R.id.config_monitoring_device_toolbar)
            .setNavigationOnClickListener {
                findNavController().popBackStack()
            }

        viewModel.navigate.observe(viewLifecycleOwner) { location ->
            if (location != IDLE)
                viewModel.resetNavigation()
            when (location) {
                BACK_TO_RIDE_DETAILS -> {
                    findNavController().popBackStack()
                }
                SENT_LIST -> {
                    findNavController().navigate(
                        ConfigMonitoringDeviceFragmentDirections.actionShowSentRidesSelection(
                            signComponentUseCase.executeCustomSign()
                        )
                    )
                }
                FINISH ->
                    findNavController().popBackStack(R.id.dashboardFragment, false)
                ERROR -> {
                    dialogsHelper.showTranslatedOkDialog(
                        parentFragmentManager,
                        "critical_error_default_title",
                        "critical_error_default_title",
                        "dialog_ok"
                    )
                        .subscribe()
                }
                else -> {
                }
            }
        }
    }
}