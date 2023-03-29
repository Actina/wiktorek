package pl.gov.mf.etoll.translations

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

sealed class AppLanguageManagerUC {

    class GetCurrentLanguageUseCase(private val ds: AppLanguageManager) : AppLanguageManagerUC() {
        fun execute(): SupportedLanguages = ds.getSelectedLanguage()

        fun executeObservation(): Observable<SupportedLanguages> = ds.observeSelectedLanguage()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    class SetCurrentLanguageUseCase(private val ds: AppLanguageManager) : AppLanguageManagerUC() {
        fun execute(language: SupportedLanguages) = ds.setLanguage(language)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    class GetAvailableLanguagesUseCase : AppLanguageManagerUC() {
        fun execute(): Single<List<SupportedLanguages>> =
            Single.just(SupportedLanguages.values().toList())
    }

    class LanguageWasDetectedOrSetUseCase(private val ds: AppLanguageManager) :
        AppLanguageManagerUC() {
        fun execute() = ds.languageWasDetectedOrSet()
    }

}