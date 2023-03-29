package pl.gov.mf.mobile.ui.components.dialogs.basic

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.etoll.ui.components.databinding.DialogBaseTwoactionsBinding
import pl.gov.mf.mobile.ui.components.dialogs.BlurableWithSoundDatabindingDialogFragment
import pl.gov.mf.mobile.utils.JsonConvertible
import pl.gov.mf.mobile.utils.toObject

abstract class BasicTwoActionsDialogFragment : BlurableWithSoundDatabindingDialogFragment() {

    private var binding: DialogBaseTwoactionsBinding? = null

    private val _dialogResult = MutableLiveData<DialogResult>()
    val dialogResult: LiveData<DialogResult>
        get() = _dialogResult

    val viewModel = object : DialogViewModel(
    ) {
        override fun onPositiveButtonPressed() {
            _dialogResult.value = DialogResult.CONFIRMED
            dismiss()
        }

        override fun onNegativeButtonPressed() {
            _dialogResult.value = DialogResult.CANCELLED
            dismiss()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(
                context
            ), R.layout.dialog_base_twoactions, null, false
        )
        binding!!.viewModelSchema = viewModel
        dialog?.setCanceledOnTouchOutside(false)
        return binding!!.root
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (_dialogResult.value == null)
            _dialogResult.value = DialogResult.CANCELLED
    }

    override fun getBindings(): ViewDataBinding? = binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            // restore state
            it.getString(KEY_SAVED_DATA)?.let { state ->
                viewModel.restoreFromState(state)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // save state
        outState.putString(KEY_SAVED_DATA, viewModel.generateStateString())
    }

    abstract class DialogViewModel {
        private val _contentText: MutableLiveData<String> = MutableLiveData()
        val contentText: LiveData<String>
            get() = _contentText
        private val _headerText: MutableLiveData<String> = MutableLiveData()
        val headerText: LiveData<String>
            get() = _headerText
        private val _buttonText: MutableLiveData<String> = MutableLiveData()
        val buttonText: LiveData<String>
            get() = _buttonText
        private val _cancelButtonText: MutableLiveData<String> = MutableLiveData()
        val cancelButtonText: LiveData<String>
            get() = _cancelButtonText
        private val _headerTextColor: MutableLiveData<String> = MutableLiveData()
        val headerTextColor: LiveData<String>
            get() = _headerTextColor
        private val _buttonStyleRed: MutableLiveData<Boolean> = MutableLiveData(false)
        val buttonStyleRed: LiveData<Boolean>
            get() = _buttonStyleRed
        private val _buttonStyleBlue: MutableLiveData<Boolean> = MutableLiveData(false)
        val buttonStyleBlue: LiveData<Boolean>
            get() = _buttonStyleBlue
        private val _buttonStyleGreen: MutableLiveData<Boolean> = MutableLiveData(false)
        val buttonStyleGreen: LiveData<Boolean>
            get() = _buttonStyleGreen
        private val _cancelButtonStyleRedOutlined: MutableLiveData<Boolean> = MutableLiveData(false)
        val cancelButtonStyleRedOutlined: LiveData<Boolean>
            get() = _cancelButtonStyleRedOutlined
        private val _cancelButtonStyleRed: MutableLiveData<Boolean> = MutableLiveData(false)
        val cancelButtonStyleRed: LiveData<Boolean>
            get() = _cancelButtonStyleRed
        private val _cancelButtonStyleBlue: MutableLiveData<Boolean> = MutableLiveData(false)
        val cancelButtonStyleBlue: LiveData<Boolean>
            get() = _cancelButtonStyleBlue
        private val _cancelButtonStyleGreen: MutableLiveData<Boolean> = MutableLiveData(false)
        val cancelButtonStyleGreen: LiveData<Boolean>
            get() = _cancelButtonStyleGreen
        private val _verticalButtons: MutableLiveData<Boolean> = MutableLiveData(false)
        val verticalButtons: LiveData<Boolean> = _verticalButtons
        private val _horizontalButtons: MutableLiveData<Boolean> = MutableLiveData(false)
        val horizontalButtons: LiveData<Boolean> = _horizontalButtons
        private val _iconResource: MutableLiveData<String> = MutableLiveData()
        val iconResource: LiveData<String>
            get() = _iconResource

        abstract fun onPositiveButtonPressed()

