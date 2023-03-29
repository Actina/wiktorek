package pl.gov.mf.mobile.ui.components.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

fun View.setVisibilityAsBoolean(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

fun View.getVisibilityAsBoolean(): Boolean = visibility == View.VISIBLE

fun View.showKeyboard() {
    post {
        requestFocus()

        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.run {
            showSoftInput(this@showKeyboard, InputMethodManager.SHOW_IMPLICIT)
        }
    }
}

fun View.hideKeyboard() {
    post {
        clearFocus()

        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.run {
            hideSoftInputFromWindow(this@hideKeyboard.windowToken, 0)
        }
    }
}