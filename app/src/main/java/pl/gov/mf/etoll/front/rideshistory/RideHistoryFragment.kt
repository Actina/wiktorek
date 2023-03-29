package pl.gov.mf.etoll.front.rideshistory

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.iterator
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import org.joda.time.LocalDate
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.core.ridehistory.model.RideHistoryDataItem
import pl.gov.mf.etoll.databinding.FragmentRideHistoryBinding
import pl.gov.mf.etoll.front.BaseDatabindingFragment
import pl.gov.mf.etoll.front.rideshistory.RideHistoryFragmentViewModel.ViewData
import pl.gov.mf.etoll.front.rideshistory.RideHistoryFragmentViewModel.ViewState
import pl.gov.mf.etoll.front.rideshistory.adapter.*
import pl.gov.mf.etoll.front.rideshistory.adapter.RideHistoryCellViewModel.*
import pl.gov.mf.etoll.translations.AppLanguageManagerUC
import pl.gov.mf.etoll.ui.components.dialogs.CalendarDialogFragment
import pl.gov.mf.mobile.utils.translate
import javax.inject.Inject

class RideHistoryFragment :
    BaseDatabindingFragment<RideHistoryFragmentViewModel, RideHistoryFragmentComponent>() {
    @Inject
    lateinit var adapter: RidesHistoryAdapter

    @Inject
    lateinit var getCurrentLanguageUseCase: AppLanguageManagerUC.GetCurrentLanguageUseCase

    private lateinit var binding: FragmentRideHistoryBinding

    private lateinit var rideHistoryToolbar: MaterialToolbar
    private lateinit var rideHistoryList: RecyclerView

    override fun createComponent(): RideHistoryFragmentComponent =
        activityComponent.plus(
            RideHistoryFragmentModule(this, lifecycle)
        )

    override fun invalidateViewAfterModeChange() {
        super.invalidateViewAfterModeChange()
        adapter.notifyBindingsChanged()
    }

    override fun getBindings(): ViewDataBinding? = binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        component.inject(viewModel)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_ride_history,
            container,
            false
        )
        binding.viewModel = viewModel
        adapter.lifecycleOwner = viewLifecycleOwner
        binding.lifecycleOwner = viewLifecycleOwner
        setupView(binding.root)
        return binding.root
    }

    private fun setupView(view: View) {
        rideHistoryToolbar = view.findViewById(R.id.ride_history_toolbar)
        rideHistoryList = view.findViewById(R.id.rideHistoryList)
    }

    private fun translateMenu() {
        rideHistoryToolbar.menu.iterator().forEach {
            it.title = it.title.toString().translate(requireContext())
        }

        rideHistoryToolbar.setOnMenuItemClickListener { item ->
            return@setOnMenuItemClickListener when (item.itemId) {
                R.id.ride_history_menu_calendar -> {
                    val showDate = (viewModel.historyDate ?: LocalDate.now()).toDate().time
                    val minDate = (viewModel.firstHistoryDate ?: LocalDate.now()).toDate().time
                    var maxDate = (viewModel.lastHistoryDate ?: LocalDate.now()).toDate().time
                    if (maxDate < LocalDate.now().toDate().time) maxDate =
                        LocalDate.now().toDate().time
                    CalendarDialogFragment.createDialog(
                        minDate, showDate, maxDate,
                        getCurrentLanguageUseCase.execute().apiLanguageName
                    )
                        .showDialog(childFragmentManager, CALENDAR_DIALOG_TAG)
                        .observe(viewLifecycleOwner) { selection ->
                            viewModel.updateHistoryDate(selection.date)
                        }
                    true
                }
                else -> false
            }
        }
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        init()
        observeViewModels()

        rideHistoryToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()
        hideBlur()
    }

    private fun init() {
        translateMenu()
        rideHistoryList.adapter = adapter
        viewModel.loadHistory()
    }

    private fun observeViewModels() {
        viewModel.viewData.observe(viewLifecycleOwner) { viewData ->
            when (viewData.state) {
                ViewState.LOADING -> showLoading()
                ViewState.ERROR -> hideLoading()
                ViewState.HISTORY_LOADED -> {
                    adapter.submitList(
                        viewData.toRideHistoryCellUiItems(requireContext())
                    ) {
                        hideLoading()
                        rideHistoryList.scrollToPosition(0)
                    }
                }
            }
        }

        viewModel.navigate.observe(viewLifecycleOwner) { state ->
            when (state) {
                is NavigationTargets.Details -> {
                    findNavController().navigate(
                        RideHistoryFragmentDirections
                            .actionRideHistoryFragmentToRideHistoryDetailsFragment(
                                signComponentUseCase.executeCustomSign(),
                                state.item.id
                            )
                    )
                    viewModel.resetNavigation()
                }
                else -> {
                    //do nothing
                }
            }
        }
    }

    private fun ViewData.toRideHistoryCellUiItems(context: Context): List<RideHistoryCellUiItem> =
        rideHistoryCellsViewModels.map {
            when (it) {
                is RideHistoryDisruptionItemViewModel -> RideHistoryDisruptionUiItem(context, it)
                is RideHistoryEventItemViewModel -> RideHistoryEventUiItem(context, it).apply {
                    detailsClickListener = object : OnRideDetailsClickListener {
                        override fun onRideDetailsClick(item: RideHistoryDataItem) {
                            findNavController().navigate(
                                RideHistoryFragmentDirections
                                    .actionRideHistoryFragmentToRideHistoryDetailsFragment(
                                        signComponentUseCase.executeCustomSign(),
                                        item.id
                                    )
                            )
                        }
                    }
                }
                is RideHistoryHeaderItemViewModel -> RideHistoryHeaderUiItem(it)
            }
        }

    companion object {
        const val CALENDAR_DIALOG_TAG = "RideHistoryCalendarDialogFragment"
    }
}


