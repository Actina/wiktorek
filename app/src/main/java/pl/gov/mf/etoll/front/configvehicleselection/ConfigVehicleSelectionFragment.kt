package pl.gov.mf.etoll.front.configvehicleselection

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.core.model.CoreVehicle
import pl.gov.mf.etoll.core.model.CoreVehicleCategory
import pl.gov.mf.etoll.databinding.FragmentConfigVehicleSelectionBinding
import pl.gov.mf.etoll.front.BaseDatabindingFragment
import pl.gov.mf.etoll.front.configvehicleselection.ConfigVehicleSelectionFragment.ConfigVehicleSelectionConstHeader.*
import pl.gov.mf.etoll.front.configvehicleselection.adapteritem.ConfigVehicleSelectionHeader
import pl.gov.mf.etoll.front.configvehicleselection.adapteritem.ConfigVehicleSelectionSectionHeader
import pl.gov.mf.etoll.front.configvehicleselection.adapteritem.ConfigVehicleSelectionVehicleItem
import pl.gov.mf.etoll.utils.DynamicBindingAdapter
import pl.gov.mf.mobile.ui.components.dialogs.DialogsHelper
import javax.inject.Inject

class ConfigVehicleSelectionFragment :
    BaseDatabindingFragment<ConfigVehicleSelectionFragmentViewModel, ConfigVehicleSelectionFragmentComponent>() {

    @Inject
    lateinit var dialogsHelper: DialogsHelper

    private lateinit var binding: FragmentConfigVehicleSelectionBinding

    private lateinit var adapter: DynamicBindingAdapter
    private val consHeaders: HashMap<ConfigVehicleSelectionConstHeader, DynamicBindingAdapter.Item> =
        hashMapOf(
            TOP to ConfigVehicleSelectionHeader(),
            SECTION_LAST_USED to ConfigVehicleSelectionSectionHeader("config_vehicle_selection_section_last_used"),
            SECTION_OTHERS to ConfigVehicleSelectionSectionHeader(
                "config_vehicle_selection_section_other",
                true
            ),
            SECTION_NO_VEHICLES_ASSIGNED to ConfigVehicleSelectionSectionHeader(
                "config_vehicle_selection_no_vehicles_assigned",
                true,
                Gravity.CENTER_HORIZONTAL
            )
        )

    override fun getBindings(): ViewDataBinding? = binding

    override fun invalidateViewAfterModeChange() {
        super.invalidateViewAfterModeChange()
        adapter.notifyBindingsExpressionsChanged()
    }

    override fun createComponent(): ConfigVehicleSelectionFragmentComponent =
        activityComponent.plus(
            ConfigVehicleSelectionFragmentModule(this, lifecycle)
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        component.inject(viewModel)

        lockBackPress {
            findNavController().popBackStack()
        }

        viewModel.restoreModel()
        viewModel.onArgumentsSet(arguments)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_config_vehicle_selection,
            container,
            false
        )

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        adapter = DynamicBindingAdapter(viewLifecycleOwner)
        binding.configVehicleSelectionRecyclerView.adapter = adapter

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<MaterialToolbar>(R.id.config_vehicle_selection_toolbar)
            .setNavigationOnClickListener {
                findNavController().popBackStack()
            }

        viewModel.recentOthersSelected.observe(viewLifecycleOwner) {
            adapter.submitList(regenerateAdapterItems(it))
        }

        viewModel.navigate.observe(viewLifecycleOwner) { selected ->
            if (selected != ConfigVehicleSelectionNavTargets.IDLE)
                viewModel.resetNavigation()
            when (selected) {
                ConfigVehicleSelectionNavTargets.IDLE -> {
                }
                ConfigVehicleSelectionNavTargets.TRAILER -> {
                    findNavController().navigate(
                        ConfigVehicleSelectionFragmentDirections.actionShowTrailerCategory(
                            signComponentUseCase.executeCustomSign()
                        )
                    )
                }
                ConfigVehicleSelectionNavTargets.MONITORING_DEVICE -> {
                    findNavController().navigate(
                        ConfigVehicleSelectionFragmentDirections.actionShowConfigMonitoringDevice(
                            signComponentUseCase.executeCustomSign()
                        )
                    )
                }
                ConfigVehicleSelectionNavTargets.ERROR -> {
                    dialogsHelper.showTranslatedOkDialog(
                        parentFragmentManager,
                        "critical_error_default_title",
                        "critical_error_default_title",
                        "dialog_ok"
                    )
                        .subscribe({

                        }, {

                        })
                }
                ConfigVehicleSelectionNavTargets.FINISH -> {
                    findNavController().popBackStack(R.id.dashboardFragment, false)
                }
                ConfigVehicleSelectionNavTargets.SENT_LIST -> {
                    findNavController().navigate(
                        ConfigVehicleSelectionFragmentDirections.actionShowSentList(
                            signComponentUseCase.executeCustomSign()
                        )
                    )
                }
                else -> {
                    throw RuntimeException("Unknown navigation path in config type selection")
                }
            }
        }
    }

    private fun regenerateAdapterItems(recentAndOtherVehicles: Triple<List<CoreVehicle>, List<CoreVehicle>, CoreVehicle?>): MutableList<DynamicBindingAdapter.Item> {
        val items = mutableListOf<DynamicBindingAdapter.Item>()
        consHeaders[TOP]?.let {
            items.add(it)
        }
        val (recents, others, selected) = recentAndOtherVehicles

        if (recents.isEmpty() && others.isEmpty()) {
            consHeaders[SECTION_NO_VEHICLES_ASSIGNED]?.let {
                items.add(it)
            }
        } else if (recents.isEmpty() && others.isNotEmpty()) {
            others.forEach {
                items.add(createVehicleItem(it, selected))
            }
        } else if (recents.isNotEmpty() && others.isEmpty()) {
            consHeaders[SECTION_LAST_USED]?.let {
                items.add(it)
            }
            recents.forEach {
                items.add(createVehicleItem(it, selected))
            }
        } else {
            consHeaders[SECTION_LAST_USED]?.let {
                items.add(it)
            }
            recents.forEach {
                items.add(createVehicleItem(it, selected))
            }

            consHeaders[SECTION_OTHERS]?.let {
                items.add(it)
            }
            others.forEach {
                items.add(createVehicleItem(it, selected))
            }
        }
        items.add(DynamicBindingAdapter.Item(R.layout.item_ride_configuration_empty_space))

        return items
    }

    private fun createVehicleItem(
        vehicleFromList: CoreVehicle,
        selected: CoreVehicle?
    ): ConfigVehicleSelectionVehicleItem {
        return ConfigVehicleSelectionVehicleItem(
            vehicleFromList,
            vehicleFromList.id == selected?.id,
            if (viewModel.isSentActive()) vehicleFromList.category != CoreVehicleCategory.MOTORCYCLE.value else true,
            viewModel::onVehicleSelected,
            this::createBottomSheetDialog
        )
    }

    private fun createBottomSheetDialog(selectedVehicle: CoreVehicle) {
        if (!viewModel.isDialogShown()) {
            ConfigVehicleSelectionBottomSheetFragment.createAndShow(
                parentFragmentManager,
                selectedVehicle.toJSON()
            )
                .dialogResult
                .observe(viewLifecycleOwner) {
                    if (it == ConfigVehicleSelectionBottomSheetFragment.BottomSheetDialogResult.CHOSEN) {
                        viewModel.onVehicleSelected(selectedVehicle)
                    }
                    if (it == ConfigVehicleSelectionBottomSheetFragment.BottomSheetDialogResult.DISMISSED) {
                        viewModel.notifyDialogHidden()
                    }
                }
            viewModel.notifyDialogShown()
        }
    }

    enum class ConfigVehicleSelectionConstHeader {
        TOP,
        SECTION_LAST_USED,
        SECTION_OTHERS,
        SECTION_NO_VEHICLES_ASSIGNED
    }

}
