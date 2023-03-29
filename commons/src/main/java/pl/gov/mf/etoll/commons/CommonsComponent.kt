package pl.gov.mf.etoll.commons

import pl.gov.mf.etoll.app.CommonGetBusinessNumberUseCase
import pl.gov.mf.etoll.appmode.AppModeManagerUC
import pl.gov.mf.etoll.interfaces.CommonInterfacesUC
import pl.gov.mf.etoll.interfaces.SoundNotificationController
import pl.gov.mf.etoll.logging.LogUseCase
import pl.gov.mf.etoll.translations.AppLanguageManagerUC.GetCurrentLanguageUseCase
import pl.gov.mf.etoll.translations.AppLanguageManagerUC.SetCurrentLanguageUseCase

interface CommonsComponent {
    fun useCaseLog(): LogUseCase

    fun useCaseGetCurrentLanguage(): GetCurrentLanguageUseCase
    fun useCaseSetCurrentLanguage(): SetCurrentLanguageUseCase

    fun useCaseGetCurrentAppMode(): AppModeManagerUC.GetCurrentAppModeUseCase
    fun useCaseSetCurrentAppMode(): AppModeManagerUC.SetCurrentAppModeUseCase

    fun useCaseGetBusinessNumber(): CommonGetBusinessNumberUseCase

    fun soundController(): SoundNotificationController

    fun useCaseAddNotificationToHistory(): CommonInterfacesUC.AddNotificationToHistoryUseCase

    fun useCaseCheckIfShouldAddNotificationToHistory(): CommonInterfacesUC.CheckIfShouldAddNotificationToHistory
}