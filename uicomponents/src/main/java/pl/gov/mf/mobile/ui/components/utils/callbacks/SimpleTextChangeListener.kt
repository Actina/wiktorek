package pl.gov.mf.mobile.ui.components.utils.callbacks

import android.text.Editable
import android.text.TextWatcher

abstract class SimpleTextChangeListener : TextWatcher {
    override fun afterTextChanged(text: Editable?) {}

    override fun beforeTextChanged(
        text: CharSequence?,
        changeBeginningPosition: Int,
        lengthOfOldCharSequence: Int,
        lengthOfNewCharSequence: Int
    ) {}

    override fun onTextChanged(
        text: CharSequence?,
        changeBeginningPosition: Int,
        lengthOfOldCharSequence: Int,
        lengthOfNewCharSequence: Int
    ) {}
}