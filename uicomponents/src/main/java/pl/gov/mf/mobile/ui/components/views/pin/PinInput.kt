package pl.gov.mf.mobile.ui.components.views.pin

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.mobile.ui.components.utils.callbacks.SimpleTextChangeListener
import pl.gov.mf.mobile.ui.components.utils.hideKeyboard
import pl.gov.mf.mobile.ui.components.utils.showKeyboard

class PinInput @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr), LifecycleObserver {

    private lateinit var pinBox1: PinBox
    private lateinit var pinBox2: PinBox
    private lateinit var pinBox3: PinBox
    private lateinit var pinBox4: PinBox
    private lateinit var pinInput: EditText

    private companion object {
        private const val ELEMENT_NOT_FOUND = -1
        private const val PIN_LENGTH = 4
    }

    var isLocked: Boolean = false
        set(value) {
            field = value

            if (value) lockPinInput()
            else unlockPinInput()
        }

    private var onPinReady: () -> Unit = {}
    private var onPinChanged: () -> Unit = {}

    private val pinBoxes: List<PinBox>

    fun invalidateUncommonItems() {
        pinBox1.invalidateItem()
        pinBox2.invalidateItem()
        pinBox3.invalidateItem()
        pinBox4.invalidateItem()
    }

    private val textWatcher = object : SimpleTextChangeListener() {
        override fun onTextChanged(
            text: CharSequence?,
            changeBeginningPosition: Int,
            lengthOfOldCharSequence: Int,
            lengthOfNewCharSequence: Int,
        ) {
            onPinChanged()

            text?.let {
                if (lengthOfNewCharSequence > 0) {
                    setNextPinBoxAsFilled(it)
                    notifyIfPinIsFullyEntered()
                } else {
                    clearLastPinBox()
                }
            }
        }
    }

    init {
        val view: View = LayoutInflater.from(context)
            .inflate(R.layout.view_pin_input, this, true)
        setupView(view)
        pinBoxes = listOf(
            pinBox1,
            pinBox2,
            pinBox3,
            pinBox4
        )

        setOnClickListener {
            pinInput.showKeyboard()
            markFirstNotFilledBoxAsSelected()
        }

        pinInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus)
                clearSelection()
        }
    }

    private fun setupView(view: View) {
        pinBox1 = findViewById(R.id.pin_box_1)
        pinBox2 = findViewById(R.id.pin_box_2)
        pinBox3 = findViewById(R.id.pin_box_3)
        pinBox4 = findViewById(R.id.pin_box_4)
        pinInput = findViewById(R.id.pin_input)
    }

    private fun clearSelection() {
        pinBoxes.forEach { pb -> pb.isSelected = false }
    }

    var pin: String
        get() = pinInput.text.toString()
        set(value) {
            pinInput.setText(value)
            if (value.isEmpty())
                clearPin()
        }

    fun setOnPinChanged(listener: () -> Unit) {
        onPinChanged = listener
    }

    fun setOnPinReady(listener: () -> Unit) {
        onPinReady = listener
    }

    fun clearPin() {
        if (pin.isNotEmpty())
            pin = ""
        pinBoxes.forEach { it.reset() }
    }

    fun lockPinInput() {
        clearPin()
        isEnabled = false
        pinBoxes.forEach { it.isLocked = true }
    }

    fun unlockPinInput() {
        isEnabled = true
        pinBoxes.forEach { it.isLocked = false }
    }

    private fun markFirstNotFilledBoxAsSelected() {
        val index = pinBoxes.indexOfLast { pb -> pb.isFilled }

        if (index + 1 < PIN_LENGTH)
            pinBoxes[index + 1].isSelected = true
    }

    private fun clearLastPinBox() {
        val indexOfLastFilledSingleNumber = pinBoxes.indexOfLast { pb -> pb.isFilled }

        if (indexOfLastFilledSingleNumber != ELEMENT_NOT_FOUND) {
            pinBoxes[indexOfLastFilledSingleNumber].run {
                isFilled = false
                isSelected = true
            }

            setSelectionValueFor(indexOfLastFilledSingleNumber + 1, false)
        }
    }

    private fun setNextPinBoxAsFilled(pin: CharSequence) {
        val indexOfFirstEmptySingleNumber = pinBoxes.indexOfFirst { pb -> pb.isFilled.not() }

        if (indexOfFirstEmptySingleNumber != ELEMENT_NOT_FOUND) {
            pinBoxes[indexOfFirstEmptySingleNumber].run {
                isFilled = true
                isSelected = false
                setDigit(pin.last())
            }

            setSelectionValueFor(indexOfFirstEmptySingleNumber + 1, true)
        }
    }

    private fun notifyIfPinIsFullyEntered() {
        if (pinInput.text.length == PIN_LENGTH)
            onPinReady()
    }

    private fun setSelectionValueFor(index: Int, value: Boolean) {
        if (index < PIN_LENGTH)
            pinBoxes[index].isSelected = value
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onResume() = pinInput.addTextChangedListener(textWatcher)

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun onPause() {
        pinInput.removeTextChangedListener(textWatcher)
        pinInput.hideKeyboard()
    }
}