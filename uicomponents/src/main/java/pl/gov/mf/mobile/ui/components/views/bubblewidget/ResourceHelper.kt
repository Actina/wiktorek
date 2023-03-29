package pl.gov.mf.mobile.ui.components.views.bubblewidget

import android.content.Context
import android.util.DisplayMetrics
import android.view.LayoutInflater
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat

//TODO: simplify? do we really need this?
object ResourceHelper {
    fun getHeight(context: Context, @LayoutRes resource: Int) =
        LayoutInflater.from(context).inflate(resource, null).run {
            measure(0, 0)
            val height = toDP(context, measuredHeight)
            height.toInt()
        }

    fun getColor(context: Context, @ColorRes res: Int) =
        ContextCompat.getColorStateList(context, res)

    private fun toDP(context: Context, value: Int) =
        value / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}