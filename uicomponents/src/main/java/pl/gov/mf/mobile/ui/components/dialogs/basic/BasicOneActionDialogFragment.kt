package pl.gov.mf.mobile.ui.components.dialogs.basic

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pl.gov.mf.etoll.app.BaseApplication
import pl.gov.mf.etoll.appmode.AppMode
import pl.gov.mf.etoll.interfaces.NotificationHistoryController
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.etoll.ui.components.databinding.DialogBaseOneactionBinding
import pl.gov.mf.etoll.ui.components.databinding.DialogBaseOneactionExpandedBinding
import pl.gov.mf.mobile.ui.components.dialogs.BlurableWithSoundDatabindingDialogFragment
import pl.gov.mf.mobile.utils.JsonConvertible
import pl.gov.mf.mobile.utils.toObject

abstract class BasicOneActionDialogFragment : BlurableWithSoundDatabindingDialogFragment() {

    private var binding: DialogBaseOneactionExpandedBinding? = null

    private val _dialogResult = MutableLiveData<DialogResult>()
    val dialogResult: LiveData<DialogResult>
        get() = _dialogResult

    val viewModel = object : DialogViewModel(
    ) {
        override fun onButtonPressed() {
            _dialogResult.value = DialogResult.CONFIRMED
            dismiss()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (getTypeForHistory() != null)
            (context.applicationContext as BaseApplication).getApplicationComponent()
                .useCaseAddNotificationToHistory().execute(
                    getTypeForHistory()!!,
                    viewModel.headerText.value!!,
                    viewModel.contentText.value!!,
                    viewModel.iconResource.value,
                    getPayloadForHistory()
                ).subscribe()
    }

    /**
     * Get item type that will be added to history - if it's null, then dialog is not added to history
     */
    open fun getTypeForHistory(): NotificationHistoryController.Type? = null

    /**
     * Get optional payload to revoke full dialog from history if required
     */
    open fun getPayloadForHistory(): String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = if (viewModel.contentStyle == ContentStyles.EXPANDED) {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(
                context
            ), R.layout.dialog_base_oneaction_expanded, null, false
        )
        binding!!.viewModelSchema = viewModel
        dialog?.setCanceledOnTouchOutside(false)
        binding!!.root
    } else {
        val binding: DialogBaseOneactionBinding = DataBindingUtil.inflate(
            LayoutInflater.from(
                context
            ), R.layout.dialog_base_oneaction, null, false
        )
        binding.viewModelSchema = viewModel
        dialog?.setCanceledOnTouchOutside(false)
        binding.root
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (_dialogResult.value == null || _dialogResult.value != DialogResult.CONFIRMED)
            _dialogResult.value = DialogResult.CONFIRMED
    }

    override fun getBindings(): ViewDataBinding? = binding

    override fun invalidateViewAfterModeChange() {
        super.invalidateViewAfterModeChange()
        viewModel.iconResource.value?.let {
            val darkMode =
                (requireContext().applicationContext as BaseApplication).getApplicationComponent()
                    .useCaseGetCurrentAppMode().execute() == AppMode.DARK_MODE
            viewModel._iconResource.value = it.mapDrawableByMode(darkMode)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // dirty bugfix for refreshing dialog icons/txt to mode different than one declared in configure
        invalidateViewAfterModeChange()
    }

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
        private val _headerTextColor: MutableLiveData<String> = MutableLiveData()
        val headerTextColor: LiveData<String>
            get() = _headerTextColor
        private val _buttonStyleBlue: MutableLiveData<Boolean> = MutableLiveData(true)
        val buttonStyleBlue: LiveData<Boolean>
            get() = _buttonStyleBlue
        internal val _iconResource: MutableLiveData<Int> = MutableLiveData()
        val iconResource: LiveData<Int>
            get() = _iconResource
        private val _iconVisible: MutableLiveData<Boolean> = MutableLiveData()
        val iconVisible: LiveData<Boolean> = _iconVisible

        var contentStyle: ContentStyles = ContentStyles.STANDARD

        abstract fun onButtonPressed()

        fun configure(
            startHeaderText: String,
            startContentText: String,
            startButtonText: String,
            startIconRes: Int = R.drawable.ic_warning_light,
            startButtonStyle: ButtonStyles,
            startHeaderColor: String = "spoeTextContent",
            contentStyle: ContentStyles = ContentStyles.STANDARD,
            iconVisible: Boolean = true
        ) {
            this.contentStyle = contentStyle
            _contentText.value = startContentText
            _headerText.value = startHeaderText
            _buttonText.value = startButtonText
            _iconResource.value = startIconRes
            _iconVisible.value = iconVisible
            startHeaderColor?.let { _headerTextColor.value = startHeaderColor }
            when (startButtonStyle) {
                ButtonStyles.BLUE -> _buttonStyleBlue.postValue(true)
            }
        }

        fun generateStateString(): String =
            DialogState(
                headerText = headerText.value ?: "",
                contentText = contentText.value ?: "",
                buttonText = buttonText.value ?: "",
                iconRes = iconResource.value ?: R.drawable.ic_warning_light,
                buttonStyle = buttonStyleBlue.value ?: false,
                headerColor = headerTextColor.value ?: "spoeTextContent",
                contentStyle = contentStyle.ordinal,
                iconVisible = iconVisible.value ?: true
            ).toJSON()

        fun restoreFromState(stateString: String) {
            try {
                // as this could be malformed string, we need to be ready for it
                val state = stateString.toObject<DialogState>()
                configure(
                    startHeaderText = state.headerText,
                    startContentText = state.contentText,
                    startButtonText = state.buttonText,
                    startIconRes = state.iconRes,
                    startButtonStyle = ButtonStyles.BLUE, // what for we keep it as we support only 1 type?
                    startHeaderColor = state.headerColor,
                    contentStyle = ContentStyles.values()[state.contentStyle],
                    iconVisible = state.iconVisible
                )
            } catch (_: Exception) {
                // do nothing
            }
        }
    }

