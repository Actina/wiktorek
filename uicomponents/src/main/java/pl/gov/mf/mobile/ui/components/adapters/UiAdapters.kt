package pl.gov.mf.mobile.ui.components.adapters

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.TypedValue
import android.view.View
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.text.util.LinkifyCompat
import androidx.core.widget.ImageViewCompat
import androidx.core.widget.TextViewCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import pl.gov.mf.etoll.appmode.AppMode
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.mobile.ui.components.utils.getVisibilityAsBoolean
import pl.gov.mf.mobile.ui.components.utils.setVisibilityAsBoolean
import pl.gov.mf.mobile.utils.*


object UiAdapters {

    @BindingAdapter("android:visibility")
    @JvmStatic
    fun setVisibility(view: View, visibility: Boolean) {
        if (view.getVisibilityAsBoolean() != visibility) {
            view.setVisibilityAsBoolean(visibility)
        }
    }

    @BindingAdapter("android:visibilityOrInvisible")
    @JvmStatic
    fun setVisibilityOrInv(view: View, visibility: Boolean) {
        if (view.getVisibilityAsBoolean() != visibility) {
            view.visibility = if (visibility) View.VISIBLE else View.INVISIBLE
        }
    }

    //Deprecated
    @BindingAdapter("android:background")
    @JvmStatic
    fun setBackground(view: View, drawable: Int) {
        if (drawable != 0)
            view.setBackgroundResource(drawable)
    }

    //Mode-aware adapters start

    @BindingAdapter("nkspo:backgroundDrawableInMode")
    @JvmStatic
    fun View.setBackgroundDrawableInMode(drawableName: String) {
        drawableName.toDrawableIdInMode(context).let {
            if (it != 0)
                setBackgroundResource(it)
        }
    }

    @BindingAdapter("nkspo:backgroundColorInMode")
    @JvmStatic
    fun View.setBackgroundColorInMode(colorName: String) {
        colorName.toColorInMode(context).let {
            if (it != 0)
                setBackgroundColor(it)
        }
    }

    @BindingAdapter("nkspo:tintInMode")
    @JvmStatic
    fun ImageView.setTintInMode(colorName: String) {
        ImageViewCompat.setImageTintList(
            this,
            ColorStateList.valueOf(colorName.toColorInMode(context))
        )
    }

    @BindingAdapter("nkspo:textColorInMode")
    @JvmStatic
    fun TextView.setTextColorInMode(colorName: String) {
        colorName.toColorInMode(context).let {
            if (it != 0)
                setTextColor(it)
        }
    }

    @BindingAdapter("nkspo:hintColorInMode")
    @JvmStatic
    fun TextInputEditText.setTextHintColorInMode(colorName: String) {
        colorName.toColorInMode(context).let {
            if (it != 0)
                setHintTextColor(it)
        }
    }

    @BindingAdapter("nkspo:textColorInMode")
    @JvmStatic
    fun AppCompatTextView.setTextColorInMode(colorName: String) {
        colorName.toColorInMode(context).let {
            if (it != 0)
                setTextColor(it)
        }
    }

    @BindingAdapter("nkspo:textColorInModeFromVm")
    @JvmStatic
    fun TextView.setTextColorInModeFromVm(colorName: String?) {
        colorName?.toColorInMode(context)?.let {
            setTextColor(it)
        }
    }

    @BindingAdapter("nkspo:tabIndicatorColorInMode")
    @JvmStatic
    fun TabLayout.setTabIndicatorColorInMode(colorName: String?) {
        colorName?.toColorInMode(context)?.let {
            setSelectedTabIndicatorColor(it)
        }
    }

    @BindingAdapter("nkspo:textColorInMode")
    @JvmStatic
    fun TabLayout.setTextColorInMode(drawableName: String?) {
        drawableName?.toColorListInMode(context)?.let {
            tabTextColors = it
        }
    }

