package pl.gov.mf.etoll.appmode

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.core.content.ContextCompat
import io.reactivex.Completable
import io.reactivex.Observable
import pl.gov.mf.etoll.initialization.LoadableSystem

interface AppModeManager : LoadableSystem {
    fun setAppMode(appMode: AppMode, followSystem: Boolean): Completable
    fun getCurrentAppMode(): AppMode
    fun observeModeChanges(): Observable<AppMode>
    fun getCurrentAppConfMode(): AppMode?

    companion object {

        //Get rawRes ---

        @RawRes
        fun getRawRes(rawResName: String, appMode: AppMode, context: Context): Int =
            getRawResourceByName(rawResName + getResourceSuffixInMode(appMode), context)

        @RawRes
        private fun getRawResourceByName(fieldName: String, context: Context): Int =
            context.resources.getIdentifier(fieldName, "raw", context.packageName)

        //Get colorStateList ---

        fun getColorStateListInModeByContextCompat(
            colorStateListName: String,
            appMode: AppMode,
            context: Context,
        ): ColorStateList? = ContextCompat.getColorStateList(
            context,
            getColorStateListInMode(colorStateListName, appMode, context)
        )

        @ColorRes
        fun getColorStateListInMode(drawableName: String, appMode: AppMode, context: Context): Int =
            getDrawableResourceByName(drawableName + getResourceSuffixInMode(appMode), context)

        //Get drawable ---

        fun getDrawableInModeByContextCompat(
            drawableName: String,
            appMode: AppMode,
            context: Context,
        ): Drawable? =
            ContextCompat.getDrawable(context, getDrawableIdInMode(drawableName, appMode, context))

        @DrawableRes
        fun getDrawableIdInMode(drawableName: String, appMode: AppMode, context: Context): Int =
            getDrawableResourceByName(drawableName + getResourceSuffixInMode(appMode), context)

        private fun getDrawableResourceByName(fieldName: String, context: Context): Int =
            context.resources.getIdentifier(fieldName, "drawable", context.packageName)

        //Get color ---

        @ColorInt
        fun getColorInModeByContextCompat(
            colorName: String,
            appMode: AppMode,
            context: Context,
        ): Int =
            ContextCompat.getColor(context, getColorIdInMode(colorName, appMode, context))

        @ColorRes
        fun getColorIdInMode(colorName: String, appMode: AppMode, context: Context): Int =
            getColorResourceByName(colorName + getResourceSuffixInMode(appMode), context)

        private fun getColorResourceByName(fieldName: String, context: Context): Int =
            context.resources.getIdentifier(fieldName, "color", context.packageName)

        //Common ---

        private fun getResourceSuffixInMode(appMode: AppMode) = when (appMode) {
            AppMode.DARK_MODE -> "_dark"
            AppMode.LIGHT_MODE -> "_light"
        }
    }
}