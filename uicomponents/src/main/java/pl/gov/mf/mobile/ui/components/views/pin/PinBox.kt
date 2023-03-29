package pl.gov.mf.mobile.ui.components.views.pin

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.mobile.utils.toColorInMode
import pl.gov.mf.mobile.utils.toDrawableIdInMode

class PinBox @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private val stateValueHidden = intArrayOf(R.attr.state_value_hidden)
    private val stateLocked = intArrayOf(R.attr.state_locked)

    fun invalidateItem() {
        setTextColor("pinText".toColorInMode(context))
        setBackgroundResource("bg_pin_box".toDrawableIdInMode(context))
    }

    private val hideTextRunnable = Runnable {
        text = ""
        isValueHidden = true
    }

    private var isValueHidden = false
        set(value) {
            field = value

            refreshDrawableState()
        }

    var isFilled: Boolean = false
        set(value) {
            field = value

            if (!value)
                isValueHidden = false
        }

    var isLocked: Boolean = false
        set(value) {
            field = value

            if (value)
                reset()

            refreshDrawableState()
        }

    fun setDigit(digit: Char) {
        text = digit.toString()

        handler?.postDelayed(hideTextRunnable, 200)
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 2)

        if (isValueHidden) {
            mergeDrawableStates(drawableState, stateValueHidden)
        }

        if (isLocked)
            mergeDrawableStates(drawableState, stateLocked)
        return drawableState
    }

    fun reset() {
        handler?.removeCallbacks(hideTextRunnable)
        isFilled = false
        isSelected = false
        isValueHidden = false
        text = ""
    }
}