    data class DialogState(
        val headerText: String,
        val contentText: String,
        val buttonText: String,
        val iconRes: Int,
        val buttonStyle: Boolean,
        val headerColor: String,
        val contentStyle: Int,
        val iconVisible: Boolean,
    ) : JsonConvertible

    enum class DialogResult {
        CONFIRMED
    }

    enum class ButtonStyles {
        BLUE
    }

    enum class ContentStyles {
        STANDARD, EXPANDED
    }

    companion object {
        private const val KEY_SAVED_DATA = "SAVED_DATA"
    }
}

fun Int.mapDrawableByMode(darkMode: Boolean): Int {
    // warning, this is a risky function, but no other option available atm without altering db
    if (this == R.drawable.ic_confirm_finish_ride_light || this == R.drawable.ic_confirm_finish_ride_dark) {
        if (darkMode)
            return R.drawable.ic_confirm_finish_ride_dark
        else
            return R.drawable.ic_confirm_finish_ride_light
    }
    if (this == R.drawable.ic_error_light || this == R.drawable.ic_error_dark) {
        if (darkMode)
            return R.drawable.ic_error_dark
        else
            return R.drawable.ic_error_light
    }
    if (this == R.drawable.ic_warning_light || this == R.drawable.ic_warning_dark) {
        if (darkMode)
            return R.drawable.ic_warning_dark
        else
            return R.drawable.ic_warning_light
    }
    if (this == R.drawable.ic_autoconfig_light || this == R.drawable.ic_autoconfig_dark) {
        if (darkMode)
            return R.drawable.ic_autoconfig_dark
        else
            return R.drawable.ic_autoconfig_light
    }
    if (this == R.drawable.ic_network_error_light || this == R.drawable.ic_network_error_dark) {
        if (darkMode)
            return R.drawable.ic_network_error_dark
        else
            return R.drawable.ic_network_error_light
    }
    if (this == R.drawable.ic_confirm_finish_ride_light || this == R.drawable.ic_confirm_finish_ride_dark) {
        if (darkMode)
            return R.drawable.ic_confirm_finish_ride_dark
        else
            return R.drawable.ic_confirm_finish_ride_light
    }
    if (this == R.drawable.ic_wrongpin_light || this == R.drawable.ic_wrongpin_dark) {
        if (darkMode)
            return R.drawable.ic_wrongpin_dark
        else
            return R.drawable.ic_wrongpin_light
    }
    if (this == R.drawable.ic_tecs_amount_too_high_light || this == R.drawable.ic_tecs_amount_too_high_dark) {
        if (darkMode)
            return R.drawable.ic_tecs_amount_too_high_dark
        else
            return R.drawable.ic_tecs_amount_too_high_light
    }
    if (this == R.drawable.ic_tecs_amount_too_low_light || this == R.drawable.ic_tecs_amount_too_low_dark) {
        if (darkMode)
            return R.drawable.ic_tecs_amount_too_low_dark
        else
            return R.drawable.ic_tecs_amount_too_low_light
    }
    if (this == R.drawable.ic_warning_red_light || this == R.drawable.ic_warning_red_dark) {
        if (darkMode)
            return R.drawable.ic_warning_red_dark
        else
            return R.drawable.ic_warning_red_light
    }
    if (this == R.drawable.ic_autoconfig_light || this == R.drawable.ic_autoconfig_dark) {
        if (darkMode)
            return R.drawable.ic_autoconfig_dark
        else
            return R.drawable.ic_autoconfig_light
    }

    // fallback
    return if (darkMode) R.drawable.ic_warning_dark else R.drawable.ic_warning_light
}
