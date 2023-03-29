package pl.gov.mf.etoll.front.localization

import android.content.Context
import pl.gov.mf.etoll.translations.SupportedLanguages
import pl.gov.mf.etoll.translations.TranslationsContainer

fun TranslationsContainer.localize(context: Context, language: SupportedLanguages) {
    this.keys.forEach { key ->
        try {
            this.items[key] = context.getString(
                context.resources.getIdentifier(
                    key.key + "_" + language.localePrefix,
                    "string",
                    context.packageName
                )
            )
        } catch (_: Exception) {
//            throw RuntimeException("Translation $key not found!")
            this.items[key] = "PLACEHOLDER"
        }
    }
}
