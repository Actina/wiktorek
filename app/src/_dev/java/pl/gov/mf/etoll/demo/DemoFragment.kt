package pl.gov.mf.etoll.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.front.BaseDatabindingFragment
import pl.gov.mf.etoll.front.bottomNavigation.ds.BottomNavigationSelectedSection
import pl.gov.mf.etoll.front.bottomNavigation.ds.BottomNavigationState
import pl.gov.mf.etoll.front.watchdog.DemoListItem
import pl.gov.mf.etoll.front.watchdog.DemoListItemType
import pl.gov.mf.etoll.networking.api.model.EventStreamLocation
import pl.gov.mf.etoll.networking.api.model.EventStreamLocationWithoutLocation
import pl.gov.mf.mobile.ui.components.utils.WrapContentLinearLayoutManager
import javax.inject.Inject

class DemoFragment : BaseDatabindingFragment<DemoFragmentViewModel, DemoFragmentComponent>(),
    OnItemClickedListener {

    @Inject
    lateinit var itemsAdapter: DemoFragmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        component.inject(viewModel)
    }

    override fun createComponent(): DemoFragmentComponent =
        activityComponent.plus(DemoFragmentModule(this, lifecycle))

    override fun getBottomNavigationState(): BottomNavigationState =
        BottomNavigationState(true, BottomNavigationSelectedSection.LEFT)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_demo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemsAdapter.viewModel = viewModel
        val demoFragmentRecyclerView =
            view.findViewById<RecyclerView>(R.id.demoFragment_recyclerView)
        demoFragmentRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = WrapContentLinearLayoutManager(context)
            adapter = itemsAdapter
        }

        viewModel.data.observe(viewLifecycleOwner) {
            itemsAdapter.notifyItemInserted(0)
            demoFragmentRecyclerView.scrollToPosition(
                (demoFragmentRecyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            )
        }

        itemsAdapter.onItemClickedListener = this
    }

    override fun onItemClicked(item: DemoListItem) {
        when (item.type) {
            DemoListItemType.COLLECTED_DATA, DemoListItemType.COLLECTED_DATA_SENT -> {
                item.data?.let { data ->
                    val itemDetails = if (data is EventStreamLocation)
                        "type:${data.eventType}\nLat: ${data.latitude}\nLon:${data.longitude}\nAlt:${data.altitude}\nAcc:${data.accuracy}\nSpeed:${data.gpsSpeed}\nHeading:${data.gpsHeading}\n" +
                                "LAC:${data.lac}\nMNC:${data.mnc}\nMCC:${data.mcc}\nCID:${data.mobileCellId}"
                    else if (data is EventStreamLocationWithoutLocation)
                        "type:${data.eventType}\n" + "LAC:${data.lac}\nMNC:${data.mnc}\nMCC:${data.mcc}\nCID:${data.mobileCellId}"
                    else ""
                    Toast.makeText(requireContext(), itemDetails, Toast.LENGTH_LONG)
                        .show()
                }
            }
            DemoListItemType.DATA_SENT_TRY, DemoListItemType.DATA_SENT_TRY_SENT -> {
                item.sentIdsList?.let { ids ->
                    var sentItems = "Wysłane dataId:\n"
                    ids.forEach {
                        sentItems += if (it.contentEquals("-1")) " serwisowy;" else " ${it};"
                    }
                    Toast.makeText(requireContext(), sentItems, Toast.LENGTH_LONG)
                        .show()
                }
            }
            DemoListItemType.ERROR -> {
            }
            DemoListItemType.INFO -> {
            }
            DemoListItemType.START_ETOLL -> {
            }
            DemoListItemType.START_SENT -> {
            }
        }
    }
}