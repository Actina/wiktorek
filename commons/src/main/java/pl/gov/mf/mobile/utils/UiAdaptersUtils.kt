package pl.gov.mf.mobile.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import pl.gov.mf.etoll.app.BaseApplication
import pl.gov.mf.etoll.appmode.AppMode
import pl.gov.mf.etoll.appmode.AppModeManager

fun String.fromColorNameToColorListInMode(context: Context) =
    ColorStateList.valueOf(toColorInMode(context))

@ColorInt
fun String.toColorInMode(context: Context): Int =
    AppModeManager.getColorInModeByContextCompat(this, getAppMode(context), context)

fun String.toColorListInMode(context: Context) =
    AppModeManager.getColorStateListInModeByContextCompat(this, getAppMode(context), context)

@DrawableRes
fun String.toDrawableIdInMode(context: Context): Int =
    AppModeManager.getDrawableIdInMode(this, getAppMode(context), context)

fun String.toDrawableInMode(context: Context): Drawable? =
    AppModeManager.getDrawableInModeByContextCompat(this, getAppMode(context), context)

@RawRes
fun String.toRawResInMode(context: Context): Int =
    AppModeManager.getRawRes(this, getAppMode(context), context)

fun getAppMode(context: Context): AppMode =
    (context.applicationContext as BaseApplication).getApplicationComponent()
        .useCaseGetCurrentAppMode()
        .execute()

