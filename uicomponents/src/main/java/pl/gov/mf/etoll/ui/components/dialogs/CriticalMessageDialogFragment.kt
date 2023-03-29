package pl.gov.mf.etoll.ui.components.dialogs

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pl.gov.mf.etoll.interfaces.*
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.etoll.ui.components.databinding.DialogCriticalMessageBinding
import pl.gov.mf.mobile.ui.components.dialogs.BlurableWithSoundDatabindingDialogFragment
import pl.gov.mf.mobile.utils.JsonConvertible
import pl.gov.mf.mobile.utils.toObject

class CriticalMessageDialogFragment : BlurableWithSoundDatabindingDialogFragment() {

    private var binding: DialogCriticalMessageBinding? = null

    private val _dialogResult = MutableLiveData<Boolean>()
    val dialogResult: LiveData<Boolean>
        get() = _dialogResult

    private val viewModel = object : CriticalMessageDialogViewModelSchema {
        lateinit var mode: CriticalMessageState
        override val icon: String
            get() {
                return when (mode) {
                    is BatteryState -> if ((mode as BatteryState).batterySignal == WarningsBasicLevels.RED)
                        "ic_battery_level_red" else "ic_battery_level_yellow"
                    is GpsState -> if ((mode as GpsState).gpsSignal == WarningsBasicLevels.RED)
                        "ic_gps_data_red" else "ic_gps_data_yellow"
                    is DataConnectionState -> if ((mode as DataConnectionState).dataConnectionSignal == WarningsBasicLevels.RED)
                        "ic_network_communication_red" else "ic_network_communication_yellow"
                    else -> ""
                }
            }

        override val textColor: String
            get() = when (mode) {
                is BatteryState ->
                    if ((mode as BatteryState).batterySignal == WarningsBasicLevels.RED) "warningRedText" else "warningYellowText"
                is GpsState -> if ((mode as GpsState).gpsSignal == WarningsBasicLevels.RED) "warningRedText" else "warningYellowText"
                is DataConnectionState -> if ((mode as DataConnectionState).dataConnectionSignal == WarningsBasicLevels.RED) "warningRedText" else "warningYellowText"
            }

        override val headerText: String
            get() = when (mode) {
                is BatteryState -> if ((mode as BatteryState).batterySignal == WarningsBasicLevels.RED) "critical_messages_title" else "critical_messages_title"
                is GpsState -> if ((mode as GpsState).gpsSignal == WarningsBasicLevels.RED) "critical_messages_title" else "critical_messages_title"
                is DataConnectionState -> if ((mode as DataConnectionState).dataConnectionSignal == WarningsBasicLevels.RED) "critical_messages_title" else "critical_messages_title"
            }

        override val contentText: String
            get() = when (mode) {
                is BatteryState -> if ((mode as BatteryState).batterySignal == WarningsBasicLevels.RED) "critical_messages_low_battery" else "critical_messages_medium_battery"
                is GpsState -> if ((mode as GpsState).gpsSignal == WarningsBasicLevels.RED) "critical_messages_low_gps" else "critical_messages_medium_gps"
                is DataConnectionState -> if ((mode as DataConnectionState).dataConnectionSignal == WarningsBasicLevels.RED) "critical_messages_low_data_connection" else "critical_messages_medium_data_connection"
            }

        override val frameDrawable: String
            get() = when (mode) {
                is BatteryState ->
                    if ((mode as BatteryState).batterySignal == WarningsBasicLevels.RED) "bg_framed_red" else "bg_framed_yellow"
                is GpsState ->
                    if ((mode as GpsState).gpsSignal == WarningsBasicLevels.RED) "bg_framed_red" else "bg_framed_yellow"
                is DataConnectionState ->
                    if ((mode as DataConnectionState).dataConnectionSignal == WarningsBasicLevels.RED) "bg_framed_red" else "bg_framed_yellow"
            }

        override fun onContinue() {
            _dialogResult.value = true
            dismiss()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_MODEL, viewModel.mode.toJson())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            if (it.containsKey(KEY_MODEL))
                viewModel.mode =
                    it.getString(KEY_MODEL)!!.toObject<CriticalMessageStateJson>().toOriginalModel()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(
                context
            ), R.layout.dialog_critical_message, null, false
        )
        binding!!.viewModelSchema = viewModel
        dialog?.setCanceledOnTouchOutside(false)
        return binding!!.root
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        _dialogResult.value = true
    }

    override fun getBindings(): ViewDataBinding? = binding

    companion object {
        private const val KEY_MODEL = "MODEL"
        const val TAG = "CRITICAL_MSG"
        fun createAndShow(fm: FragmentManager, mode: CriticalMessageState) =
            CriticalMessageDialogFragment().apply {
                viewModel.mode = mode
                isCancelable = false
                show(fm, TAG)
            }
    }

    interface CriticalMessageDialogViewModelSchema {
        fun onContinue()
        val icon: String
        val textColor: String
        val headerText: String
        val contentText: String
        val frameDrawable: String
    }
}

data class CriticalMessageStateJson(val cause: Int, val lvl: Int) :
    JsonConvertible {
    fun toOriginalModel(): CriticalMessageState =
        when (cause) {
            0 -> BatteryState(lvl.toWarningLvl())
            1 -> GpsState(lvl.toWarningLvl())
            2 -> DataConnectionState(lvl.toWarningLvl())
            else -> BatteryState(WarningsBasicLevels.UNKNOWN)
        }
}

private fun Int.toWarningLvl(): WarningsBasicLevels =
    when (this) {
        3 -> WarningsBasicLevels.GREEN
        2 -> WarningsBasicLevels.YELLOW
        1 -> WarningsBasicLevels.RED
        else -> WarningsBasicLevels.UNKNOWN
    }

private fun CriticalMessageState.toJson(): String = when (this) {
    is BatteryState ->
        CriticalMessageStateJson(
            0,
            this.batterySignal.toInt()
        ).toJSON()
    is GpsState ->
        CriticalMessageStateJson(1, this.gpsSignal.toInt()).toJSON()
    is DataConnectionState ->
        CriticalMessageStateJson(2, this.dataConnectionSignal.toInt()).toJSON()
    else -> ""
}

private fun WarningsBasicLevels.toInt(): Int =
    when (this) {
        WarningsBasicLevels.GREEN -> 3
        WarningsBasicLevels.YELLOW -> 2
        WarningsBasicLevels.RED -> 1
        WarningsBasicLevels.UNKNOWN -> 0
        else -> 0
    }
