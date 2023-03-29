package pl.gov.mf.mobile.ui.components.adapters

import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import pl.gov.mf.mobile.ui.components.views.pin.PinInput

object PinInputAdapter {

    @BindingAdapter("nkspo:pinAttrChanged")
    @JvmStatic
    fun setPinAttrChanged(pinInput: PinInput, listener: InverseBindingListener?) {
        listener ?: return

        pinInput.setOnPinChanged { listener.onChange() }
    }

    @InverseBindingAdapter(attribute = "nkspo:pin")
    @JvmStatic
    fun getPin(pinInput: PinInput): String = pinInput.pin

    @BindingAdapter("nkspo:pin")
    @JvmStatic
    fun setPin(pinInput: PinInput, pin: String?) {
        pinInput.pin = pin ?: ""
    }

    @BindingAdapter("nkspo:isLocked")
    @JvmStatic
    fun setIsLocked(pinInput: PinInput, isLocked: Boolean?) {
        pinInput.isLocked = isLocked ?: false
    }
}