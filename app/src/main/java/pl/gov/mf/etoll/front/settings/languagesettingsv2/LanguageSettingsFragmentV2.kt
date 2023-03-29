package pl.gov.mf.etoll.front.settings.languagesettingsv2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.fragment.findNavController
import pl.gov.mf.etoll.BaseComposeFragment
import pl.gov.mf.etoll.MainNavgraphDirections
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.front.MainActivity
import pl.gov.mf.etoll.translations.SupportedLanguages
import pl.gov.mf.etoll.translations.TranslationKeys
import pl.gov.mf.etoll.translations.TranslationsContainer
import pl.gov.mf.etoll.ui.components.compose.combined.button.EtollButtonWithDefaultPadding
import pl.gov.mf.etoll.ui.components.compose.combined.buttonisle.EtollButtonIsleWithDivider
import pl.gov.mf.etoll.ui.components.compose.combined.radiobutton.EtollRadio
import pl.gov.mf.etoll.ui.components.compose.combined.toolbar.EtollBackIconButton
import pl.gov.mf.etoll.ui.components.compose.combined.toolbar.EtollToolbar
import pl.gov.mf.etoll.ui.components.compose.combined.toolbar.EtollToolbarTitle
import pl.gov.mf.etoll.ui.components.compose.theme.Dimens
import pl.gov.mf.etoll.ui.components.compose.theme.EtollTheme
import pl.gov.mf.etoll.ui.components.compose.theme.h4bold
import pl.gov.mf.etoll.ui.components.compose.theme.p1Normal
import pl.gov.mf.etoll.utils.observeOnce
import pl.gov.mf.mobile.utils.getAppMode

class LanguageSettingsFragmentV2 :
    BaseComposeFragment<LanguageSettingsViewModelV2, LanguageSettingsFragmentV2Component>() {

    override fun getViewModel(): LanguageSettingsViewModelV2 = viewModel

    override fun createComponent(): LanguageSettingsFragmentV2Component =
        (context as MainActivity).component.plus(LanguageSettingsFragmentV2Module(this, lifecycle))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        component.inject(viewModel)
    }

    override fun onResume() {
        super.onResume()
        val startedFromSplash =
            findNavController().previousBackStackEntry?.destination?.id == R.id.splashFragment
        viewModel.setRegistrationMode(startedFromSplash)

        viewModel.navigation.observeOnce(viewLifecycleOwner) {
            when (it) {
                LanguageSettingsViewModelV2.LanguageSettingsNavigation.APPLY_NEW_LANGUAGE -> {
                    viewModel.resetNavigation()
                    viewModel.findNewDestination(startedFromSplash)
                }

                LanguageSettingsViewModelV2.LanguageSettingsNavigation.NEW_REGULATIONS_TO_ACCEPT -> {
                    viewModel.resetNavigation()
                    findNavController().navigate(
                        MainNavgraphDirections.actionRegistrationRegulations(
                            signing = signComponentUseCase.executeCustomSign()
                        )
                    )
                }

                LanguageSettingsViewModelV2.LanguageSettingsNavigation.GO_BACK_TO_SETTINGS -> {
                    viewModel.resetNavigation()
                    findNavController().popBackStack()
                }

                LanguageSettingsViewModelV2.LanguageSettingsNavigation.NONE -> {}
            }
        }

        viewModel.registrationMode.observeOnce(viewLifecycleOwner) {
            when (it) {
                LanguageSettingsViewModelV2.RegistrationMode.APP_REGISTERED -> {}
                LanguageSettingsViewModelV2.RegistrationMode.REGISTRATION -> {
                    minimizeOnBackPress()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setContent {
            EtollTheme(getAppMode = { getAppMode(requireContext()) }) {
                val translations: TranslationsContainer by viewModel.translations.observeAsState(
                    TranslationsContainer()
                )
                val viewData by viewModel.viewData.observeAsState(
                    LanguageSettingsViewModelV2.LanguagesSettingsViewData()
                )
                val shouldShowStartIcon = viewModel.registrationMode.observeAsState()

                ContentWithTranslations(translations = translations) {
                    Scaffold(
                        topBar = {
                            LanguageSettingsToolbar(
                                shouldShowStartIcon.value != LanguageSettingsViewModelV2.RegistrationMode.REGISTRATION,
                                translations[TranslationKeys.LANGUAGE_SELECTION_HEADER]
                            )
                        },
                        bottomBar = { LanguageSettingsFooter(composeTranslations[TranslationKeys.MISC_SAVE_BUTTON]) },
                        content = { paddingValues ->
                            LanguageSettingsContent(
                                padding = paddingValues,
                                title = { LanguageSelectionTitle(composeTranslations[TranslationKeys.LANGUAGE_SELECTION_TITLE]) },
                                list = {
                                    LanguagesList(
                                        languages = viewData.availableLanguages,
                                        selectedLanguage = viewData.selectedLanguage
                                    )
                                }
                            )
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun LanguageSettingsToolbar(
        shouldShowStartIcon: Boolean,
        title: String
    ) {
        EtollToolbar(
            showStartIcon = shouldShowStartIcon,
            startIcon = {
                EtollBackIconButton(
                    onClick = findNavController()::popBackStack
                )
            },
            title = {
                EtollToolbarTitle(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    text = title
                )
            })
    }

    @Composable
    fun LanguageSettingsContent(
        padding: PaddingValues,
        title: @Composable () -> Unit,
        list: @Composable () -> Unit,
    ) {
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            title()
            list()
        }
    }

    @Composable
    private fun LanguagesList(
        languages: List<SupportedLanguages>,
        selectedLanguage: SupportedLanguages
    ) {
        languages.forEach {
            LanguageRow(
                selected = it == selectedLanguage,
                name = composeTranslations[it.translationName]
            ) {
                viewModel.onLanguageSelected(it)
            }
        }
    }

    @Composable
    private fun LanguageSelectionTitle(title: String) {
        Text(
            text = title,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = Dimens.XL_SPACING,
                    end = Dimens.XXL_SPACING,
                    top = Dimens.XL_SPACING,
                    bottom = Dimens.XL_SPACING,
                ),
            style = MaterialTheme.typography.h4bold,
            color = EtollTheme.colors.spoeTextContent
        )
    }

    @Composable
    fun LanguageRow(
        selected: Boolean,
        name: String,
        modifier: Modifier = Modifier,
        onClick: () -> Unit,
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            EtollRadio(
                selected, Modifier.padding(
                    start = Dimens.PADDING_CONTENT_HORIZONTAL
                )
            )
            Text(
                text = name,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = Dimens.M_SPACING,
                        horizontal = Dimens.S_SPACING
                    ),
                style = MaterialTheme.typography.p1Normal,
                color = EtollTheme.colors.spoeTextContent
            )
        }
    }

    @Composable
    fun LanguageSettingsFooter(text: String) {
        EtollButtonIsleWithDivider {
            EtollButtonWithDefaultPadding(
                text = text,
                onClick = { viewModel.onApplyNewLanguageClicked() }
            )

        }
    }
}