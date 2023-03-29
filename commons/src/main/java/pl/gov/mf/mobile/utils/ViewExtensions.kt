package pl.gov.mf.mobile.utils

import android.graphics.drawable.Drawable
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.HtmlCompat

fun TextView.setHtml(html: String) {
    text = HtmlCompat.fromHtml(
        html,
        HtmlCompat.FROM_HTML_MODE_COMPACT
    )
}

fun TextView.setDrawableTop(drawable: Drawable) {
    setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
}

fun TextView.setDrawableStart(drawable: Drawable) {
    setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
}

fun AppCompatTextView.setDrawableEnd(drawable: Drawable) {
    setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
}