package pl.gov.mf.etoll.front.regulationsv2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.fragment.findNavController
import pl.gov.mf.etoll.BaseComposeFragment
import pl.gov.mf.etoll.front.driverwarning.DriverWarningFragmentDirections
import pl.gov.mf.etoll.translations.TranslationKeys
import pl.gov.mf.etoll.translations.TranslationsContainer
import pl.gov.mf.etoll.ui.components.compose.combined.buttonisle.EtollButtonIsle
import pl.gov.mf.etoll.ui.components.compose.combined.dialogs.EtollDialogDefault
import pl.gov.mf.etoll.ui.components.compose.combined.isledivider.EtollIsleDivider
import pl.gov.mf.etoll.ui.components.compose.combined.pdf.EbiletPdfView
import pl.gov.mf.etoll.ui.components.compose.combined.toolbar.EtollCloseIconButton
import pl.gov.mf.etoll.ui.components.compose.combined.toolbar.EtollToolbar
import pl.gov.mf.etoll.ui.components.compose.combined.toolbar.EtollToolbarTitle
import pl.gov.mf.etoll.ui.components.compose.theme.*
import pl.gov.mf.etoll.utils.observeOnce
import pl.gov.mf.etoll.ui.components.compose.combined.button.EtollButtonWithDefaultPadding
import pl.gov.mf.mobile.utils.getAppMode

