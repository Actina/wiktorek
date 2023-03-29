package pl.gov.mf.etoll.front.tecs.amountSelection

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.annotation.Keep
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import org.joda.time.DateTime
import pl.gov.mf.etoll.BR
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.commons.CurrencyUtils
import pl.gov.mf.etoll.commons.CurrencyUtils.formatAccountValue
import pl.gov.mf.etoll.commons.CurrencyUtils.toStringAmount
import pl.gov.mf.etoll.core.model.CoreAccountInfo
import pl.gov.mf.etoll.databinding.ItemTecsAmountInputBindingImpl
import pl.gov.mf.etoll.databinding.ItemTecsAmountsButtonsBinding
import pl.gov.mf.etoll.front.tecs.amountSelection.AmountOrderKey.*
import pl.gov.mf.etoll.front.tecs.amountSelection.TecsAmountSelectionItemType.*
import java.util.*
import javax.inject.Inject

class TecsAmountSelectionAdapter @Inject constructor(
    private val lifecycleOwner: LifecycleOwner,
) :
    RecyclerView.Adapter<TecsAmountSelectionViewHolder>() {

    private lateinit var viewModel: TecsAmountSelectionCellViewModel
    private lateinit var viewDataType: List<TecsAmountSelectionItemType>
    private var inputInitialized = false
    private var invalidateBindings: Boolean = false

    private var callbacks: TecsAmountSelectionAdapterCallbacks? = null

    fun setCallbacks(callbacks: TecsAmountSelectionAdapterCallbacks) {
        this.callbacks = callbacks
    }

    fun clearCallbacks() {
        this.callbacks = null
    }

    fun initialize(
        accountInfo: CoreAccountInfo,
        possibleValues: EnumMap<AmountOrderKey, PossibleAmount>,
    ) {
        inputInitialized = false
        val lastUpdateTime =
            DateTime(accountInfo.balance!!.updateAtTimestamp)
        val formattedTime = lastUpdateTime.formatForTime()
        val formattedDate = lastUpdateTime.formatForDate()
        viewModel = TecsAmountSelectionCellViewModel(
            inputValue = "",
            suggestedAmounts = possibleValues,
            inputHintValue = Pair(
                "top_up_account_hint_android",
                possibleValues[LOWEST]?.amount ?: ""
            ),
            accountValue = if (accountInfo.balance != null && accountInfo.balance!!.isInitialized) accountInfo.balance!!.value.formatAccountValue() else "-",
            lastPaymentHour = formattedTime,
            lastPaymentDate = formattedDate
        )

        viewDataType = mutableListOf<TecsAmountSelectionItemType>().apply {
            add(ACCOUNT_SUMMARY)
            add(AMOUNT_HEADER)
            add(INPUT)
            add(AMOUNTS)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): TecsAmountSelectionViewHolder {
        val layoutId = when (TecsAmountSelectionItemType.from(viewType)) {
            ACCOUNT_SUMMARY -> R.layout.item_tecs_amount_summary
            AMOUNT_HEADER -> R.layout.item_tecs_amount_header
            INPUT -> R.layout.item_tecs_amount_input
            AMOUNTS -> R.layout.item_tecs_amounts_buttons
        }
        val binding: ViewDataBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                layoutId,
                parent,
                false
            )
        binding.lifecycleOwner = lifecycleOwner

        return TecsAmountSelectionViewHolder(binding)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(
        holder: TecsAmountSelectionViewHolder,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        if (holder.binding is ItemTecsAmountInputBindingImpl) {
            with((holder.binding.tecsAmountInputField as AppCompatEditText)) {
                filters = arrayOf(CurrencyUtils.CurrencyInputFilter())

                if (!inputInitialized) {
                    val defaultAmount =
                        viewModel.suggestedAmounts[LOWEST]?.amount ?: ""
                    callbacks?.onCellSelected(defaultAmount)
                    setText(defaultAmount)

                    inputInitialized = true
                } else if (payloads.size == 1 && payloads[0] is String) {
                    val amountFromButton: String = payloads[0] as String
                    callbacks?.onCellSelected(amountFromButton)
                    setText(amountFromButton)
                    setSelection(text.toString().length)
                }

                addTextChangedListener {
                    try {
                        it?.let { editable ->
                            val value = editable.toString().toStringAmount()
                            viewModel.inputValue = value
                            callbacks?.onCellSelected(value)
                        }
                    } catch (_: Exception) {
                        viewModel.inputValue = ""
                        callbacks?.onCellSelected("")
                    }
                }
                setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        callbacks?.onImeActionDone()
                        return@OnEditorActionListener true
                    }
                    false
                })
            }
        }

        if (holder.binding is ItemTecsAmountsButtonsBinding) {
            with(holder.binding) {
                val inputCellPosition = position - 1
                lowestAmount.setOnClickListener {
                    onAmountItemSelected(inputCellPosition, LOWEST)
                }
                lowMidleAmount.setOnClickListener {
                    onAmountItemSelected(inputCellPosition, LOW_MIDDLE)
                }
                highMiddleAmount.setOnClickListener {
                    onAmountItemSelected(inputCellPosition, HIGH_MIDDLE)
                }
                highestAmount.setOnClickListener {
                    onAmountItemSelected(inputCellPosition, HIGHEST)
                }
            }
        }

        holder.bind(viewModel)
        invalidateBindings(holder, position)
    }

    override fun onBindViewHolder(holder: TecsAmountSelectionViewHolder, position: Int) {
    }

    private fun invalidateBindings(
        holder: TecsAmountSelectionViewHolder,
        position: Int,
    ) {
        if (invalidateBindings) {
            holder.binding.invalidateAll()
            if (position == itemCount - 1) {
                invalidateBindings = false
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun notifyBindingsChanged() {
        invalidateBindings = true
        notifyDataSetChanged()
    }

    private fun onAmountItemSelected(
        positionOfInputCell: Int,
        amountOrderKey: AmountOrderKey,
    ) {
        if (positionOfInputCell < 0 || getItemViewType(positionOfInputCell) != INPUT.ordinal) {
            Log.e(TECS_ADAPTER_DEBUG, "Bad input cell position")
        }
        viewModel.suggestedAmounts[amountOrderKey]?.run {
            notifyItemChanged(positionOfInputCell, amount)
        }
    }

    override fun getItemViewType(position: Int): Int = viewDataType[position].ordinal

    override fun getItemCount(): Int = viewDataType.size

    companion object {
        const val TECS_ADAPTER_DEBUG = "TECS_ADAPTER_DEBUG"
    }
}

private fun DateTime.formatForDate(): String = this.toString("dd.MM.yyyy")

private fun DateTime.formatForTime(): String = this.toString("HH:mm")

class TecsAmountSelectionCellViewModel(
    var inputValue: String = "",
    var suggestedAmounts: EnumMap<AmountOrderKey, PossibleAmount> = EnumMap(AmountOrderKey::class.java),
    val inputHintValue: Pair<String, String>?,
    var accountValue: String = "zł",
    var lastPaymentHour: String = "",
    var lastPaymentDate: String = "",
) {

    fun getSuggestedAmountWithCurrency(amountOrderKey: AmountOrderKey): String? =
        suggestedAmounts[amountOrderKey]?.withCurrency()
}

class TecsAmountSelectionViewHolder(val binding: ViewDataBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(item: TecsAmountSelectionCellViewModel) {
        binding.setVariable(BR.item, item)
        if (binding.hasPendingBindings()) {
            binding.executePendingBindings()
        }
    }
}

enum class AmountOrderKey {
    @Keep
    LOWEST,

    @Keep
    LOW_MIDDLE,

    @Keep
    HIGH_MIDDLE,

    @Keep
    HIGHEST
}

enum class TecsAmountSelectionItemType {
    ACCOUNT_SUMMARY, AMOUNT_HEADER, INPUT, AMOUNTS;

    companion object {
        fun from(value: Int) = values()[value]
    }
}

interface TecsAmountSelectionAdapterCallbacks {
    fun onCellSelected(amount: String)
    fun onImeActionDone()
}
