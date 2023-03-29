package pl.gov.mf.mobile.ui.components.utils

import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout
import pl.gov.mf.etoll.ui.components.R

fun TextInputLayout.lock() {
    this.editText?.text = null
    this.startIconDrawable = ContextCompat.getDrawable(this.context, R.drawable.ic_lock_light)
    this.isEnabled = false
}

fun TextInputLayout.unlock() {
    this.startIconDrawable = null
    this.isEnabled = true
}