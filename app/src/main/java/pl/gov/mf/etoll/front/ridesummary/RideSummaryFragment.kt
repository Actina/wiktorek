package pl.gov.mf.etoll.front.ridesummary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.navigation.fragment.findNavController
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.databinding.FragmentRideSummaryBinding
import pl.gov.mf.etoll.front.BaseDatabindingFragment
import pl.gov.mf.etoll.front.shared.adapter.CardsAdapter
import javax.inject.Inject

class RideSummaryFragment :
    BaseDatabindingFragment<RideSummaryViewModel, RideSummaryFragmentComponent>() {

    @Inject
    lateinit var rideSummaryAdapter: CardsAdapter

    private lateinit var binding: FragmentRideSummaryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        component.inject(viewModel)
        viewModel.generateCards(rideSummaryAdapter)
        lockBackPress { viewModel.onCloseClick() }
    }

    override fun getBindings(): ViewDataBinding? = binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_ride_summary,
            container,
            false
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        rideSummaryAdapter.generateView(
            binding.rideSummaryList
        )
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.navigate.observe(viewLifecycleOwner) { rideSummaryNavTargets ->
            when (rideSummaryNavTargets) {
                RideSummaryViewModel.RideSummaryNavTargets.IDLE -> {
                }
                RideSummaryViewModel.RideSummaryNavTargets.FORWARD_SENT_RIDE_DETAILS -> {
                    viewModel.getParamsToShowMapDetails()?.let {
                        findNavController().navigate(
                            RideSummaryFragmentDirections.actionShowSentRideDetailsSelection(
                                signComponentUseCase.executeCustomSign(),
                                it
                            )
                        )
                    }
                    viewModel.resetNavigation()
                }
                else -> {
                }
            }
        }

        viewModel.navigateBack.observe(viewLifecycleOwner) { shouldReturn ->
            if (shouldReturn)
                findNavController().popBackStack(R.id.dashboardFragment, false)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.addCallbacksForCards(rideSummaryAdapter)
    }

    override fun onPause() {
        super.onPause()
        viewModel.removeCallbacksForCards(rideSummaryAdapter)
    }

    override fun createComponent(): RideSummaryFragmentComponent =
        activityComponent.plus(
            RideSummaryFragmentModule(this, lifecycle, this)
        )

}
