package pl.gov.mf.etoll.front.ridedetails.sentselection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.databinding.FragmentRideDetailsSentSelectionBinding
import pl.gov.mf.etoll.front.MainActivity
import pl.gov.mf.etoll.front.BaseDatabindingFragment
import pl.gov.mf.etoll.front.configsentridesselection.details.SentRideDetailsFragment
import pl.gov.mf.etoll.front.ridedetails.sentselection.RideDetailsSentSelectionFragmentViewModel.ViewState
import pl.gov.mf.etoll.front.ridedetails.sentselection.adapter.SentRidesAdapter
import pl.gov.mf.etoll.ui.components.dialogs.ridedetails.ConfirmSentActivateDialogFragment
import pl.gov.mf.etoll.ui.components.dialogs.ridedetails.ConfirmSentCancelDialogFragment
import pl.gov.mf.etoll.ui.components.dialogs.ridedetails.ConfirmSentEndDialogFragment
import pl.gov.mf.mobile.ui.components.dialogs.DialogsHelper
import pl.gov.mf.mobile.ui.components.dialogs.basic.BasicTwoActionsDialogFragment
import javax.inject.Inject


class RideDetailsSentSelectionFragment :
    BaseDatabindingFragment<RideDetailsSentSelectionFragmentViewModel, RideDetailsSentSelectionFragmentComponent>() {

    @Inject
    lateinit var adapter: SentRidesAdapter

    @Inject
    lateinit var dialogsHelper: DialogsHelper

    private lateinit var binding: FragmentRideDetailsSentSelectionBinding

    private lateinit var rideDetailsSentList: RecyclerView
    private lateinit var rideDetailsSentSelectionToolbar: MaterialToolbar
    private lateinit var rideDetailsSentListActivateButton: MaterialButton

    override fun getBindings(): ViewDataBinding? = binding

    override fun invalidateViewAfterModeChange() {
        super.invalidateViewAfterModeChange()
        adapter.notifyBindingsChanged()
    }

    override fun createComponent(): RideDetailsSentSelectionFragmentComponent =
        (context as MainActivity).component.plus(
            RideDetailsSentSelectionFragmentModule(this, lifecycle)
        )

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
            R.layout.fragment_ride_details_sent_selection,
            container,
            false
        )
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        adapter.lifecycleOwner = viewLifecycleOwner
        setupView(binding.root)

        return binding.root
    }

    private fun setupView(view: View) {
        rideDetailsSentList = view.findViewById(R.id.ride_details_sent_list)
        rideDetailsSentSelectionToolbar =
            view.findViewById(R.id.ride_details_sent_selection_toolbar)
        rideDetailsSentListActivateButton =
            view.findViewById(R.id.ride_details_sent_list_activate_button)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rideDetailsSentList.adapter = adapter
        rideDetailsSentList.setHasFixedSize(true)

        rideDetailsSentSelectionToolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        rideDetailsSentListActivateButton.setOnClickListener {
            ConfirmSentActivateDialogFragment
                .createAndShow(childFragmentManager)
                ?.observe(viewLifecycleOwner) { data ->
                    if (data == BasicTwoActionsDialogFragment.DialogResult.CONFIRMED) {
                        viewModel.onActivateClick()
                    }
                }
        }

        viewModel.viewState.observe(viewLifecycleOwner) { viewState ->
            when (viewState) {
                is ViewState.Details -> {
                    viewModel.resetNavigation()
                    findNavController().navigate(
                        RideDetailsSentSelectionFragmentDirections.actionShowSentRideDetailsSelection(
                            signComponentUseCase.executeCustomSign(),
                            viewState.data
                        )
                    )
                }
                is ViewState.ListUpdate -> {
                    adapter.submitList(viewState.data)
                    viewModel.resetNavigation()
                }
                is ViewState.ShowCancelSentDialog -> {
                    ConfirmSentCancelDialogFragment
                        .createAndShow(
                            childFragmentManager,
                            viewModel.shouldFinishRideAfterActionOnList
                        )
                        ?.observe(viewLifecycleOwner) { data ->
                            if (data == BasicTwoActionsDialogFragment.DialogResult.CONFIRMED) {
                                viewModel.cancelSent(viewState.item)
                            }
                        }
                }
                is ViewState.ShowEndSentDialog -> {
                    ConfirmSentEndDialogFragment
                        .createAndShow(
                            childFragmentManager,
                            viewModel.shouldFinishRideAfterActionOnList
                        )
                        ?.observe(viewLifecycleOwner) { data ->
                            if (data == BasicTwoActionsDialogFragment.DialogResult.CONFIRMED) {
                                viewModel.finishSent(viewState.item)
                            }
                        }
                }
                is ViewState.FinishedRide -> {
                    findNavController().popBackStack(R.id.dashboardFragment, false)
                }
                is ViewState.Nothing -> {
                    // do nothing
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
    }
}