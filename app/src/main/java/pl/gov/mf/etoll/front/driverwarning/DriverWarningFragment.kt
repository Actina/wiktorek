package pl.gov.mf.etoll.front.driverwarning

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.fragment.findNavController
import pl.gov.mf.etoll.BaseComposeFragment
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.front.MainActivity
import pl.gov.mf.etoll.translations.TranslationKeys
import pl.gov.mf.etoll.translations.TranslationsContainer
import pl.gov.mf.etoll.ui.components.compose.combined.button.EtollButton
import pl.gov.mf.etoll.ui.components.compose.combined.buttonisle.EtollButtonIsle
import pl.gov.mf.etoll.ui.components.compose.combined.checkbox.EtollCheckbox
import pl.gov.mf.etoll.ui.components.compose.combined.isledivider.EtollIsleDivider
import pl.gov.mf.etoll.ui.components.compose.combined.utils.SystemRippleDisabled
import pl.gov.mf.etoll.ui.components.compose.helpers.getIconInMode
import pl.gov.mf.etoll.ui.components.compose.theme.Dimens
import pl.gov.mf.etoll.ui.components.compose.theme.EtollTheme
import pl.gov.mf.etoll.ui.components.compose.theme.h2bold
import pl.gov.mf.etoll.ui.components.compose.theme.p2Semibold
import pl.gov.mf.etoll.ui.components.compose.theme.p2regular
import pl.gov.mf.mobile.utils.getAppMode

class DriverWarningFragment :
    BaseComposeFragment<DriverWarningFragmentViewModel, DriverWarningFragmentComponent>() {

    override fun createComponent(): DriverWarningFragmentComponent =
        (context as MainActivity).component.plus(DriverWarningFragmentModule(this, lifecycle))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        component.inject(viewModel)
        minimizeOnBackPress()

        if (viewModel.shouldSkipScreen()) onContinueClicked()
    }

    override fun getViewModel(): DriverWarningFragmentViewModel = viewModel

    override fun getStatusBarColor(): Int = R.color.statusBarGray

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setContent {
            DriverWarningScaffold(
                content = {
                    DriverWarningContent(this)
                },
                footerIsleDivider = {
                    EtollIsleDivider()
                },
                footer = {
                    DriverWarningFooter()
                })
        }
    }

    @Composable
    private fun DriverWarningScaffold(
        content: @Composable ColumnScope.() -> Unit,
        footerIsleDivider: @Composable ColumnScope.() -> Unit,
        footer: @Composable ColumnScope.() -> Unit,
    ) {
        EtollTheme(getAppMode = { getAppMode(requireContext()) }) {
            val translations: TranslationsContainer by viewModel.translations.observeAsState(
                TranslationsContainer(keys = mutableListOf())
            )
            ContentWithTranslations(translations = translations) {
                Column {
                    content()
                    footerIsleDivider()
                    footer()
                }
            }
        }
    }

    @Composable
    private fun DriverWarningContent(scope: ColumnScope) {
        scope.run {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Image(
                    painterResource(
                        id = getIconInMode(
                            R.drawable.ic_driver_warning_new_light,
                            R.drawable.ic_driver_warning_new_dark
                        )
                    ),
                    contentDescription = "driver warning icon",
                )

                Text(
                    text = composeTranslations[TranslationKeys.DRIVER_WARNING_HEADER],
                    color = EtollTheme.colors.textColorRed,
                    style = EtollTheme.typography.h2bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 42.dp)
                        .padding(horizontal = Dimens.PADDING_CONTENT_HORIZONTAL)
                )
                Text(
                    text = composeTranslations[TranslationKeys.DRIVER_WARNING_CONTENT],
                    color = EtollTheme.colors.spoeTextContent,
                    style = EtollTheme.typography.p2regular,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = Dimens.H_SPACING, bottom = Dimens.XXL_SPACING)
                        .padding(horizontal = Dimens.PADDING_CONTENT_HORIZONTAL)
                )

                DriverWarningCheckboxContainer(viewModel::onCheckboxClicked)
            }
        }
    }

    @Composable
    private fun DriverWarningCheckboxContainer(onClick: (Boolean) -> Unit) {
        var checkedState by remember { mutableStateOf(false) }
        DriverWarningCheckboxContainerStateless(checked = checkedState) {
            checkedState = !checkedState
            onClick(checkedState)
        }
    }

    @Composable
    private fun DriverWarningCheckboxContainerStateless(checked: Boolean, onClick: () -> Unit) {
        val containerInteractionSource = remember { MutableInteractionSource() }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.XL_SPACING)
                .clickable(
                    enabled = true,
                    interactionSource = containerInteractionSource,
                    indication = rememberRipple()
                ) { onClick() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {

            SystemRippleDisabled {
                EtollCheckbox(
                    checked = checked,
                    interactionSource = containerInteractionSource
                ) { onClick() }
            }

            Text(
                text = composeTranslations[TranslationKeys.DRIVER_WARNING_CHECKBOX_TEXT],
                color = EtollTheme.colors.textPrimary,
                style = EtollTheme.typography.p2Semibold
            )
        }
    }

    @Composable
    private fun DriverWarningFooter() {
        EtollButtonIsle(button = {
            EtollButton(
                modifier = Modifier.padding(Dimens.XL_SPACING),
                text = composeTranslations[TranslationKeys.DRIVER_WARNING_CONTINUE],
                onClick = ::onContinueClicked
            )
        })
    }

    private fun onContinueClicked() {
        findNavController().navigate(
            DriverWarningFragmentDirections.actionShowDashboard(signComponentUseCase.executeCustomSign())
        )
    }
}