    @BindingAdapter("nkspo:backgroundDrawableInModeFromVm")
    @JvmStatic
    fun View.setBackgroundDrawableInModeFromVm(drawableName: String?) {
        drawableName?.toDrawableIdInMode(context)?.let {
            if (it != 0)
                setBackgroundResource(it)
        }
    }

    @BindingAdapter("nkspo:textColorListInMode")
    @JvmStatic
    fun TextView.setTextColorListInMode(colorStateListDrawableName: String) {
        colorStateListDrawableName.toColorListInMode(context)?.let {
            setTextColor(it)
        }
    }

    @BindingAdapter("nkspo:drawableTopCompatInMode")
    @JvmStatic
    fun TextView.setDrawableTopCompatInMode(drawableName: String) {
        drawableName.toDrawableInMode(context)?.let {
            setDrawableTop(it)
        }
    }

    @BindingAdapter("nkspo:drawableStartCompatInMode")
    @JvmStatic
    fun TextView.setDrawableStartCompatInMode(drawableName: String) {
        drawableName.toDrawableInMode(context)?.let {
            setDrawableStart(it)
        }
    }

    @BindingAdapter("nkspo:drawableEndCompatInMode")
    @JvmStatic
    fun AppCompatTextView.setDrawableEndCompatInMode(drawableName: String) {
        drawableName.toDrawableInMode(context)?.let {
            setDrawableEnd(it)
        }
    }

    @BindingAdapter("nkspo:srcInMode")
    @JvmStatic
    fun ImageView.setSrc(drawableName: String) {
        drawableName.toDrawableInMode(context)?.let {
            setImageDrawable(it)
        }
    }

    @BindingAdapter("nkspo:srcInMode")
    @JvmStatic
    fun AppCompatImageView.setSrc(drawableName: String) {
        drawableName.toDrawableInMode(context)?.let {
            setImageDrawable(it)
        }
    }

    @BindingAdapter("nkspo:srcInModeFromVm")
    @JvmStatic
    fun ImageView.setSrcInModeFromVm(drawableName: String?) {
        drawableName?.toDrawableInMode(context)?.let {
            setImageDrawable(it)
        }
    }

//    @BindingAdapter("nkspo:indeterminateDrawableInMode")
//    @JvmStatic
//    fun ProgressBar.setIndeterminateDrawableInMode(drawableName: String?) {
//        drawableName?.let {
////            indeterminateDrawable = drawableName.toDrawableInMode(context)
//        }
//    }

    @BindingAdapter("nkspo:backgroundTintListInMode")
    @JvmStatic
    fun MaterialButton.setBackgroundTintList(colorStateListDrawableName: String) {
        colorStateListDrawableName.toColorListInMode(context)?.let {
            backgroundTintList = it
        }
    }

    @BindingAdapter("nkspo:backgroundTintColorInMode")
    @JvmStatic
    fun MaterialButton.setBackgroundTintColorInMode(colorName: String) {
        colorName.fromColorNameToColorListInMode(context).let {
            backgroundTintList = it
        }
    }

    @BindingAdapter("nkspo:rippleColorInMode")
    @JvmStatic
    fun MaterialButton.setRippleColorInMode(colorName: String) {
        colorName.fromColorNameToColorListInMode(context).let {
            rippleColor = it
        }
    }

    @BindingAdapter("nkspo:rippleListInMode")
    @JvmStatic
    fun MaterialButton.setRippleList(colorStateListDrawableName: String) {
        colorStateListDrawableName.toColorListInMode(context)?.let {
            rippleColor = it
        }
    }

    @BindingAdapter("nkspo:lottie_rawResInMode")
    @JvmStatic
    fun LottieAnimationView.setRawResInMode(rawResName: String) {
        rawResName.toRawResInMode(context).takeIf { it != 0 }?.let {
            setAnimation(it)
        }
    }

    @BindingAdapter("nkspo:textViewCompoundDrawableTintList")
    @JvmStatic
    fun TextView.setTextViewCompoundDrawableTintList(colorStateListDrawableName: String) {
        colorStateListDrawableName.toColorListInMode(context)?.let {
            TextViewCompat.setCompoundDrawableTintList(
                this,
                it
            )
        }
    }

