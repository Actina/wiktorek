package pl.gov.mf.etoll.demo

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.joda.time.DateTime
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.commons.TimeUtils
import pl.gov.mf.etoll.front.watchdog.DemoListItem
import pl.gov.mf.etoll.front.watchdog.DemoListItemType
import pl.gov.mf.etoll.networking.api.model.EventStreamLocation
import pl.gov.mf.etoll.networking.api.model.EventStreamLocationWithoutLocation
import javax.inject.Inject

class DemoFragmentAdapter @Inject constructor() : RecyclerView.Adapter<DemoViewHolder>(),
    OnItemClickedListener {

    lateinit var viewModel: DemoFragmentViewModel

    var onItemClickedListener: OnItemClickedListener? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DemoViewHolder = DemoViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(DemoListItemType.values()[viewType].layout, parent, false)
    )

    override fun getItemViewType(position: Int): Int = viewModel.data.value!![position].type.ordinal

    override fun onBindViewHolder(
        holder: DemoViewHolder,
        position: Int
    ) {
        holder.bind(viewModel.data.value!![position], this)
    }

    override fun getItemCount(): Int {
        if (viewModel.data.value == null) return 0
        return viewModel.data.value!!.size
    }

    override fun onItemClicked(item: DemoListItem) {
        onItemClickedListener?.onItemClicked(item)
    }
}

class DemoViewHolder(private val parentView: View) : RecyclerView.ViewHolder(parentView) {

    private val errorTextView: TextView? = parentView.findViewById(R.id.row_errorTextView)
    private val infoTextView: TextView? = parentView.findViewById(R.id.row_infoTextView)
    private val sentCountTextView: TextView? =
        parentView.findViewById(R.id.row_packageSizeValueTextView)
    private val latitudeTextView: TextView? =
        parentView.findViewById(R.id.row_latitudeValueTextView)
    private val longitudeTextView: TextView? =
        parentView.findViewById(R.id.row_longitudeValueTextView)
    private val timestampTextView: TextView? =
        parentView.findViewById(R.id.row_timestampTextView)
    private val eventTypeTextView: TextView? =
        parentView.findViewById(R.id.row_typeTextView)

    @SuppressLint("SetTextI18n")
    fun bind(
        item: DemoListItem,
        listener: OnItemClickedListener
    ) {
        parentView.setOnClickListener { listener.onItemClicked(item) }
        when (item.type) {
            DemoListItemType.COLLECTED_DATA, DemoListItemType.COLLECTED_DATA_SENT -> {
                if (item.data is EventStreamLocation) {
                    eventTypeTextView?.text =
                        (if (item.type == DemoListItemType.COLLECTED_DATA_SENT) "SENT " else "") +
                                "Typ: ${item.data.eventType}  ID:${item.data.dataId}"
                    latitudeTextView?.text = "${item.data.latitude}"
                    longitudeTextView?.text = "${item.data.longitude}"
                    timestampTextView?.text =
                        TimeUtils.DefaultDateTimeFormatterForUi.print(
                            DateTime(item.data.fixTime / 1000)
                        )
                } else if (item.data is EventStreamLocationWithoutLocation) {
                    eventTypeTextView?.text =
                        "Typ: ${item.data.eventType}  ID:${item.data.dataId}"
                    latitudeTextView?.text = "-brak-"
                    longitudeTextView?.text = "-brak-"
                    timestampTextView?.text =
                        TimeUtils.DefaultDateTimeFormatterForUi.print(
                            DateTime(item.data.fixTime / 1000)
                        )
                } else {
                    // do nothing
                }
            }
            DemoListItemType.DATA_SENT_TRY, DemoListItemType.DATA_SENT_TRY_SENT -> {
                sentCountTextView?.text = "${item.amountOfSentData}"
                timestampTextView?.text = item.timestamp
            }
            DemoListItemType.ERROR -> {
                errorTextView?.text = item.error
                timestampTextView?.text = item.timestamp
            }
            DemoListItemType.INFO -> {
                infoTextView?.text = item.info
                timestampTextView?.text = item.timestamp
            }
            DemoListItemType.START_ETOLL -> {
                infoTextView?.text = item.info
                timestampTextView?.text = item.timestamp
            }
            DemoListItemType.START_SENT -> {
                infoTextView?.text = item.info
                timestampTextView?.text = item.timestamp
            }
        }
    }

}

interface OnItemClickedListener {
    fun onItemClicked(item: DemoListItem)
}