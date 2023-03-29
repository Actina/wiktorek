package pl.gov.mf.etoll.front.configsentridesselection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.databinding.FragmentConfigSentRidesSelectionBinding
import pl.gov.mf.etoll.front.MainActivity
import pl.gov.mf.etoll.front.BaseDatabindingFragment
import pl.gov.mf.etoll.front.configridetypeselection.ConfigRideTypeSelectionFragmentDirections
import pl.gov.mf.etoll.front.configsentridesselection.ConfigSentRidesSelectionFragmentViewModel.SentRidesNavigation
import pl.gov.mf.etoll.front.configsentridesselection.adapter.SentRidesAdapter
import pl.gov.mf.etoll.front.configsentridesselection.details.SentRideDetailsFragment
import pl.gov.mf.mobile.ui.components.dialogs.DialogsHelper
import javax.inject.Inject

class ConfigSentRidesSelectionFragment :
    BaseDatabindingFragment<ConfigSentRidesSelectionFragmentViewModel, SentRidesSelectionFragmentComponent>() {

    @Inject
    lateinit var adapter: SentRidesAdapter

    @Inject
    lateinit var dialogsHelper: DialogsHelper

    private lateinit var binding: FragmentConfigSentRidesSelectionBinding
    private lateinit var sentRidesRecyclerView: RecyclerView
    private lateinit var configSentRidesSelectionToolbar: MaterialToolbar

    override fun createComponent(): SentRidesSelectionFragmentComponent =
        (context as MainActivity).component.plus(
            SentRidesSelectionFragmentModule(this, lifecycle)
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        component.inject(viewModel)
    }

    override fun getBindings(): ViewDataBinding? = binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            layoutInflater, R.layout.fragment_config_sent_rides_selection, container, false
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        adapter.lifecycleOwner = viewLifecycleOwner
        setupView(binding.root)

        return binding.root
    }

    private fun setupView(view: View) {
        sentRidesRecyclerView = view.findViewById(R.id.sent_rides_recycler_view)
        configSentRidesSelectionToolbar =
            view.findViewById(R.id.config_sent_rides_selection_toolbar)
    }

    override fun invalidateViewAfterModeChange() {
        super.invalidateViewAfterModeChange()
        adapter.notifyBindingsChanged()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sentRidesRecyclerView.setHasFixedSize(true)

        configSentRidesSelectionToolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        viewModel.sentRides.observe(viewLifecycleOwner) {
            sentRidesRecyclerView.adapter = adapter.apply {
                submitList(it)
            }
        }

        viewModel.navigate.observe(viewLifecycleOwner) { navigation ->
            if (navigation.location != SentRidesNavigation.Location.IDLE) viewModel.resetNavigation()
            when (navigation.location) {
                SentRidesNavigation.Location.FINISH -> {
                    findNavController().navigate(
                        ConfigRideTypeSelectionFragmentDirections.actionShowDashboard(
                            signComponentUseCase.executeCustomSign()
                        )
                    )
                }
                SentRidesNavigation.Location.SENT_DETAILS -> {
                    val sentInfo = navigation.data

                    findNavController().navigate(
                        ConfigSentRidesSelectionFragmentDirections.actionShowSentRideDetailsSelection(
                            signComponentUseCase.executeCustomSign(), sentInfo
                        )
                    )
                }
                SentRidesNavigation.Location.ERROR -> {
                    dialogsHelper.showTranslatedOkDialog(
                        parentFragmentManager,
                        "critical_error_default_title",
                        "critical_error_default_title",
                        "dialog_ok"
                    ).subscribe()
                }
                else -> {
                }
            }

        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>(
            SentRideDetailsFragment.ON_SENT_RIDE_INFO_RESULT
        )?.observe(
            viewLifecycleOwner
        ) { result ->
            viewModel.onSentRideInfoResult(result)
        }

        viewModel.backShouldBeVisible.observe(viewLifecycleOwner) { visible ->
            if (!visible) lockBackPress()
        }
    }
}