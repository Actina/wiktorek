package pl.gov.mf.etoll.ui.components.compose.combined.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.etoll.ui.components.compose.combined.button.EtollButtonWithDefaultPadding
import pl.gov.mf.etoll.ui.components.compose.combined.buttonisle.EtollButtonIsle
import pl.gov.mf.etoll.ui.components.compose.theme.*
import pl.gov.mf.etoll.ui.components.compose.helpers.getIconInMode

@Composable
fun EtollDialogDefault(
    modifier: Modifier = Modifier,
    header: @Composable ColumnScope.() -> Unit,
    title: @Composable ColumnScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
    isleDivider: @Composable ColumnScope.() -> Unit = {},
    buttonsIsle: @Composable () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            modifier = modifier.wrapContentHeight(),
            color = EtollTheme.colors.dialogBackground,
            shape = EtollTheme.shapes.dialogRoundedCorner
        ) {
            EtollDialogContent(
                modifier = Modifier
                    .padding(top = Dimens.XL_SPACING),
                header = { header() },
                title = { title() },
                content = { content() },
                isleDivider = { isleDivider() },
                buttonsIsle = buttonsIsle
            )
        }
    }
}

@Preview
@Composable
fun EtollDialogDefaultPreview() {
    EtollDialogDefault(
        header = {
            Image(
                painter = painterResource(
                    id = getIconInMode(
                        icLightRes = R.drawable.ic_crm_info_light,
                        icDarkRes = R.drawable.ic_crm_info_dark,
                    )
                ),
                modifier = Modifier.fillMaxWidth(),
                contentDescription = ""
            )
        },
        title = {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.XL_SPACING)
                    .padding(top = Dimens.XL_SPACING),
                text = "Title",
                textAlign = TextAlign.Center,
                style = EtollTheme.typography.h2bold,
                color = EtollTheme.colors.dialogInfoHeader
            )
        },
        content = {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.M_SPACING),
                text = "Content",
                textAlign = TextAlign.Center,
                style = EtollTheme.typography.p1Normal,
                color = EtollTheme.colors.textPrimary
            )
        },
        buttonsIsle = {
            EtollButtonIsle(button = {
                EtollButtonWithDefaultPadding(text = "Ok") {

                }
            })
        },
        isleDivider = {},
        onDismiss = {}
    )
}