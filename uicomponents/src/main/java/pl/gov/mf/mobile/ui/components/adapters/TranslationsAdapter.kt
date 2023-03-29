package pl.gov.mf.mobile.ui.components.adapters

import android.annotation.SuppressLint
import android.content.res.Resources
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.android.material.switchmaterial.SwitchMaterial
import pl.gov.mf.etoll.app.BaseApplication
import pl.gov.mf.etoll.translations.AppLanguageManager
import pl.gov.mf.etoll.translations.TranslationKeys
import pl.gov.mf.mobile.utils.setHtml
import pl.gov.mf.mobile.utils.translate

object TranslationsAdapter {

    private const val COLONS_END = ": "

    @BindingAdapter("nkspo:conditionallyTranslatedText")
    @JvmStatic
    fun bindConditionalTranslations(
        view: TextView,
        conditionallyTranslatedText: ConditionallyTranslatedText?,
    ) {
        conditionallyTranslatedText?.let {
            if (it.shouldBeTranslated)
                translate(view, it.untranslatedText)
            else
                view.text = it.untranslatedText
        }
    }

    @SuppressLint("CheckResult")
    @BindingAdapter("nkspo:translatedText")
    @JvmStatic
    fun bindTranslations(
        view: TextView,
        name: String?,
    ) {
        translate(view, name)
    }

    /*
    Sample usage:
    * 1. Add import: <import type="pl.gov.mf.etoll.translations.TranslationKeys"/> to given xml
    * 2. Add translation: nkspo:translatedKey="@{TranslationKeys.DRIVER_WARNING_HEADER}"
     */
    @SuppressLint("CheckResult")
    @BindingAdapter("nkspo:translatedKey")
    @JvmStatic
    fun bindTranslations(
        view: TextView,
        translationKey: TranslationKeys?,
    ) {
        translationKey?.key?.let {
            translate(view, it)
        }
    }

    @SuppressLint("CheckResult")
    @BindingAdapter("nkspo:translatedTextColonsEnded", "nkspo:skipColonsWhen", requireAll = false)
    @JvmStatic
    fun bindTranslationsColonsEnded(
        view: TextView,
        name: String?,
        skipColons: Boolean = false
    ) {
        translate(view, name, if (skipColons) "" else COLONS_END)
    }

    @SuppressLint("CheckResult")
    @BindingAdapter("nkspo:translatedText")
    @JvmStatic
    fun bindTranslations(
        view: Button,
        name: String?,
    ) {
        translate(view, name)
    }


    @SuppressLint("CheckResult")
    @BindingAdapter("nkspo:translatedText")
    @JvmStatic
    fun bindTranslations(
        view: SwitchMaterial,
        name: String?,
    ) {
        translate(view, name)
    }

    @SuppressLint("CheckResult")
    @BindingAdapter("nkspo:translatedHint")
    @JvmStatic
    fun bindHintTranslations(
        view: EditText,
        name: String,
    ) {
        view.hint = name.translate(view.context)
    }

    @SuppressLint("CheckResult")
    @BindingAdapter("nkspo:translatedHint")
    @JvmStatic
    fun EditText.bindHintTranslations(
        nameAndInsertingValue: Pair<String, String>?
    ) {
        nameAndInsertingValue?.run {
            hint = first.translate(context, second)
        }
    }

    @SuppressLint("CheckResult")
    @BindingAdapter("nkspo:translatedTextWithValue")
    @JvmStatic
    fun TextView.bindTranslationsInsertingValue(
        nameAndInsertingValue: Pair<String, String?>?
    ) {
        nameAndInsertingValue?.run {
            text = if (second != null) first.translate(context, second)
            else first.translate(context)
        }
    }

    private fun translate(view: TextView, name: String?, end: String = "") {
        if (name == null) return
        val language =
            (view.context.applicationContext as BaseApplication).getApplicationComponent()
                .useCaseGetCurrentLanguage()
                .execute()
        try {
            val translation =
                AppLanguageManager.getStringResourceByName(
                    name + language.translationPostfix,
                    view.context
                )

            translation.let {
                val finalText = if (end == COLONS_END && !it.endsWith(":")) it + end else it
                // left to remember, that this is NOT a solution as we do have some other html tags in code
        //                    .replace("<", "&lt;")
        //                    .replace(">", "&gt;")
                view.setHtml(finalText)
            }
        } catch (ex: Resources.NotFoundException) {
            // we do not want app to crash, instead do not show anything there
            view.setHtml(name)
        }
    }

    data class ConditionallyTranslatedText(
        val untranslatedText: String,
        val shouldBeTranslated: Boolean
    )

    fun String.toConditionallyTranslatedText(shouldBeTranslated: Boolean = false): ConditionallyTranslatedText =
        ConditionallyTranslatedText(this, shouldBeTranslated)
}