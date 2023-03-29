package pl.gov.mf.etoll.translations

import android.content.Context
import io.reactivex.Completable
import io.reactivex.Observable
import pl.gov.mf.etoll.initialization.LoadableSystem

interface AppLanguageManager : LoadableSystem {

    companion object {
        /**
         * Get R.string.name value by using just "name"
         */
        fun getStringResourceByName(
            fieldName: String,
            context: Context
        ): String =
            context.getString(
                context.resources.getIdentifier(
                    fieldName,
                    "string",
                    context.packageName
                )
            )
    }

    /**
     * Get current default language
     */
    fun getSelectedLanguage(): SupportedLanguages

    /**
     * Observe default language changes
     */
    fun observeSelectedLanguage(): Observable<SupportedLanguages>

    /**
     * Set default language
     */
    fun setLanguage(language: SupportedLanguages): Completable

    fun isLoaded(): Boolean

    fun languageWasDetectedOrSet(): Boolean
}