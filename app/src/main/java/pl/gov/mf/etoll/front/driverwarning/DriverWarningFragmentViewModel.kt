package pl.gov.mf.etoll.front.driverwarning

import androidx.lifecycle.SavedStateHandle
import pl.gov.mf.etoll.base.BaseComposeViewModel
import pl.gov.mf.etoll.core.CoreComposedUC
import pl.gov.mf.etoll.front.localization.TranslationsGenerator
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC
import pl.gov.mf.etoll.translations.TranslationKeys
import pl.gov.mf.etoll.translations.TranslationsContainer
import javax.inject.Inject

class DriverWarningFragmentViewModel(savedStateHandle: SavedStateHandle) :
    BaseComposeViewModel(savedStateHandle), TranslationsGenerator {

    @Inject
    lateinit var readSettingsUseCase: SettingsUC.ReadSettingsUseCase

    @Inject
    lateinit var writeSettingsUseCase: SettingsUC.WriteSettingsUseCase

    @Inject
    lateinit var getCoreStatusUseCase: CoreComposedUC.GetCoreStatusUseCase

    fun shouldSkipScreen() = readSettingsUseCase.executeForBoolean(Settings.SKIP_DRIVER_WARNING)

    override fun generateTranslations(): TranslationsContainer = TranslationsContainer(
        keys = mutableListOf(
            TranslationKeys.DRIVER_WARNING_HEADER,
            TranslationKeys.DRIVER_WARNING_CONTENT,
            TranslationKeys.DRIVER_WARNING_CONTINUE,
            TranslationKeys.DRIVER_WARNING_CHECKBOX_TEXT
        )
    )

    fun onCheckboxClicked(selected: Boolean) {
        val currentFlagValue = readSettingsUseCase.executeForBoolean(Settings.SKIP_DRIVER_WARNING)
        if (currentFlagValue != selected)
            writeSettingsUseCase.execute(Settings.SKIP_DRIVER_WARNING, selected).subscribe()
    }
}