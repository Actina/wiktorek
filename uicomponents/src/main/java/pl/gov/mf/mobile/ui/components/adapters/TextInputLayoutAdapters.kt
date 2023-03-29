package pl.gov.mf.mobile.ui.components.adapters

import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout
import pl.gov.mf.mobile.ui.components.utils.lock
import pl.gov.mf.mobile.ui.components.utils.unlock

object TextInputLayoutAdapters {

    @BindingAdapter("nkspo:isLocked")
    @JvmStatic
    fun setIsLocked(textInputLayout: TextInputLayout, isLocked: Boolean?) {
        if (isLocked == true) textInputLayout.lock()
        else textInputLayout.unlock()
    }
}