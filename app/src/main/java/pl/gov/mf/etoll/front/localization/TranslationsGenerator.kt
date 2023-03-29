package pl.gov.mf.etoll.front.localization

import pl.gov.mf.etoll.translations.TranslationsContainer

//Todo: replace with abstract method in BaseViewModel and override in all view models
interface TranslationsGenerator{
    fun generateTranslations(): TranslationsContainer
}