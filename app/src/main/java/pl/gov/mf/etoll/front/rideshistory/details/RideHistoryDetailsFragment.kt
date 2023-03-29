package pl.gov.mf.etoll.front.rideshistory.details

import android.content.Context
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
import pl.gov.mf.etoll.databinding.FragmentRideHistoryDetailsBinding
import pl.gov.mf.etoll.front.BaseDatabindingFragment
import pl.gov.mf.etoll.front.rideshistory.details.adapter.RideHistoryCellItem
import pl.gov.mf.etoll.front.rideshistory.details.adapter.RideHistoryCellItem.*
import pl.gov.mf.etoll.front.rideshistory.details.adapter.RideHistoryDetailsAdapter
import pl.gov.mf.etoll.front.rideshistory.details.adapter.RideHistoryDetailsCellViewModel
import pl.gov.mf.etoll.front.rideshistory.details.adapter.RideHistoryDetailsCellViewModel.*
import pl.gov.mf.etoll.translations.AppLanguageManagerUC
import javax.inject.Inject

class RideHistoryDetailsFragment :
    BaseDatabindingFragment<RideHistoryDetailsFragmentViewModel, RideHistoryDetailsFragmentComponent>() {

    @Inject
    lateinit var adapter: RideHistoryDetailsAdapter

    @Inject
    lateinit var getCurrentLanguageUseCase: AppLanguageManagerUC.GetCurrentLanguageUseCase

    private lateinit var binding: FragmentRideHistoryDetailsBinding

    override fun getBindings(): ViewDataBinding? = binding

    override fun createComponent(): RideHistoryDetailsFragmentComponent =
        activityComponent.plus(
            RideHistoryDetailsFragmentModule(this, lifecycle)
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        component.inject(viewModel)
        viewModel.loadRideHistoryDetails(requireArguments().getInt("id"))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_ride_history_details,
            container,
            false
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        adapter.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<RecyclerView>(R.id.ride_history_details_list).adapter = adapter
        observeViewModel()

        view.findViewById<MaterialToolbar>(R.id.ride_history_details_toolbar)
            .setNavigationOnClickListener {
                findNavController().popBackStack()
            }
    }

    private fun observeViewModel() {
        viewModel.detailsCellsViewModels.observe(viewLifecycleOwner) {
            adapter.submitList(it.toRideHistoryCellUiItems(requireContext()))
        }
    }
}

private fun List<RideHistoryDetailsCellViewModel>.toRideHistoryCellUiItems(context: Context): List<RideHistoryCellItem> =
    map {
        when (it) {
            is CategoryWeightExceededItemCellViewModel -> CategoryWeightExceededItem(it)
            is ConnectedSentRidesHeaderCellViewModel -> ConnectedSentRidesHeader(it)
            is LocationReportItemCellViewModel -> LocationReportItem(it)
            is RideTimeItemCellViewModel -> RideTimeItem(it)
            is SentNumberItemCellViewModel -> SendNumberItem(context, it)
            is VehicleItemCellViewModel -> VehicleItem(it)
        }
    }