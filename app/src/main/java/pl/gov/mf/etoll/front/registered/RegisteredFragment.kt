package pl.gov.mf.etoll.front.registered

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.fragment.findNavController
import pl.gov.mf.etoll.BaseComposeFragment
import pl.gov.mf.etoll.front.MainActivity
import pl.gov.mf.etoll.translations.NO_TRANSLATION
import pl.gov.mf.etoll.translations.TranslationKeys
import pl.gov.mf.etoll.ui.components.compose.combined.businessnumber.EtollBusinessNumber
import pl.gov.mf.etoll.ui.components.compose.combined.button.EtollButtonWithDefaultPadding
import pl.gov.mf.etoll.ui.components.compose.combined.buttonisle.EtollButtonIsleWithDivider
import pl.gov.mf.etoll.ui.components.compose.combined.isledivider.EtollIsleDivider
import pl.gov.mf.etoll.ui.components.compose.combined.isledivider.IsleDividerShadeType
import pl.gov.mf.etoll.ui.components.compose.combined.toolbar.EtollToolbar
import pl.gov.mf.etoll.ui.components.compose.combined.toolbar.EtollToolbarTitleDefault
import pl.gov.mf.etoll.ui.components.compose.theme.Dimens
import pl.gov.mf.etoll.ui.components.compose.theme.EtollTheme
import pl.gov.mf.etoll.ui.components.compose.theme.h4bold
import pl.gov.mf.etoll.ui.components.compose.theme.p2regular
import pl.gov.mf.etoll.utils.observeOnce
import pl.gov.mf.mobile.ui.components.combined.htmlText.EtollHtmlTextWithIcon
import pl.gov.mf.mobile.utils.PredefiniedIntents

class RegisteredFragment :
    BaseComposeFragment<RegisteredViewModel, RegisteredFragmentComponent>() {

    override fun getViewModel(): RegisteredViewModel = viewModel

    override fun createComponent(): RegisteredFragmentComponent =
        (context as MainActivity).component.plus(
            RegisteredFragmentModule(this, lifecycle)
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        component.inject(viewModel)
        minimizeOnBackPress()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setContent {
            EtollThemeWithModeAndTranslations(
                screenContent = {
                    RegisteredModeContent()
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.navigationSource.observeOnce(this) { target ->
            when (target) {
                RegisteredViewModel.NavTarget.NONE -> {}
                RegisteredViewModel.NavTarget.DRIVER_WARNING -> {
                    viewModel.resetNavigationTarget()
                    viewModel.markViewStepAsShown {
                        findNavController().navigate(
                            RegisteredFragmentDirections.actionDriverWarning(
                                signComponentUseCase.executeCustomSign()
                            )
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun RegisteredModeContent() {
        val businessNumber by viewModel.businessNumber.observeAsState()

        Scaffold(
            topBar = {
                EtollToolbar(title = {
                    EtollToolbarTitleDefault(
                        rowScope = this,
                        text = composeTranslations[TranslationKeys.REGISTERED_MODE_HEADER]
                    )
                })
            },
            bottomBar = {
                EtollButtonIsleWithDivider(
                    button = {
                        EtollButtonWithDefaultPadding(
                            text = composeTranslations[TranslationKeys.REGISTERED_MODE_CONTINUE_BUTTON],
                            onClick = viewModel::onContinueClicked
                        )
                    }
                )
            },
            content = { paddingValues ->
                Column {
                    EtollBusinessNumber(
                        title = composeTranslations[TranslationKeys.REGISTERED_MODE_BUSINESS_IDENTIFIER_CONTENT],
                        businessNumber = businessNumber!!,
                        onShareAreaClicked = { onShareButtonClicked() }
                    )
                    EtollIsleDivider(dividerType = IsleDividerShadeType.BOTTOM)
                    Column(modifier = Modifier.padding(paddingValues)) {
                        Text(
                            text = composeTranslations[TranslationKeys.REGISTERED_MODE_ASSIGN_VEHICLE_TITLE],
                            modifier = Modifier.padding(
                                start = Dimens.PADDING_CONTENT_VERTICAL,
                                top = Dimens.M_SPACING,
                            ),
                            color = EtollTheme.colors.spoeTextContent,
                            style = EtollTheme.typography.h4bold
                        )
                        HtmlLinkDescriptionRow(
                            text = composeTranslations[TranslationKeys.REGISTERED_MODE_ASSIGN_VEHICLE_CONTENT],
                            htmlText = composeTranslations[TranslationKeys.REGISTERED_MODE_ASSIGN_VEHICLE_LINK],
                            onLinkClicked = {
                                startActivity(PredefiniedIntents.openWebsiteIntent(it))
                            })
                        HtmlLinkDescriptionRow(text = composeTranslations[TranslationKeys.REGISTERED_MODE_ASSIGN_SENT_CONTENT],
                            htmlText = composeTranslations[TranslationKeys.REGISTERED_MODE_ASSIGN_SENT_LINK],
                            onLinkClicked = {
                                startActivity(PredefiniedIntents.openWebsiteIntent(it))
                            })
                    }
                }
            }
        )
    }

    @Composable
    private fun HtmlLinkDescriptionRow(
        text: String,
        htmlText: String,
        onLinkClicked: (String) -> Unit,
    ) {
        //Required - onLinkClicked should return only correct links from correct translations
        if (htmlText == NO_TRANSLATION) return

        Text(
            text = text,
            modifier = Modifier.padding(
                start = Dimens.PADDING_CONTENT_VERTICAL,
                end = Dimens.PADDING_CONTENT_VERTICAL,
                top = Dimens.M_SPACING,
            ),
            color = EtollTheme.colors.spoeTextContent,
            style = EtollTheme.typography.p2regular
        )
        EtollHtmlTextWithIcon(htmlText, onLinkClicked)
    }

    private fun onShareButtonClicked() {
        viewModel.businessNumber.value?.let { businessNumber ->
            // action share
            startActivity(
                Intent.createChooser(
                    PredefiniedIntents.shareIntent(businessNumber),
                    null
                )
            )
        }
    }
}