        abstract fun onNegativeButtonPressed()

        fun configure(
            startHeaderText: String,
            startContentText: String,
            startButtonText: String,
            startCancelButtonText: String,
            startIconResName: String?,
            startButtonStyle: ButtonStyles,
            startCancelButtonStyle: ButtonStyles,
            startHeaderColorName: String = "spoeTextContent",
            horizontalButtonsVisible: Boolean = true,
            verticalButtonsVisible: Boolean = false,
        ) {
            _contentText.value = (startContentText)
            _headerText.value = (startHeaderText)
            _buttonText.value = (startButtonText)
            _cancelButtonText.value = (startCancelButtonText)
            _iconResource.value = (startIconResName)
            _horizontalButtons.value = (horizontalButtonsVisible)
            _verticalButtons.value = (verticalButtonsVisible)
            startHeaderColorName?.let { _headerTextColor.value = (startHeaderColorName) }
            when (startButtonStyle) {
                ButtonStyles.BLUE -> _buttonStyleBlue.value = (true)
                ButtonStyles.RED -> _buttonStyleRed.value = (true)
                ButtonStyles.GREEN -> _buttonStyleGreen.value = (true)
                else -> {
                }
            }
            when (startCancelButtonStyle) {
                ButtonStyles.BLUE -> _cancelButtonStyleBlue.value = (true)
                ButtonStyles.RED -> _cancelButtonStyleRed.value = (true)
                ButtonStyles.GREEN -> _cancelButtonStyleGreen.value = (true)
                ButtonStyles.RED_OUTLINED -> _cancelButtonStyleRedOutlined.value = (true)
            }
        }

        fun restoreFromState(stateString: String) {
            try {
                // as this could be malformed string, we need to be ready for it
                val state = stateString.toObject<DialogState>()
                configure(
                    startHeaderText = state.headerText,
                    startContentText = state.contentText,
                    startButtonText = state.buttonText,
                    startCancelButtonText = state.cancelButtonText,
                    startIconResName = state.iconResName,
                    startButtonStyle = ButtonStyles.values()[state.buttonStyle],
                    startCancelButtonStyle = ButtonStyles.values()[state.cancelButtonStyle],
                    startHeaderColorName = state.headerColorName,
                    horizontalButtonsVisible = state.horizontalButtonsVisible,
                    verticalButtonsVisible = state.verticalButtonsVisible
                )
            } catch (_: Exception) {
                // do nothing
            }
        }

        fun generateStateString(): String = DialogState(
            headerText = headerText.value ?: "",
            contentText = contentText.value ?: "",
            buttonText = buttonText.value ?: "",
            cancelButtonText = cancelButtonText.value ?: "",
            iconResName = iconResource.value ?: "",
            buttonStyle = getButtonStyle(),
            cancelButtonStyle = getCancelButtonStyle(),
            headerColorName = headerTextColor.value ?: "spoeTextContent",
            horizontalButtonsVisible = horizontalButtons.value ?: true,
            verticalButtonsVisible = verticalButtons.value ?: false
        ).toJSON()

        private fun getButtonStyle(): Int {
            val style =
                if (buttonStyleBlue.value == true) ButtonStyles.BLUE
                else if (buttonStyleGreen.value == true) ButtonStyles.GREEN else ButtonStyles.RED
            return style.ordinal
        }

        private fun getCancelButtonStyle(): Int {
            val style =
                if (cancelButtonStyleBlue.value == true) ButtonStyles.BLUE
                else if (cancelButtonStyleGreen.value == true) ButtonStyles.GREEN
                else if (cancelButtonStyleRed.value == true) ButtonStyles.RED
                else ButtonStyles.RED_OUTLINED
            return style.ordinal
        }
    }

    data class DialogState(
        val headerText: String,
        val contentText: String,
        val buttonText: String,
        val cancelButtonText: String,
        val iconResName: String?,
        val buttonStyle: Int,
        val cancelButtonStyle: Int,
        val headerColorName: String,
        val horizontalButtonsVisible: Boolean,
        val verticalButtonsVisible: Boolean,
    ) : JsonConvertible

    enum class DialogResult {
        CONFIRMED, CANCELLED
    }

    enum class ButtonStyles {
        BLUE, RED, GREEN, RED_OUTLINED
    }

    companion object {
        private const val KEY_SAVED_DATA = "SAVED_DATA"
    }
}