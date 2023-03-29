package pl.gov.mf.etoll

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import pl.gov.mf.etoll.base.BaseComposeViewModel
import pl.gov.mf.etoll.base.BaseComposeViewModel.Companion.PENDING_ERROR_DIALOG_TAG
import pl.gov.mf.etoll.base.BaseMVVMFragment
import pl.gov.mf.etoll.translations.TranslationKeys
import pl.gov.mf.etoll.translations.TranslationsContainer
import pl.gov.mf.etoll.ui.components.compose.combined.dialogs.EbiletOneButtonDialog
import pl.gov.mf.etoll.utils.observeOnce
import pl.gov.mf.etoll.ui.components.compose.theme.EtollTheme
import pl.gov.mf.mobile.utils.GooglePlayStoreUtils
import pl.gov.mf.mobile.utils.getAppMode

abstract class BaseComposeFragment<VIEWMODEL : BaseComposeViewModel, COMPONENT> :
    BaseMVVMFragment<VIEWMODEL, COMPONENT>() {

    private val localTranslations = compositionLocalOf { TranslationsContainer() }

    /*
     Should be used in root to provide translations
    to whole compose tree at once - without passing arguments
    */
    protected val composeTranslations: TranslationsContainer
        @Composable get() = localTranslations.current


    @Composable
    fun ContentWithTranslations(
        translations: TranslationsContainer,
        content: @Composable () -> Unit,
    ) {
        CompositionLocalProvider(localTranslations provides translations) {
            content()
        }
    }

    @Composable
    protected fun EtollThemeWithModeAndTranslations(
        screenContent: @Composable () -> Unit,
    ) {
        val translations: TranslationsContainer by viewModel.translations.observeAsState(
            TranslationsContainer()
        )
        EtollTheme(getAppMode = { getAppMode(requireContext()) }) {
            ContentWithTranslations(translations = translations) {
                screenContent()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getViewModel().loadingIndicator.observeOnce(this) { show ->
            if (show) showLoading() else hideLoading()
        }
        getViewModel().pendingError.observeOnce(viewLifecycleOwner) {
            if (it.shouldBeShown)
                showErrorDialog(it.error, it.customMessage)
            else
                EbiletOneButtonDialog.dismiss(parentFragmentManager, PENDING_ERROR_DIALOG_TAG)
        }
    }

    override fun onStart() {
        super.onStart()
        getViewModel().onStart(requireContext())
    }

    override fun onStop() {
        super.onStop()
        getViewModel().onStop()
    }

    abstract fun getViewModel(): BaseComposeViewModel

    private fun showErrorDialog(cause: ViewErrorCause?, customText: String? = null) {
        val translations: TranslationsContainer =
            getViewModel().getErrorTranslations(requireContext())
        when (cause) {
            ViewErrorCause.NETWORK_ERROR -> {
                EbiletOneButtonDialog.createAndShow(
                    fragmentManager = parentFragmentManager,
                    title = translations[TranslationKeys.MISC_ERROR_TITLE],
                    description = customText
                        ?: translations[TranslationKeys.NETWORK_ERROR_INFO_CONTENT],
                    buttonText = translations[TranslationKeys.MISC_OK_BUTTON],
                    iconLightRes = R.drawable.ic_error_new_light,
                    iconDarkRes = R.drawable.ic_error_new_dark,
                    dismissible = false,
                    uniqueTag = PENDING_ERROR_DIALOG_TAG,
                    dialogType = EbiletOneButtonDialog.DialogType.ERROR,
                )?.observeOnce(viewLifecycleOwner) {
                    getViewModel().onErrorShown(cause, customText)
                }
            }
            ViewErrorCause.APP_TOO_OLD -> {
                EbiletOneButtonDialog.createAndShow(
                    fragmentManager = parentFragmentManager,
                    title = translations[TranslationKeys.MISC_ERROR_TITLE],
                    description = translations[TranslationKeys.OLD_VERSION_ERROR_INFO_CONTENT],
                    buttonText = translations[TranslationKeys.MISC_OK_BUTTON],
                    iconLightRes = R.drawable.ic_version_issue_light,
                    iconDarkRes = R.drawable.ic_version_issue_dark,
                    dismissible = false,
                    uniqueTag = PENDING_ERROR_DIALOG_TAG,
                    dialogType = EbiletOneButtonDialog.DialogType.ERROR,
                )?.observeOnce(viewLifecycleOwner) {
                    getViewModel().onErrorShown(cause, customText)
                    GooglePlayStoreUtils.showStoreForCurrentApp(activity = requireActivity())
                }
            }
            ViewErrorCause.WRONG_SYSTEM_TIME -> {
                EbiletOneButtonDialog.createAndShow(
                    fragmentManager = parentFragmentManager,
                    title = translations[TranslationKeys.MISC_ERROR_TITLE],
                    description = translations[TranslationKeys.TIME_ERROR_TITLE],
                    buttonText = translations[TranslationKeys.MISC_OK_BUTTON],
                    iconLightRes = R.drawable.ic_wrong_time_error_light,
                    iconDarkRes = R.drawable.ic_wrong_time_error_dark,
                    dismissible = false,
                    uniqueTag = PENDING_ERROR_DIALOG_TAG,
                    dialogType = EbiletOneButtonDialog.DialogType.ERROR,
                )?.observeOnce(viewLifecycleOwner) {
                    getViewModel().onErrorShown(cause, customText)
                }
            }
            else -> {}
        }
    }

}