package pl.gov.mf.mobile.utils

import android.content.Context
import android.content.res.Resources
import android.widget.TextView
import pl.gov.mf.etoll.app.BaseApplication
import pl.gov.mf.etoll.translations.AppLanguageManager
import java.util.*

fun TextView.setTranslatedText(name: String) = apply {
    text = name.translate(this.context)
}

fun String.translate(context: Context): String = try {
    AppLanguageManager.getStringResourceByName(
        this + (context.applicationContext as BaseApplication).getApplicationComponent()
            .useCaseGetCurrentLanguage().execute().translationPostfix, context
    )
} catch (ex: Resources.NotFoundException) {
    this
}

fun String.translate(context: Context, vararg args: Any?): String = try {
    val text = AppLanguageManager.getStringResourceByName(
        this + (context.applicationContext as BaseApplication).getApplicationComponent()
            .useCaseGetCurrentLanguage().execute().translationPostfix, context
    )
    String.format(text, *args)
} catch (ex: Resources.NotFoundException) {
    this
}

fun String.translateIsoCountry(context: Context): String {
    if (this.isEmpty())
        return ""

    val appLanguage = (context.applicationContext as BaseApplication).getApplicationComponent()
        .useCaseGetCurrentLanguage().execute().localePrefix
    return Locale("", this).getDisplayCountry(Locale.forLanguageTag(appLanguage))
}