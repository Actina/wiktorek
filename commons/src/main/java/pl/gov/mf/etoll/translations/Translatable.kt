package pl.gov.mf.etoll.translations

import com.google.gson.annotations.SerializedName
import pl.gov.mf.mobile.utils.JsonConvertible

data class Translatable(
    @SerializedName("PL") val polish: String,
    @SerializedName("EN") val english: String?,
    @SerializedName("RU") val russian: String?,
    @SerializedName("DE") val german: String?,
    @SerializedName("BE") val belorussian: String?,
    @SerializedName("LT") val lithuanian: String?,
    @SerializedName("UK") val ukrainian: String?,
    @SerializedName("CS") val czech: String?,
    @SerializedName("SK") val slovakian: String?
) : JsonConvertible {
    fun getForLanguage(language: SupportedLanguages): String = when (language) {
        SupportedLanguages.POLISH -> polish
        SupportedLanguages.ENGLISH -> english
        SupportedLanguages.GERMAN -> german
        SupportedLanguages.CZECH -> czech
        SupportedLanguages.SLOVAK -> slovakian
        SupportedLanguages.UKRAINIAN -> ukrainian
        SupportedLanguages.BELARUSIAN -> belorussian
        SupportedLanguages.LITHUANIAN -> lithuanian
        SupportedLanguages.RUSSIAN -> russian
    } ?: english ?: polish
}