    @BindingAdapter("nkspo:strokeColorListInMode")
    @JvmStatic
    fun MaterialButton.setStrokeColorListInMode(colorStateListDrawableName: String) {
        colorStateListDrawableName.toColorListInMode(context)?.let {
            strokeColor = it
        }
    }

    @BindingAdapter("nkspo:iconTintColorInMode")
    @JvmStatic
    fun MaterialButton.setIconTintColorInMode(colorName: String) {
        colorName.fromColorNameToColorListInMode(context).let {
            iconTint = it
        }
    }

    @BindingAdapter("nkspo:tintInMode")
    @JvmStatic
    fun MaterialRadioButton.setTrackInMode(colorStateListName: String) {
        colorStateListName.toColorListInMode(context)?.let {
            buttonTintList = it
        }
    }

    @BindingAdapter("nkspo:tintInMode")
    @JvmStatic
    fun MaterialCheckBox.setTrackInMode(colorStateListName: String) {
        colorStateListName.toColorListInMode(context)?.let {
            buttonTintList = it
        }
    }

    @BindingAdapter("nkspo:thumbInMode")
    @JvmStatic
    fun SwitchMaterial.setThumbInMode(colorStateListName: String) {
        colorStateListName.toColorListInMode(context)?.let {
            thumbTintList = it
        }
    }

    @BindingAdapter("nkspo:trackInMode")
    @JvmStatic
    fun SwitchMaterial.setTrackInMode(colorStateListName: String) {
        colorStateListName.toColorListInMode(context)?.let {
            trackTintList = it
        }
    }

    @BindingAdapter("nkspo:visibleWhenMode")
    @JvmStatic
    fun CalendarView.setCalendarVisibilityWhenDarkMode(appMode: AppMode) {
        setVisibility(this, getAppMode(context) == appMode)
    }

    //Mode-aware adapters end

    @BindingAdapter("nkspo:changeAnimationsEnabled")
    @JvmStatic
    fun RecyclerView.changeAnimationsEnabled(changeAnimationsEnabled: Boolean) {
        (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = changeAnimationsEnabled
    }

    @BindingAdapter("nkspo:navigationIconVisible")
    @JvmStatic
    fun MaterialToolbar.setNavigationIconVisible(visible: Boolean) {
        navigationIcon = if (visible) {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(R.attr.homeAsUpIndicator, typedValue, true)
            val imageResId = typedValue.resourceId
            ContextCompat.getDrawable(context, imageResId)?.apply {
                setTint(Color.WHITE)
            } ?: throw IllegalArgumentException("Cannot load drawable $imageResId")
        } else {
            null
        }
    }

    @BindingAdapter("nkspo:translatedHtml")
    @JvmStatic
    fun TextView.setHtmlFromResource(untranslatedHtmlText: String) {
        setHtml(untranslatedHtmlText.translate(context))
    }

    @BindingAdapter("nkspo:translatedHtmlWithUrls")
    @JvmStatic
    fun TextView.setHtmlWithUrls(untranslatedHtmlText: String?) {
        untranslatedHtmlText?.let {
            setHtmlFromResource(untranslatedHtmlText)
            movementMethod = LinkMovementMethod.getInstance()
            LinkifyCompat.addLinks(this, Linkify.WEB_URLS)
        }
    }

    @BindingAdapter("nkspo:textColorFromRes")
    @JvmStatic
    fun TextView.setTextColorFromRes(@ColorRes colorRes: Int) {
        if (colorRes != 0) {
            setTextColor(ContextCompat.getColor(context, colorRes))
        }
    }

    @SuppressLint("SetTextI18n")
    @BindingAdapter("nkspo:textTimer")
    @JvmStatic
    fun TextView.setTextTimer(time: Long) {
        val seconds = time % 60
        var minutes = (time / 60) % 60
        this.text = "${minutes.fillZero()}:${seconds.fillZero()}"
    }

    private fun Long.fillZero(): String =
        if (this < 10) "0$this" else "$this"
}