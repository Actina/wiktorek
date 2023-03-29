package pl.gov.mf.mobile.utils

import android.content.res.Resources

fun Float.px(): Float = (this / Resources.getSystem().displayMetrics.density)