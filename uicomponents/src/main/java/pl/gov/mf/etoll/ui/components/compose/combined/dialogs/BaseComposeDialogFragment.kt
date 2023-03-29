package pl.gov.mf.etoll.ui.components.compose.combined.dialogs

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.fragment.app.DialogFragment
import pl.gov.mf.etoll.translations.TranslationsContainer
import pl.gov.mf.etoll.ui.components.dialogs.base.BaseComposeDialogFragmentViewModel
import pl.gov.mf.etoll.ui.components.compose.theme.EtollTheme
import pl.gov.mf.mobile.utils.getAppMode

abstract class BaseComposeDialogFragment : DialogFragment() {

    private val localTranslations = compositionLocalOf { TranslationsContainer() }

    /*
     Should be used in root to provide translations
    to whole compose tree at once - without passing arguments
    */
    protected val composeTranslations: TranslationsContainer
        @Composable get() = localTranslations.current


    @Composable
    protected fun EtollThemeWithModeAndTranslations(
        screenContent: @Composable () -> Unit,
    ) {
        val translations: TranslationsContainer by getViewModel().translations.observeAsState(
            TranslationsContainer()
        )
        EtollTheme(getAppMode = { getAppMode(requireContext()) }) {
            ContentWithTranslations(translations = translations) {
                screenContent()
            }
        }
    }

    @Composable
    private fun ContentWithTranslations(
        translations: TranslationsContainer,
        content: @Composable () -> Unit,
    ) {
        CompositionLocalProvider(localTranslations provides translations) {
            content()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getViewModel().onCreate()
    }

    override fun onStart() {
        super.onStart()
        getViewModel().onStart(requireContext())
    }

    override fun onStop() {
        super.onStop()
        getViewModel().onStop()
    }

    abstract fun getViewModel(): BaseComposeDialogFragmentViewModel
}