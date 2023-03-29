package pl.gov.mf.etoll.translations

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.res.Resources
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import pl.gov.mf.etoll.initialization.LoadableSystemsLoader
import javax.inject.Inject

class AppLanguageManagerImpl @Inject constructor(
    private val context: Context,
    private val resources: Resources
) :
    AppLanguageManager {

    companion object {
        private const val DEFAULT_LANGUAGE_VAR_NAME = "defLanguage"
        private const val LANGUAGE_FILENAME = "language_settings"
        private val DEFAULT_LANGUAGE = SupportedLanguages.ENGLISH
    }

    private lateinit var owner: LoadableSystemsLoader
    private var loaded = false

    // cached language
    private val defaultLanguageDS = BehaviorSubject.create<SupportedLanguages>()

    // language configuration file - we do not encrypt it
    private var _sharedPreferences: SharedPreferences? = null
    private val sharedPreferences: SharedPreferences
        get() {
            if (_sharedPreferences == null) _sharedPreferences =
                context.getSharedPreferences(LANGUAGE_FILENAME, MODE_PRIVATE)
            return _sharedPreferences!!
        }

    override fun getSelectedLanguage(): SupportedLanguages = if (defaultLanguageDS.hasValue())
        defaultLanguageDS.value!!
    else {
        loadSynchronous()
        if (defaultLanguageDS.hasValue())
            defaultLanguageDS.value!!
        else DEFAULT_LANGUAGE
    }


    override fun observeSelectedLanguage(): Observable<SupportedLanguages> = defaultLanguageDS

    override fun setLanguage(language: SupportedLanguages): Completable =
        Completable.create {
            defaultLanguageDS.onNext(language)
            sharedPreferences.edit()
                .putInt(DEFAULT_LANGUAGE_VAR_NAME, language.ordinal)
                .apply()
            it.onComplete()
        }

    override fun isLoaded(): Boolean = loaded

    override fun load(): Single<Boolean> =
        if (loaded) Single.just(true) else
            Single.create { emitter ->
                loadSynchronous()
                emitter.onSuccess(true)
            }

    override fun setOwner(loadableSystemsLoader: LoadableSystemsLoader) {
        owner = loadableSystemsLoader
    }

    override fun languageWasDetectedOrSet(): Boolean = sharedPreferences.getInt(
        DEFAULT_LANGUAGE_VAR_NAME, -1
    ) != -1

    private fun loadSynchronous() {
        var foundLanguage: SupportedLanguages? = null
        if (!sharedPreferences.contains(DEFAULT_LANGUAGE_VAR_NAME)) {
            //     detect default language based on device locale
            var localeLanguage = resources.configuration.locales[0]?.toLanguageTag()
            if (localeLanguage != null && localeLanguage.contains("_"))
                localeLanguage = localeLanguage.substringBefore("_")
            if (localeLanguage != null && localeLanguage.contains("-"))
                localeLanguage = localeLanguage.substringBefore("-")
            for (language in SupportedLanguages.values()) {
                if (localeLanguage != null && localeLanguage.uppercase()
                        .contentEquals(language.localePrefix.uppercase())
                ) {
                    foundLanguage = language
                    break
                }
            }
            if (foundLanguage == null) {
                foundLanguage = DEFAULT_LANGUAGE
            } else {
                sharedPreferences.edit()
                    .putInt(DEFAULT_LANGUAGE_VAR_NAME, foundLanguage.ordinal)
                    .apply()
            }
        } else {
            foundLanguage = SupportedLanguages.values()[sharedPreferences.getInt(
                DEFAULT_LANGUAGE_VAR_NAME,
                DEFAULT_LANGUAGE.ordinal
            )]
        }

        defaultLanguageDS.onNext(foundLanguage)
        loaded = true
    }
}