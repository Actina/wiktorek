package pl.gov.mf.etoll.translations

import androidx.annotation.Keep
import pl.gov.mf.mobile.utils.JsonConvertible

enum class SupportedLanguages(
    val localePrefix: String,
    val translationPostfix: String,
    val apiLanguageName: String,
    val translationName: TranslationKeys
) : JsonConvertible {
    //We serialize this enum to json so we have to keep it's fields from obfuscation, otherwise we get crash.
    //IMPORTANT: We should add @Keep annotation in all enums which we want to serialize to JSON.
    @Keep
    POLISH("pl", "_pl", "pl-PL", TranslationKeys.LANG_POLISH),

    @Keep
    ENGLISH("en", "_en", "en-EN", TranslationKeys.LANG_ENGLISH),

    // todo - check "apiLanguageNames"
    @Keep
    GERMAN("de", "_de", "de", TranslationKeys.LANG_GERMAN),

    @Keep
    CZECH("cs", "_cs", "cs", TranslationKeys.LANG_CZECH),

    @Keep
    SLOVAK("sk", "_sk", "sk", TranslationKeys.LANG_SLOVAK),

    @Keep
    UKRAINIAN("uk", "_uk", "uk", TranslationKeys.LANG_UKRAINIAN),

    @Keep
    BELARUSIAN("be", "_be", "be", TranslationKeys.LANG_BELARUSIAN),

    @Keep
    LITHUANIAN("lt", "_lt", "lt", TranslationKeys.LANG_LITHUANIAN),

    @Keep
    RUSSIAN("ru", "_ru", "ru", TranslationKeys.LANG_RUSSIAN),
}