class RegulationsFragmentV2 :
    BaseComposeFragment<RegulationsFragmentV2ViewModel, RegulationsFragmentV2Component>() {

    override fun getViewModel(): RegulationsFragmentV2ViewModel = viewModel

    override fun createComponent(): RegulationsFragmentV2Component =
        activityComponent.plus(RegulationsFragmentV2Module(this, lifecycle))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        component.inject(viewModel)
    }

    override fun onResume() {
        super.onResume()
        viewModel.regulationsMode.observeOnce(this) { mode ->
            if (!mode.isAllAccepted()) {
                minimizeOnBackPress()
            }
        }
        viewModel.navigate.observeOnce(this) {
            when (it) {
                RegulationsFragmentV2ViewModel.NavigationTargets.NONE -> {}
                RegulationsFragmentV2ViewModel.NavigationTargets.REGISTERED -> {
                    viewModel.resetNavigation()
                    findNavController().navigate(
                        RegulationsFragmentV2Directions.actionRegistered(
                            signComponentUseCase.executeCustomSign()
                        )
                    )
                }
                RegulationsFragmentV2ViewModel.NavigationTargets.DRIVER_WARNING -> {
                    viewModel.resetNavigation()
                    findNavController().navigate(
                        DriverWarningFragmentDirections.actionDriverWarning(
                            signComponentUseCase.executeCustomSign()
                        )
                    )
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
            val mode by viewModel.regulationsMode.observeAsState()
            val fileName by viewModel.filename.observeAsState()
            val showDialog by viewModel.shouldShowAcceptDialog.observeAsState()
            val translations by viewModel.translations.observeAsState(TranslationsContainer())

            showDialog?.let {
                NewRegulationsDialog(
                    translationsContainer = translations,
                    shouldBeShown = it,
                    onOkClicked = viewModel::onNewRegulationsOkClicked
                )
            }

            RegulationsScaffold(
                translationsContainer = translations,
                toolbar = {
                    EtollToolbar(
                        startIcon = {
                            EtollCloseIconButton(
                                onClick =
                                findNavController()::popBackStack
                            )
                        },
                        showStartIcon = mode.isAllAccepted(),
                        title = {
                            EtollToolbarTitle(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                text = composeTranslations[TranslationKeys.REGISTRATION_REGULATIONS_TITLE]
                            )
                        }
                    )
                },
                content = {
                    EbiletPdfView(modifier = Modifier.fillMaxWidth(), pdfAssetName = fileName) {
                        // TODO: error support ?
                    }
                },
                footerDivider = {
                    EtollIsleDivider(modifier = Modifier.align(Alignment.BottomCenter))
                },
                footer = {
                    RegulationsFooter()
                },
                footerVisible = !mode.isAllAccepted()
            )
        }
    }

    private fun RegulationsProvider.Mode?.isAllAccepted() =
        this == RegulationsProvider.Mode.ALL_ACCEPTED

    @Composable
    private fun NewRegulationsDialog(
        translationsContainer: TranslationsContainer,
        shouldBeShown: Boolean,
        onOkClicked: () -> Unit,
    ) {
        NewRegulationsDialogScaffold(
            shouldBeShown = shouldBeShown,
            translationsContainer = translationsContainer
        ) {
            EtollDialogDefault(
                header = {
                    Text(
                        modifier = Modifier
                            .padding(horizontal = Dimens.XL_SPACING),
                        text = composeTranslations[TranslationKeys.NEW_REGULATIONS_WHATSNEW_DIALOG_HEADER],
                        style = EtollTheme.typography.h4BoldHeader,
                        color = EtollTheme.colors.dialogHeaderTextPrimary
                    )
                },
                title = {
                    Text(
                        modifier = Modifier
                            .padding(horizontal = Dimens.XL_SPACING)
                            .padding(top = Dimens.M_SPACING),
                        text = composeTranslations[TranslationKeys.NEW_REGULATIONS_WHATSNEW_DIALOG_TITLE],
                        style = EtollTheme.typography.p1Semibold,
                        color = EtollTheme.colors.dialogHeaderTextPrimary
                    )
                },
                content = {
                    Text(
                        modifier = Modifier
                            .padding(horizontal = Dimens.XL_SPACING)
                            .padding(bottom = Dimens.XL_SPACING)
                            .padding(top = Dimens.M_SPACING),
                        text = composeTranslations[TranslationKeys.NEW_REGULATIONS_WHATSNEW_DIALOG_CONTENT],
                        style = EtollTheme.typography.p2regular,
                        color = EtollTheme.colors.textPrimary
                    )
                },
                isleDivider = {
                    EtollIsleDivider()
                },
                buttonsIsle = {
                    EtollButtonIsle(button = {
                        EtollButtonWithDefaultPadding(text = composeTranslations[TranslationKeys.MISC_OK_BUTTON]) { onOkClicked() }
                    })
                },
                onDismiss = {
                })
        }
    }

    @Composable
    private fun RegulationsScaffold(
        translationsContainer: TranslationsContainer,
        toolbar: @Composable ColumnScope.() -> Unit,
        content: @Composable BoxScope.() -> Unit,
        footerDivider: @Composable BoxScope.() -> Unit,
        footer: @Composable ColumnScope.() -> Unit,
        footerVisible: Boolean,
    ) {
        EtollTheme(getAppMode = { getAppMode(requireContext()) }) {
            ContentWithTranslations(translations = translationsContainer) {
                Column {
                    toolbar()
                    Box(modifier = Modifier.weight(1f)) {
                        content()
                        if (footerVisible)
                            footerDivider()
                    }
                    if (footerVisible)
                        footer()
                }
            }
        }
    }

    @Composable
    private fun NewRegulationsDialogScaffold(
        translationsContainer: TranslationsContainer,
        shouldBeShown: Boolean,
        dialog: @Composable () -> Unit,
    ) {
        if (shouldBeShown)
            EtollTheme(getAppMode = { getAppMode(requireContext()) }) {
                ContentWithTranslations(translations = translationsContainer) {
                    dialog()
                }
            }
    }

    @Composable
    fun RegulationsFooter(
        modifier: Modifier = Modifier,
    ) {
        EtollButtonIsle(modifier = modifier, button = {
            EtollButtonWithDefaultPadding(text = composeTranslations[TranslationKeys.REGISTRATION_REGULATIONS_ACCEPT]) {
                getViewModel().acceptRegulations()
            }
        })
    }

}