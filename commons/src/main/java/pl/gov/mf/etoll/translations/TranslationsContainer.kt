package pl.gov.mf.etoll.translations

import java.io.Serializable
import java.util.*

const val NO_TRANSLATION = "NO_TRANSLATION"

data class TranslationsContainer(
    val items: MutableMap<TranslationKeys, String> = EnumMap(TranslationKeys::class.java),
    val keys: List<TranslationKeys> = mutableListOf(),
) : Serializable {
    operator fun get(key: TranslationKeys): String = items[key] ?: NO_TRANSLATION
}
