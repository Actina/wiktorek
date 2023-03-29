package pl.gov.mf.etoll.commons

import dagger.Module
import dagger.Provides
import pl.gov.mf.etoll.app.di.ApplicationScope
import pl.gov.mf.etoll.appmode.AppModeManager
import pl.gov.mf.etoll.appmode.AppModeManagerImpl
import pl.gov.mf.etoll.appmode.AppModeManagerUC
import pl.gov.mf.etoll.appmode.AppModeManagerUC.*
import pl.gov.mf.etoll.logging.LogUseCase
import pl.gov.mf.etoll.logging.LoggingHelper
import pl.gov.mf.etoll.translations.AppLanguageManager
import pl.gov.mf.etoll.translations.AppLanguageManagerImpl
import pl.gov.mf.etoll.translations.AppLanguageManagerUC
import pl.gov.mf.etoll.translations.AppLanguageManagerUC.GetCurrentLanguageUseCase
import pl.gov.mf.etoll.translations.AppLanguageManagerUC.SetCurrentLanguageUseCase
import pl.gov.mf.etoll.validations.ValidationManager
import pl.gov.mf.etoll.validations.ValidationManagerImpl
import pl.gov.mf.etoll.validations.ValidationManagerUC

@Module
class CommonsModule {

    @Provides
    @ApplicationScope
    fun providesLanguageManager(impl: AppLanguageManagerImpl): AppLanguageManager = impl

    @Provides
    @ApplicationScope
    fun providesAppModeManager(impl: AppModeManagerImpl): AppModeManager = impl

    @Provides
    fun providesGetCurrentAppModeUseCase(ds: AppModeManager): GetCurrentAppModeUseCase =
        GetCurrentAppModeUseCase(ds)

    @Provides
    fun providesSetCurrentAppModeUseCase(ds: AppModeManager): SetCurrentAppModeUseCase =
        SetCurrentAppModeUseCase(ds)

    @Provides
    fun providesValidationManager(): ValidationManager = ValidationManagerImpl()

    @Provides
    fun providesGetLanguageUseCase(ds: AppLanguageManager): GetCurrentLanguageUseCase =
        GetCurrentLanguageUseCase(ds)

    @Provides
    fun providesSetLanguageUseCase(ds: AppLanguageManager): SetCurrentLanguageUseCase =
        SetCurrentLanguageUseCase(ds)

    @Provides
    fun provideGetAvailableLanguages(): AppLanguageManagerUC.GetAvailableLanguagesUseCase =
        AppLanguageManagerUC.GetAvailableLanguagesUseCase()

    @Provides
    fun providesValidatePinsUseCase(validationManager: ValidationManager): ValidationManagerUC.ValidatePinsUseCase =
        ValidationManagerUC.ValidatePinsUseCase(validationManager)

    @Provides
    fun providesValidatePasswordsUseCase(validationManager: ValidationManager): ValidationManagerUC.ValidatePasswordsUseCase =
        ValidationManagerUC.ValidatePasswordsUseCase(validationManager)

    @Provides
    fun providesLanguageWasDetectedOrSetUseCase(ds: AppLanguageManager) =
        AppLanguageManagerUC.LanguageWasDetectedOrSetUseCase(ds)

    @ApplicationScope
    @Provides
    fun providesLogUseCase(logger: LoggingHelper): LogUseCase = LogUseCase(logger)
}