package pl.gov.mf.mobile.utils

import android.content.res.Resources
import android.util.TypedValue

fun Float.toDp(resources: Resources): Float =
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        resources.displayMetrics
    )

fun Int.toDp(resources: Resources): Float = this.toFloat().toDp(resources)