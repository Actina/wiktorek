package pl.gov.mf.mobile.ui.components.adapters

import android.util.Log
import androidx.databinding.BindingAdapter
import com.google.android.material.button.MaterialButton
import pl.gov.mf.mobile.ui.components.adapters.UiAdapters.setBackgroundTintColorInMode
import pl.gov.mf.mobile.ui.components.adapters.UiAdapters.setBackgroundTintList
import pl.gov.mf.mobile.ui.components.adapters.UiAdapters.setIconTintColorInMode
import pl.gov.mf.mobile.ui.components.adapters.UiAdapters.setTextViewCompoundDrawableTintList
import pl.gov.mf.mobile.ui.components.adapters.UiAdapters.setRippleColorInMode
import pl.gov.mf.mobile.ui.components.adapters.UiAdapters.setRippleList
import pl.gov.mf.mobile.ui.components.adapters.UiAdapters.setStrokeColorListInMode
import pl.gov.mf.mobile.ui.components.adapters.UiAdapters.setTextColorInMode
import pl.gov.mf.mobile.ui.components.adapters.UiAdapters.setTextColorListInMode

object BindingBasedStyles {

    private const val BUTTON_STANDARD = "buttonStandard"
    private const val BUTTON_WHITE_OUTLINED = "buttonWhiteOutlined"
    private const val BUTTON_WHITE_OUTLINED_WITH_ICON = "buttonWhiteOutlinedWithIcon"
    private const val STYLES_LOGGER = "styles_logger"

    @BindingAdapter(value =
    [
        "$BUTTON_WHITE_OUTLINED_WITH_ICON:backgroundTintColorInMode",
        "$BUTTON_WHITE_OUTLINED_WITH_ICON:strokeColorListInMode",
        "$BUTTON_WHITE_OUTLINED_WITH_ICON:textColorListInMode",
        "$BUTTON_WHITE_OUTLINED_WITH_ICON:rippleColorInMode",
        "$BUTTON_WHITE_OUTLINED_WITH_ICON:textViewCompoundDrawableTintList",
    ])
    @JvmStatic
    fun MaterialButton.setButtonWhiteOutlinedWithIconInMode(
        backgroundTintColorInMode: String,
        strokeColorListInMode: String,
        textColorListInMode: String,
        rippleColorInMode: String,
        iconTintInMode: String,
    ) {
        setBackgroundTintColorInMode(backgroundTintColorInMode)
        setStrokeColorListInMode(strokeColorListInMode)
        setTextColorListInMode(textColorListInMode)
        setRippleColorInMode(rippleColorInMode)
        setTextViewCompoundDrawableTintList(iconTintInMode)
        Log.i(STYLES_LOGGER, BUTTON_WHITE_OUTLINED_WITH_ICON)
    }

    @BindingAdapter(value =
    [
        "$BUTTON_WHITE_OUTLINED:backgroundTintColorInMode",
        "$BUTTON_WHITE_OUTLINED:strokeColorListInMode",
        "$BUTTON_WHITE_OUTLINED:textColorListInMode",
        "$BUTTON_WHITE_OUTLINED:rippleColorInMode",
    ])
    @JvmStatic
    fun MaterialButton.setButtonWhiteOutlinedInMode(
        backgroundTintColorInMode: String,
        strokeColorListInMode: String,
        textColorListInMode: String,
        rippleColorInMode: String,
    ) {
        setBackgroundTintColorInMode(backgroundTintColorInMode)
        setStrokeColorListInMode(strokeColorListInMode)
        setTextColorListInMode(textColorListInMode)
        setRippleColorInMode(rippleColorInMode)
        Log.i(STYLES_LOGGER, BUTTON_WHITE_OUTLINED)
    }


    @BindingAdapter(value =
    [
        "$BUTTON_STANDARD:backgroundTintListInMode",
        "$BUTTON_STANDARD:rippleListInMode",
        "$BUTTON_STANDARD:textColorInMode",
        "$BUTTON_STANDARD:iconTintColorInMode"
    ])
    @JvmStatic
    fun MaterialButton.setButtonStandardInMode(
        backgroundTintListInMode: String,
        rippleListInMode: String,
        textColorInMode: String,
        iconTintColorInMode: String,
    ) {
        setBackgroundTintList(backgroundTintListInMode)
        setRippleList(rippleListInMode)
        setTextColorInMode(textColorInMode)
        setIconTintColorInMode(iconTintColorInMode)
        Log.i(STYLES_LOGGER, BUTTON_STANDARD)
    }

}